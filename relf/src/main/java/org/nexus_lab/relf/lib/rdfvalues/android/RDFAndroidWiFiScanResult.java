package org.nexus_lab.relf.lib.rdfvalues.android;

import org.nexus_lab.relf.lib.rdfvalues.RDFDatetime;
import org.nexus_lab.relf.lib.rdfvalues.structs.RDFProtoStruct;
import org.nexus_lab.relf.proto.AndroidWiFiScanResult;

import net.badata.protobuf.converter.annotation.ProtoClass;
import net.badata.protobuf.converter.annotation.ProtoField;
import net.badata.protobuf.converter.mapping.Proto2MapperImpl;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Ruipeng Zhang
 */
@ProtoClass(value = AndroidWiFiScanResult.class, mapper = Proto2MapperImpl.class)
public class RDFAndroidWiFiScanResult extends RDFProtoStruct<AndroidWiFiScanResult> {
    @Getter
    @Setter
    @ProtoField
    private String ssid;
    @Getter
    @Setter
    @ProtoField
    private String bssid;
    @Getter
    @Setter
    @ProtoField(converter = RDFDatetime.Converter.class)
    private RDFDatetime lastSeen;
    @Getter
    @Setter
    @ProtoField
    private Integer rssi;
    @Getter
    @Setter
    @ProtoField
    private Integer frequency;
    @Getter
    @Setter
    @ProtoField
    private String capabilities;
    @Getter
    @Setter
    @ProtoField
    private AndroidWiFiScanResult.Bandwidth channelWidth;
    @Getter
    @Setter
    @ProtoField
    private Integer centerFrequency0;
    @Getter
    @Setter
    @ProtoField
    private Integer centerFrequency1;
    @Getter
    @Setter
    @ProtoField
    private Boolean isIEEE80211McResponder;
}
