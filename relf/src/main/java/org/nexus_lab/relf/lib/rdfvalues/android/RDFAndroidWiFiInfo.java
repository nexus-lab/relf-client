package org.nexus_lab.relf.lib.rdfvalues.android;

import org.nexus_lab.relf.lib.rdfvalues.structs.RDFProtoStruct;
import org.nexus_lab.relf.proto.AndroidWiFiInfo;

import net.badata.protobuf.converter.annotation.ProtoClass;
import net.badata.protobuf.converter.annotation.ProtoField;
import net.badata.protobuf.converter.mapping.Proto2MapperImpl;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Ruipeng Zhang
 */
@ProtoClass(value = AndroidWiFiInfo.class, mapper = Proto2MapperImpl.class)
public class RDFAndroidWiFiInfo extends RDFProtoStruct<AndroidWiFiInfo> {
    @Getter
    @Setter
    @ProtoField
    private AndroidWiFiInfo.State state;
    @Getter
    @Setter
    @ProtoField
    private RDFAndroidWiFiConnectionInfo connectionInfo;
    @Getter
    @Setter
    @ProtoField
    private RDFAndroidDhcpInfo dhcpInfo;
    @Getter
    @Setter
    @ProtoField
    private List<RDFAndroidWiFiConfiguration> configuredNetworks;
    @Getter
    @Setter
    @ProtoField
    private List<RDFAndroidWiFiScanResult> scanResults;
    @Getter
    @Setter
    @ProtoField
    private Boolean is5GHzBandSupported;
    @Getter
    @Setter
    @ProtoField
    private Boolean isDeviceToApRttSupported;
    @Getter
    @Setter
    @ProtoField
    private Boolean isEnhancedPowerReportingSupported;
    @Getter
    @Setter
    @ProtoField
    private Boolean isP2PSupported;
    @Getter
    @Setter
    @ProtoField
    private Boolean isPreferredNetworkOffloadSupported;
    @Getter
    @Setter
    @ProtoField
    private Boolean isScanAlwaysAvailable;
    @Getter
    @Setter
    @ProtoField
    private Boolean isTdlsSupported;

    /**
     * @param configuration WiFi configuration to be added
     */
    public void addConfiguredNetwork(RDFAndroidWiFiConfiguration configuration) {
        if (configuredNetworks == null) {
            configuredNetworks = new ArrayList<>();
        }
        configuredNetworks.add(configuration);
    }

    /**
     * @param result WiFi scan result to be added
     */
    public void addScanResult(RDFAndroidWiFiScanResult result) {
        if (scanResults == null) {
            scanResults = new ArrayList<>();
        }
        scanResults.add(result);
    }
}
