package org.nexus_lab.relf.lib.rdfvalues.standard;

import org.nexus_lab.relf.lib.rdfvalues.structs.RDFProtoStruct;
import org.nexus_lab.relf.proto.AuthenticodeSignedData;

import net.badata.protobuf.converter.annotation.ProtoClass;
import net.badata.protobuf.converter.annotation.ProtoField;
import net.badata.protobuf.converter.mapping.Proto2MapperImpl;
import net.badata.protobuf.converter.type.ByteStringConverterImpl;

import lombok.Getter;
import lombok.Setter;

/**
 * RDF wrapper of {@link AuthenticodeSignedData}
 *
 * @author Ruipeng Zhang
 */
@ProtoClass(value = AuthenticodeSignedData.class, mapper = Proto2MapperImpl.class)
public class RDFAuthenticodeSignedData extends RDFProtoStruct<AuthenticodeSignedData> {
    @Getter
    @Setter
    @ProtoField
    private Long revision;
    @Getter
    @Setter
    @ProtoField
    private Long certType;
    @Getter
    @Setter
    @ProtoField(converter = ByteStringConverterImpl.class)
    private byte[] certificate;
}
