package org.nexus_lab.relf.client.actions.android;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.ACCESS_WIFI_STATE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.SystemClock;

import org.nexus_lab.relf.client.actions.Action;
import org.nexus_lab.relf.client.actions.ActionCallback;
import org.nexus_lab.relf.client.actions.UsesPermission;
import org.nexus_lab.relf.lib.rdfvalues.RDFBytes;
import org.nexus_lab.relf.lib.rdfvalues.RDFDatetime;
import org.nexus_lab.relf.lib.rdfvalues.RDFDatetimeSeconds;
import org.nexus_lab.relf.lib.rdfvalues.RDFNull;
import org.nexus_lab.relf.lib.rdfvalues.android.RDFAndroidDhcpInfo;
import org.nexus_lab.relf.lib.rdfvalues.android.RDFAndroidWiFiConfiguration;
import org.nexus_lab.relf.lib.rdfvalues.android.RDFAndroidWiFiConnectionInfo;
import org.nexus_lab.relf.lib.rdfvalues.android.RDFAndroidWiFiInfo;
import org.nexus_lab.relf.lib.rdfvalues.android.RDFAndroidWiFiScanResult;
import org.nexus_lab.relf.lib.rdfvalues.client.MacAddress;
import org.nexus_lab.relf.lib.rdfvalues.client.RDFNetworkAddress;
import org.nexus_lab.relf.proto.AndroidWiFiConfiguration;
import org.nexus_lab.relf.proto.AndroidWiFiConnectionInfo;
import org.nexus_lab.relf.proto.AndroidWiFiInfo;
import org.nexus_lab.relf.proto.AndroidWiFiScanResult;
import org.nexus_lab.relf.proto.NetworkAddress;
import org.nexus_lab.relf.utils.ByteUtils;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * Note: there is no passpoint support currently.
 *
 * @author Ruipeng Zhang
 */
@UsesPermission(
        allOf = {ACCESS_WIFI_STATE},
        anyOf = {ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION}
)
public class GetAndroidWiFiInfo implements Action<RDFNull, RDFAndroidWiFiInfo> {

    @SuppressLint("HardwareIds")
    private RDFAndroidWiFiConnectionInfo createConnectionInfo(WifiInfo connection) {
        RDFAndroidWiFiConnectionInfo info = new RDFAndroidWiFiConnectionInfo();
        byte[] ipBytes = BigInteger.valueOf(connection.getIpAddress()).toByteArray();
        RDFNetworkAddress ip = new RDFNetworkAddress();
        ip.setAddressType(NetworkAddress.Family.INET);
        ip.setPackedBytes(new RDFBytes(ipBytes));
        info.setSsid(connection.getSSID().replaceAll("^\"|\"$", ""));
        info.setBssid(connection.getBSSID());
        info.setIpAddress(ip);
        if (connection.getMacAddress() != null) {
            MacAddress macAddress = new MacAddress(
                    ByteUtils.fromMacAddress(connection.getMacAddress()));
            info.setMacAddress(macAddress);
        }
        info.setIsHidden(connection.getHiddenSSID());
        info.setRssi(connection.getRssi());
        info.setFrequency(connection.getFrequency());
        info.setLinkSpeed(connection.getLinkSpeed());
        info.setNetworkId(connection.getNetworkId());
        info.setSupplicantState(AndroidWiFiConnectionInfo.SupplicantState.forNumber(
                connection.getSupplicantState().ordinal()));
        return info;
    }

    private RDFAndroidDhcpInfo createDhcpInfo(DhcpInfo dhcp) {
        RDFAndroidDhcpInfo info = new RDFAndroidDhcpInfo();

        RDFNetworkAddress ip = new RDFNetworkAddress();
        ip.setAddressType(NetworkAddress.Family.INET);
        ip.setPackedBytes(new RDFBytes(BigInteger.valueOf(dhcp.ipAddress).toByteArray()));
        info.setIpAddress(ip);

        RDFNetworkAddress gateway = new RDFNetworkAddress();
        gateway.setAddressType(NetworkAddress.Family.INET);
        gateway.setPackedBytes(new RDFBytes(BigInteger.valueOf(dhcp.gateway).toByteArray()));
        info.setGateway(gateway);

        RDFNetworkAddress netmask = new RDFNetworkAddress();
        netmask.setAddressType(NetworkAddress.Family.INET);
        netmask.setPackedBytes(new RDFBytes(BigInteger.valueOf(dhcp.netmask).toByteArray()));
        info.setNetmask(netmask);

        RDFNetworkAddress server = new RDFNetworkAddress();
        server.setAddressType(NetworkAddress.Family.INET);
        server.setPackedBytes(new RDFBytes(BigInteger.valueOf(dhcp.serverAddress).toByteArray()));
        info.setServerAddress(server);

        RDFNetworkAddress dns1 = new RDFNetworkAddress();
        dns1.setAddressType(NetworkAddress.Family.INET);
        dns1.setPackedBytes(new RDFBytes(BigInteger.valueOf(dhcp.dns1).toByteArray()));
        info.addDns(dns1);

        RDFNetworkAddress dns2 = new RDFNetworkAddress();
        dns2.setAddressType(NetworkAddress.Family.INET);
        dns2.setPackedBytes(new RDFBytes(BigInteger.valueOf(dhcp.dns2).toByteArray()));
        info.addDns(dns2);

        info.setLeaseDuration(new RDFDatetimeSeconds(dhcp.leaseDuration));

        return info;
    }

    private RDFAndroidWiFiConfiguration createNetworkConfiguration(
            WifiConfiguration configuration) {
        RDFAndroidWiFiConfiguration result = new RDFAndroidWiFiConfiguration();
        result.setStatus(AndroidWiFiConfiguration.Status.forNumber(configuration.status));
        result.setSsid(configuration.SSID);
        result.setBssid(configuration.BSSID);
        result.setIsHidden(configuration.hiddenSSID);
        result.setNetworkId(configuration.networkId);
        List<AndroidWiFiConfiguration.AuthAlgorithm> allowedAuthAlgorithms = new ArrayList<>();
        for (int i = 0; i < configuration.allowedAuthAlgorithms.length(); i++) {
            if (configuration.allowedAuthAlgorithms.get(i)) {
                allowedAuthAlgorithms.add(AndroidWiFiConfiguration.AuthAlgorithm.forNumber(i));
            }
        }
        List<AndroidWiFiConfiguration.GroupCipher> allowedGroupCiphers = new ArrayList<>();
        for (int i = 0; i < configuration.allowedGroupCiphers.length(); i++) {
            if (configuration.allowedGroupCiphers.get(i)) {
                allowedGroupCiphers.add(AndroidWiFiConfiguration.GroupCipher.forNumber(i));
            }
        }
        List<AndroidWiFiConfiguration.KeyManagement> allowedKeyManagement = new ArrayList<>();
        for (int i = 0; i < configuration.allowedKeyManagement.length(); i++) {
            if (configuration.allowedKeyManagement.get(i)) {
                allowedKeyManagement.add(AndroidWiFiConfiguration.KeyManagement.forNumber(i));
            }
        }
        List<AndroidWiFiConfiguration.PairwiseCipher> allowedPairwiseCiphers = new ArrayList<>();
        for (int i = 0; i < configuration.allowedPairwiseCiphers.length(); i++) {
            if (configuration.allowedPairwiseCiphers.get(i)) {
                allowedPairwiseCiphers.add(AndroidWiFiConfiguration.PairwiseCipher.forNumber(i));
            }
        }
        List<AndroidWiFiConfiguration.Protocol> allowedProtocols = new ArrayList<>();
        for (int i = 0; i < configuration.allowedProtocols.length(); i++) {
            if (configuration.allowedProtocols.get(i)) {
                allowedProtocols.add(AndroidWiFiConfiguration.Protocol.forNumber(i));
            }
        }
        result.setAllowedAuthAlgorithms(allowedAuthAlgorithms);
        result.setAllowedGroupCiphers(allowedGroupCiphers);
        result.setAllowedKeyManagement(allowedKeyManagement);
        result.setAllowedPairwiseCiphers(allowedPairwiseCiphers);
        result.setAllowedProtocols(allowedProtocols);
        return result;
    }

    private RDFAndroidWiFiScanResult createScanResult(ScanResult scan) {
        RDFAndroidWiFiScanResult result = new RDFAndroidWiFiScanResult();
        result.setSsid(scan.SSID);
        result.setBssid(scan.BSSID);
        result.setLastSeen(new RDFDatetime(scan.timestamp + SystemClock.elapsedRealtime() * 1000));
        result.setRssi(scan.level);
        result.setFrequency(scan.frequency);
        result.setCapabilities(scan.capabilities);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            result.setChannelWidth(AndroidWiFiScanResult.Bandwidth.forNumber(scan.channelWidth));
            result.setCenterFrequency0(scan.centerFreq0);
            result.setCenterFrequency1(scan.centerFreq1);
            result.setIsIEEE80211McResponder(scan.is80211mcResponder());
        }
        return result;
    }

    @Override
    public void execute(Context context, RDFNull request, ActionCallback<RDFAndroidWiFiInfo> callback) {
        WifiManager manager = (WifiManager) context.getApplicationContext().getSystemService(
                Context.WIFI_SERVICE);
        if (manager == null) {
            throw new NullPointerException("Cannot access Android WiFi Service");
        }
        RDFAndroidWiFiInfo wifiInfo = new RDFAndroidWiFiInfo();
        wifiInfo.setState(AndroidWiFiInfo.State.forNumber(manager.getWifiState()));
        wifiInfo.setIs5GHzBandSupported(manager.is5GHzBandSupported());
        wifiInfo.setIsDeviceToApRttSupported(manager.isDeviceToApRttSupported());
        wifiInfo.setIsEnhancedPowerReportingSupported(
                manager.isEnhancedPowerReportingSupported());
        wifiInfo.setIsP2PSupported(manager.isP2pSupported());
        wifiInfo.setIsPreferredNetworkOffloadSupported(
                manager.isPreferredNetworkOffloadSupported());
        wifiInfo.setIsScanAlwaysAvailable(manager.isScanAlwaysAvailable());
        wifiInfo.setIsTdlsSupported(manager.isTdlsSupported());
        if (manager.getConnectionInfo() != null) {
            wifiInfo.setConnectionInfo(createConnectionInfo(manager.getConnectionInfo()));
        }
        if (manager.getDhcpInfo() != null) {
            wifiInfo.setDhcpInfo(createDhcpInfo(manager.getDhcpInfo()));
        }
        List<WifiConfiguration> networks = manager.getConfiguredNetworks();
        if (networks != null) {
            for (WifiConfiguration network : networks) {
                wifiInfo.addConfiguredNetwork(createNetworkConfiguration(network));
            }
        }
        List<ScanResult> scanResults = manager.getScanResults();
        if (scanResults != null) {
            for (ScanResult result : scanResults) {
                wifiInfo.addScanResult(createScanResult(result));
            }
        }
        callback.onResponse(wifiInfo);
        callback.onComplete();
    }
}
