package org.nexus_lab.relf.lib.rdfvalues.crypto;

import org.nexus_lab.relf.lib.rdfvalues.structs.RDFProtoStruct;
import org.nexus_lab.relf.proto.Certificate;

import net.badata.protobuf.converter.annotation.ProtoClass;
import net.badata.protobuf.converter.annotation.ProtoField;
import net.badata.protobuf.converter.mapping.Proto2MapperImpl;
import net.badata.protobuf.converter.type.ByteStringConverterImpl;

import lombok.Getter;
import lombok.Setter;

/**
 * RDF wrapper of {@link Certificate}
 *
 * @author Ruipeng Zhang
 */
@ProtoClass(value = Certificate.class, mapper = Proto2MapperImpl.class)
public class RDFCertificate extends RDFProtoStruct<Certificate> {
    @Getter
    @Setter
    @ProtoField(converter = ByteStringConverterImpl.class)
    private byte[] pem;
    @Getter
    @Setter
    @ProtoField
    private String cn;
    @Getter
    @Setter
    @ProtoField
    private Certificate.Type type;
}
