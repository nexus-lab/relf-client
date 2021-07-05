package org.nexus_lab.relf.lib.rdfvalues.android;

import org.nexus_lab.relf.lib.rdfvalues.structs.RDFProtoStruct;
import org.nexus_lab.relf.proto.AndroidHardwareInfo;

import net.badata.protobuf.converter.annotation.ProtoClass;
import net.badata.protobuf.converter.annotation.ProtoField;
import net.badata.protobuf.converter.mapping.Proto2MapperImpl;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Ruipeng Zhang
 */
@ProtoClass(value = AndroidHardwareInfo.class, mapper = Proto2MapperImpl.class)
public class RDFAndroidHardwareInfo extends RDFProtoStruct<AndroidHardwareInfo> {
    @Getter
    @Setter
    @ProtoField
    private String brand;
    @Getter
    @Setter
    @ProtoField
    private String manufacturer;
    @Getter
    @Setter
    @ProtoField
    private String model;
    @Getter
    @Setter
    @ProtoField
    private String product;
    @Getter
    @Setter
    @ProtoField
    private String hardware;
    @Getter
    @Setter
    @ProtoField
    private String board;
    @Getter
    @Setter
    @ProtoField
    private String device;
    @Getter
    @Setter
    @ProtoField
    private String serial;
    @Getter
    @Setter
    @ProtoField
    private List<String> supportedAbis;
    @Getter
    @Setter
    @ProtoField
    private String baseband;
    @Getter
    @Setter
    @ProtoField
    private String radio;
    @Getter
    @Setter
    @ProtoField
    private String bootloader;
    @Getter
    @Setter
    @ProtoField
    private Boolean isEmulator;
    @Getter
    @Setter
    @ProtoField
    private Boolean isContainer;
    @Getter
    @Setter
    @ProtoField
    private Boolean isDebuggable;
}
