package org.nexus_lab.relf.lib.rdfvalues.android;

import org.nexus_lab.relf.lib.rdfvalues.structs.RDFProtoStruct;
import org.nexus_lab.relf.proto.AndroidDeviceInfo;

import net.badata.protobuf.converter.annotation.ProtoClass;
import net.badata.protobuf.converter.annotation.ProtoField;
import net.badata.protobuf.converter.mapping.Proto2MapperImpl;

import lombok.Getter;
import lombok.Setter;

/**
 * RDF wrapper of {@link AndroidDeviceInfo}
 *
 * @author Ruipeng Zhang
 */
@ProtoClass(value = AndroidDeviceInfo.class, mapper = Proto2MapperImpl.class)
public class RDFAndroidDeviceInfo extends RDFProtoStruct<AndroidDeviceInfo> {
    @Getter
    @Setter
    @ProtoField
    private RDFAndroidHardwareInfo hardware;
    @Getter
    @Setter
    @ProtoField
    private RDFAndroidOsInfo system;
}
