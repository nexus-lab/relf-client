package org.nexus_lab.relf.lib.rdfvalues.protodict;

import org.nexus_lab.relf.lib.rdfvalues.RDFDatetime;
import org.nexus_lab.relf.lib.rdfvalues.structs.RDFProtoStruct;
import org.nexus_lab.relf.proto.EmbeddedRDFValue;

import net.badata.protobuf.converter.annotation.ProtoClass;
import net.badata.protobuf.converter.annotation.ProtoField;
import net.badata.protobuf.converter.mapping.Proto2MapperImpl;
import net.badata.protobuf.converter.type.ByteStringConverterImpl;

import lombok.Getter;
import lombok.Setter;

/**
 * RDF wrapper of {@link EmbeddedRDFValue}
 *
 * @author Ruipeng Zhang
 */
@ProtoClass(value = EmbeddedRDFValue.class, mapper = Proto2MapperImpl.class)
public class RDFEmbeddedRDFValue extends RDFProtoStruct<EmbeddedRDFValue> {
    @Getter
    @Setter
    @ProtoField(converter = RDFDatetime.Converter.class)
    private RDFDatetime embeddedAge;
    @Getter
    @Setter
    @ProtoField
    private String name;
    @Getter
    @Setter
    @ProtoField(converter = ByteStringConverterImpl.class)
    private byte[] data;
}
