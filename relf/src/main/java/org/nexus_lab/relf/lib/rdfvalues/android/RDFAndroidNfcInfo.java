package org.nexus_lab.relf.lib.rdfvalues.android;

import org.nexus_lab.relf.lib.rdfvalues.structs.RDFProtoStruct;
import org.nexus_lab.relf.proto.AndroidNfcInfo;

import net.badata.protobuf.converter.annotation.ProtoClass;
import net.badata.protobuf.converter.annotation.ProtoField;
import net.badata.protobuf.converter.mapping.Proto2MapperImpl;

import lombok.Getter;
import lombok.Setter;

/**
 * RDF wrapper of {@link AndroidNfcInfo}
 *
 * @author Ruipeng Zhang
 */
@ProtoClass(value = AndroidNfcInfo.class, mapper = Proto2MapperImpl.class)
public class RDFAndroidNfcInfo extends RDFProtoStruct<AndroidNfcInfo> {
    @Getter
    @Setter
    @ProtoField
    private Boolean isEnabled;
    @Getter
    @Setter
    @ProtoField
    private Boolean isNdefPushEnabled;
}
