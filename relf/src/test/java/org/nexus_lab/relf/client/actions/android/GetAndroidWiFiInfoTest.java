package org.nexus_lab.relf.client.actions.android;

import static org.nexus_lab.relf.utils.ReflectionUtils.Parameter.from;
import static org.nexus_lab.relf.utils.ReflectionUtils.callConstructor;
import static org.nexus_lab.relf.utils.ReflectionUtils.callInstanceMethod;
import static org.nexus_lab.relf.utils.ReflectionUtils.callStaticMethod;
import static org.nexus_lab.relf.utils.ReflectionUtils.getStaticFieldValue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConfiguration.AuthAlgorithm;
import android.net.wifi.WifiConfiguration.GroupCipher;
import android.net.wifi.WifiConfiguration.KeyMgmt;
import android.net.wifi.WifiConfiguration.PairwiseCipher;
import android.net.wifi.WifiConfiguration.Protocol;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;

import androidx.test.core.app.ApplicationProvider;

import com.google.protobuf.Internal;
import org.nexus_lab.relf.client.actions.ActionCallback;
import org.nexus_lab.relf.client.exceptions.TestSuccessException;
import org.nexus_lab.relf.lib.rdfvalues.RDFBytes;
import org.nexus_lab.relf.lib.rdfvalues.android.RDFAndroidDhcpInfo;
import org.nexus_lab.relf.lib.rdfvalues.android.RDFAndroidWiFiConfiguration;
import org.nexus_lab.relf.lib.rdfvalues.android.RDFAndroidWiFiConnectionInfo;
import org.nexus_lab.relf.lib.rdfvalues.android.RDFAndroidWiFiInfo;
import org.nexus_lab.relf.lib.rdfvalues.android.RDFAndroidWiFiScanResult;
import org.nexus_lab.relf.utils.ByteUtils;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Shadows;
import org.robolectric.shadows.ShadowWifiInfo;
import org.robolectric.shadows.ShadowWifiManager;

import java.lang.reflect.Field;
import java.math.BigInteger;
import java.net.Inet4Address;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Random;

/**
 * @author Ruipeng Zhang
 */
@RunWith(RobolectricTestRunner.class)
public class GetAndroidWiFiInfoTest {
    private Context context;
    private DhcpInfo dhcp;
    private WifiInfo connection;
    private WifiManager manager;
    private final ActionCallback<RDFAndroidWiFiInfo> callback = wifiInfo -> {
        assertEquals(manager.is5GHzBandSupported(), wifiInfo.getIs5GHzBandSupported());
        assertEquals(manager.isScanAlwaysAvailable(), wifiInfo.getIsScanAlwaysAvailable());
        assertConnectionEquals(wifiInfo.getConnectionInfo());
        assertDhcpEquals(wifiInfo.getDhcpInfo());
        assertEquals(manager.getConfiguredNetworks().size(),
                wifiInfo.getConfiguredNetworks().size());
        for (int i = 0; i < wifiInfo.getConfiguredNetworks().size(); i++) {
            assertConfigurationEquals(manager.getConfiguredNetworks().get(i),
                    wifiInfo.getConfiguredNetworks().get(i));
        }
        assertEquals(manager.getScanResults().size(), wifiInfo.getScanResults().size());
        for (int i = 0; i < wifiInfo.getScanResults().size(); i++) {
            assertScanResultEquals(manager.getScanResults().get(i),
                    wifiInfo.getScanResults().get(i));
        }
        throw new TestSuccessException();
    };

    private void assertConnectionEquals(RDFAndroidWiFiConnectionInfo info) {
        assertEquals(connection.getSSID().replaceAll("^\"|\"$", ""), info.getSsid());
        assertEquals(connection.getBSSID(), info.getBssid());
        assertEquals(connection.getFrequency(), info.getFrequency().intValue());
        assertEquals(connection.getLinkSpeed(), info.getLinkSpeed().intValue());
        assertEquals(connection.getNetworkId(), info.getNetworkId().intValue());
        assertEquals(connection.getRssi(), info.getRssi().intValue());
        assertEquals(connection.getMacAddress().toUpperCase(),
                info.getMacAddress().getHumanReadableAddress());
        assertEquals(new RDFBytes(BigInteger.valueOf(connection.getIpAddress()).toByteArray()),
                info.getIpAddress().getPackedBytes());
    }

    private void assertDhcpEquals(RDFAndroidDhcpInfo info) {
        assertEquals(new RDFBytes(BigInteger.valueOf(dhcp.gateway).toByteArray()),
                info.getGateway().getPackedBytes());
        assertEquals(new RDFBytes(BigInteger.valueOf(dhcp.ipAddress).toByteArray()),
                info.getIpAddress().getPackedBytes());
        assertEquals(new RDFBytes(BigInteger.valueOf(dhcp.netmask).toByteArray()),
                info.getNetmask().getPackedBytes());
        assertEquals(new RDFBytes(BigInteger.valueOf(dhcp.serverAddress).toByteArray()),
                info.getServerAddress().getPackedBytes());
        assertEquals(new RDFBytes(BigInteger.valueOf(dhcp.dns1).toByteArray()),
                info.getDns().get(0).getPackedBytes());
        assertEquals(new RDFBytes(BigInteger.valueOf(dhcp.dns2).toByteArray()),
                info.getDns().get(1).getPackedBytes());
        assertEquals((long) dhcp.leaseDuration, info.getLeaseDuration().getValue().longValue());
    }

    private void assertBitSetEquals(BitSet expected, List<? extends Internal.EnumLite> actual) {
        if (expected != null && expected.length() > 0) {
            for (Internal.EnumLite e : actual) {
                assertNotNull(e);
                assertTrue(expected.get(e.getNumber()));
            }

            for (int i = 0; i < expected.length(); i++) {
                if (expected.get(i)) {
                    try {
                        Internal.EnumLite value = callStaticMethod(actual.get(0).getClass(),
                                "forNumber", from(int.class, i));
                        assertTrue(actual.contains(value));
                    } catch (ReflectiveOperationException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        } else {
            assertEquals(0, actual.size());
        }
    }

    private void assertConfigurationEquals(WifiConfiguration expected,
            RDFAndroidWiFiConfiguration actual) {
        assertEquals(expected.status, actual.getStatus().getNumber());
        assertEquals(expected.SSID, actual.getSsid());
        assertEquals(expected.BSSID, actual.getBssid());
        assertEquals(expected.hiddenSSID, actual.getIsHidden());
        assertEquals(expected.networkId, actual.getNetworkId().intValue());
        assertBitSetEquals(expected.allowedAuthAlgorithms, actual.getAllowedAuthAlgorithms());
        assertBitSetEquals(expected.allowedGroupCiphers, actual.getAllowedGroupCiphers());
        assertBitSetEquals(expected.allowedKeyManagement, actual.getAllowedKeyManagement());
        assertBitSetEquals(expected.allowedPairwiseCiphers, actual.getAllowedPairwiseCiphers());
        assertBitSetEquals(expected.allowedProtocols, actual.getAllowedProtocols());
    }

    private void assertScanResultEquals(ScanResult expected, RDFAndroidWiFiScanResult actual) {
        assertEquals(expected.SSID, actual.getSsid());
        assertEquals(expected.BSSID, actual.getBssid());
        assertEquals(expected.level, actual.getRssi().intValue());
        assertEquals(expected.frequency, actual.getFrequency().intValue());
        assertEquals(expected.capabilities, actual.getCapabilities());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            assertEquals(expected.channelWidth, actual.getChannelWidth().getNumber());
            assertEquals(expected.centerFreq0, actual.getCenterFrequency0().intValue());
            assertEquals(expected.centerFreq1, actual.getCenterFrequency1().intValue());
            assertEquals(expected.is80211mcResponder(), actual.getIsIEEE80211McResponder());
        }
    }

    private void createWifiConnection() throws Exception {
        Random random = new Random();
        connection = callConstructor(WifiInfo.class);
        ShadowWifiInfo shadowWifiInfo = Shadows.shadowOf(connection);
        shadowWifiInfo.setSSID(RandomStringUtils.randomAlphabetic(16));
        byte[] bssid = new byte[6];
        random.nextBytes(bssid);
        shadowWifiInfo.setBSSID(ByteUtils.toMacAddress(bssid));
        shadowWifiInfo.setFrequency(random.nextInt());
        byte[] ip = new byte[4];
        random.nextBytes(ip);
        shadowWifiInfo.setInetAddress(
                Inet4Address.getByAddress(RandomStringUtils.randomAlphabetic(10), ip));
        shadowWifiInfo.setLinkSpeed(random.nextInt());
        shadowWifiInfo.setNetworkId(random.nextInt());
        shadowWifiInfo.setMacAddress(ByteUtils.toMacAddress(bssid));
        shadowWifiInfo.setRssi(random.nextInt());
    }

    private void createDhcpInfo() throws Exception {
        Random random = new Random();
        dhcp = callConstructor(DhcpInfo.class);
        for (Field field : DhcpInfo.class.getDeclaredFields()) {
            if (field.getType().equals(int.class)) {
                field.setInt(dhcp, random.nextInt());
            }
        }
    }

    private WifiConfiguration createWifiConfiguration() {
        Random random = new Random();
        WifiConfiguration configuration = new WifiConfiguration();
        configuration.status = random.nextInt(3);
        configuration.SSID = RandomStringUtils.randomAlphabetic(10);
        byte[] bssid = new byte[6];
        random.nextBytes(bssid);
        configuration.BSSID = ByteUtils.toMacAddress(bssid);
        configuration.hiddenSSID = random.nextBoolean();
        configuration.networkId = random.nextInt();
        configuration.allowedAuthAlgorithms = new BitSet();
        for (int i = 0; i < random.nextInt(AuthAlgorithm.strings.length); i++) {
            configuration.allowedAuthAlgorithms.set(random.nextInt(AuthAlgorithm.strings.length));
        }
        configuration.allowedGroupCiphers = new BitSet();
        for (int i = 0; i < random.nextInt(GroupCipher.strings.length); i++) {
            configuration.allowedGroupCiphers.set(random.nextInt(GroupCipher.strings.length));
        }
        configuration.allowedKeyManagement = new BitSet();
        for (int i = 0; i < random.nextInt(KeyMgmt.strings.length); i++) {
            configuration.allowedKeyManagement.set(random.nextInt(KeyMgmt.strings.length));
        }
        configuration.allowedPairwiseCiphers = new BitSet();
        for (int i = 0; i < random.nextInt(PairwiseCipher.strings.length); i++) {
            configuration.allowedPairwiseCiphers.set(random.nextInt(PairwiseCipher.strings.length));
        }
        configuration.allowedProtocols = new BitSet();
        for (int i = 0; i < random.nextInt(Protocol.strings.length); i++) {
            configuration.allowedProtocols.set(random.nextInt(Protocol.strings.length));
        }
        return configuration;
    }

    @SuppressWarnings("JavaReflectionMemberAccess")
    private ScanResult createScanResult() throws Exception {
        Random random = new Random();
        ScanResult result = callConstructor(ScanResult.class);
        result.SSID = RandomStringUtils.randomAlphabetic(10);
        byte[] bssid = new byte[6];
        random.nextBytes(bssid);
        result.BSSID = ByteUtils.toMacAddress(bssid);
        result.level = random.nextInt();
        result.frequency = random.nextInt();
        result.capabilities = RandomStringUtils.randomAlphanumeric(10);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            result.channelWidth = random.nextInt(5);
            result.centerFreq0 = random.nextInt();
            result.centerFreq1 = random.nextInt();
            if (random.nextBoolean()) {
                callInstanceMethod(result, "setFlag", from(long.class,
                        getStaticFieldValue(ScanResult.class, "FLAG_80211mc_RESPONDER")));
            }
        }
        return result;
    }

    @Before
    public void setup() throws Exception {
        context = ApplicationProvider.getApplicationContext();
        manager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        ShadowWifiManager shadowManager = Shadows.shadowOf(manager);
        shadowManager.setIs5GHzBandSupported(true);
        shadowManager.setIsScanAlwaysAvailable(false);
        createWifiConnection();
        shadowManager.setConnectionInfo(connection);
        createDhcpInfo();
        shadowManager.setDhcpInfo(dhcp);
        Random random = new Random();
        for (int i = 0; i < random.nextInt(5) + 1; i++) {
            manager.addNetwork(createWifiConfiguration());
        }
        List<ScanResult> results = new ArrayList<>();
        for (int i = 0; i < random.nextInt(5) + 1; i++) {
            results.add(createScanResult());
        }
        shadowManager.setScanResults(results);
    }

    @Test(expected = TestSuccessException.class)
    public void execute() {
        new GetAndroidWiFiInfo().execute(context, null, callback);
    }

}