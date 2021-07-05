package org.nexus_lab.relf.lib.rdfvalues.android;

import org.nexus_lab.relf.lib.rdfvalues.structs.RDFProtoStruct;
import org.nexus_lab.relf.proto.AndroidBatteryInfo;

import net.badata.protobuf.converter.annotation.ProtoClass;
import net.badata.protobuf.converter.annotation.ProtoField;
import net.badata.protobuf.converter.mapping.Proto2MapperImpl;

import lombok.Getter;
import lombok.Setter;

/**
 * RDF wrapper of {@link AndroidBatteryInfo}
 *
 * @author Ruipeng Zhang
 */
@ProtoClass(value = AndroidBatteryInfo.class, mapper = Proto2MapperImpl.class)
public class RDFAndroidBatteryInfo extends RDFProtoStruct<AndroidBatteryInfo> {
    @Getter
    @Setter
    @ProtoField
    private AndroidBatteryInfo.ChargingStatus status;
    @Getter
    @Setter
    @ProtoField
    private AndroidBatteryInfo.Health health;
    @Getter
    @Setter
    @ProtoField
    private Boolean isPresent;
    @Getter
    @Setter
    @ProtoField
    private Float level;
    @Getter
    @Setter
    @ProtoField
    private AndroidBatteryInfo.PowerSource powerSource;
    @Getter
    @Setter
    @ProtoField
    private Integer voltage;
    @Getter
    @Setter
    @ProtoField
    private Integer temperature;
    @Getter
    @Setter
    @ProtoField
    private String technology;
}
