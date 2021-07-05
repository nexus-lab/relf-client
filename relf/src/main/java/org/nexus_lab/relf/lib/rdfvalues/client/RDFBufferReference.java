package org.nexus_lab.relf.lib.rdfvalues.client;

import org.nexus_lab.relf.lib.rdfvalues.paths.RDFPathSpec;
import org.nexus_lab.relf.lib.rdfvalues.structs.RDFProtoStruct;
import org.nexus_lab.relf.proto.BufferReference;

import net.badata.protobuf.converter.annotation.ProtoClass;
import net.badata.protobuf.converter.annotation.ProtoField;
import net.badata.protobuf.converter.mapping.Proto2MapperImpl;
import net.badata.protobuf.converter.type.ByteStringConverterImpl;

import lombok.Getter;
import lombok.Setter;

/**
 * RDF wrapper of {@link BufferReference}
 *
 * @author Ruipeng Zhang
 */
@ProtoClass(value = BufferReference.class, mapper = Proto2MapperImpl.class)
public class RDFBufferReference extends RDFProtoStruct<BufferReference> {
    @Getter
    @Setter
    @ProtoField
    private long offset;
    @Getter
    @Setter
    @ProtoField
    private long length;
    @Getter
    @Setter
    @ProtoField
    private String callback;
    @Getter
    @Setter
    @ProtoField(converter = ByteStringConverterImpl.class)
    private byte[] data;
    @Getter
    @Setter
    @ProtoField
    private RDFPathSpec pathspec;
}
