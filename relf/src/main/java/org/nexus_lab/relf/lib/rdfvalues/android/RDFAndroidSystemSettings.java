package org.nexus_lab.relf.lib.rdfvalues.android;

import org.nexus_lab.relf.lib.rdfvalues.protodict.RDFDict;
import org.nexus_lab.relf.lib.rdfvalues.structs.RDFProtoStruct;
import org.nexus_lab.relf.proto.AndroidSystemSettings;

import net.badata.protobuf.converter.annotation.ProtoClass;
import net.badata.protobuf.converter.annotation.ProtoField;
import net.badata.protobuf.converter.mapping.Proto2MapperImpl;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Ruipeng Zhang
 */
@ProtoClass(value = AndroidSystemSettings.class, mapper = Proto2MapperImpl.class)
public class RDFAndroidSystemSettings extends RDFProtoStruct<AndroidSystemSettings> {
    @Getter
    @Setter
    @ProtoField
    private RDFDict system;
    @Getter
    @Setter
    @ProtoField
    private RDFDict global;
    @Getter
    @Setter
    @ProtoField
    private RDFDict secure;
}
