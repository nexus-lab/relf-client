package org.nexus_lab.relf.lib.rdfvalues.android;

import org.nexus_lab.relf.lib.rdfvalues.client.MacAddress;
import org.nexus_lab.relf.lib.rdfvalues.client.RDFNetworkAddress;
import org.nexus_lab.relf.lib.rdfvalues.structs.RDFProtoStruct;
import org.nexus_lab.relf.proto.AndroidWiFiConnectionInfo;

import net.badata.protobuf.converter.annotation.ProtoClass;
import net.badata.protobuf.converter.annotation.ProtoField;
import net.badata.protobuf.converter.mapping.Proto2MapperImpl;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Ruipeng Zhang
 */
@ProtoClass(value = AndroidWiFiConnectionInfo.class, mapper = Proto2MapperImpl.class)
public class RDFAndroidWiFiConnectionInfo extends RDFProtoStruct<AndroidWiFiConnectionInfo> {
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
    @ProtoField
    private RDFNetworkAddress ipAddress;
    @Getter
    @Setter
    @ProtoField(converter = MacAddress.Converter.class)
    private MacAddress macAddress;
    @Getter
    @Setter
    @ProtoField
    private Boolean isHidden;
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
    private Integer linkSpeed;
    @Getter
    @Setter
    @ProtoField
    private Integer networkId;
    @Getter
    @Setter
    @ProtoField
    private AndroidWiFiConnectionInfo.SupplicantState supplicantState;
}
