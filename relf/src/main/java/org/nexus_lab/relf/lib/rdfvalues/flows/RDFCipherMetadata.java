package org.nexus_lab.relf.lib.rdfvalues.flows;

import org.nexus_lab.relf.lib.rdfvalues.RDFURN;
import org.nexus_lab.relf.lib.rdfvalues.structs.RDFProtoStruct;
import org.nexus_lab.relf.proto.CipherMetadata;

import net.badata.protobuf.converter.annotation.ProtoClass;
import net.badata.protobuf.converter.annotation.ProtoField;
import net.badata.protobuf.converter.mapping.Proto2MapperImpl;
import net.badata.protobuf.converter.type.ByteStringConverterImpl;

import lombok.Getter;
import lombok.Setter;

/**
 * RDF wrapper of {@link CipherMetadata}
 *
 * @author Ruipeng Zhang
 */
@ProtoClass(value = CipherMetadata.class, mapper = Proto2MapperImpl.class)
public class RDFCipherMetadata extends RDFProtoStruct<CipherMetadata> {
    @Getter
    @Setter
    @ProtoField(converter = RDFURN.Converter.class)
    private RDFURN source;
    @Getter
    @Setter
    @ProtoField(converter = ByteStringConverterImpl.class)
    private byte[] signature;
}
