package org.nexus_lab.relf.lib.rdfvalues.android;

import org.nexus_lab.relf.lib.rdfvalues.structs.RDFProtoStruct;
import org.nexus_lab.relf.proto.AndroidSmsMmsBody;

import net.badata.protobuf.converter.annotation.ProtoClass;
import net.badata.protobuf.converter.annotation.ProtoField;
import net.badata.protobuf.converter.mapping.Proto2MapperImpl;
import net.badata.protobuf.converter.type.ByteStringConverterImpl;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Ruipeng Zhang
 */
@ProtoClass(value = AndroidSmsMmsBody.class, mapper = Proto2MapperImpl.class)
public class RDFAndroidSmsMmsBody extends RDFProtoStruct<AndroidSmsMmsBody> {
    @Getter
    @Setter
    @ProtoField
    private String contentType;
    @Getter
    @Setter
    @ProtoField(converter = ByteStringConverterImpl.class)
    private byte[] data;
}
