package org.nexus_lab.relf.lib.rdfvalues.client;

import org.nexus_lab.relf.lib.rdfvalues.structs.RDFProtoStruct;
import org.nexus_lab.relf.proto.ExecuteResponse;

import net.badata.protobuf.converter.annotation.ProtoClass;
import net.badata.protobuf.converter.annotation.ProtoField;
import net.badata.protobuf.converter.mapping.Proto2MapperImpl;
import net.badata.protobuf.converter.type.ByteStringConverterImpl;

import lombok.Getter;
import lombok.Setter;

/**
 * RDF wrapper of {@link ExecuteResponse}
 *
 * @author Ruipeng Zhang
 */
@ProtoClass(value = ExecuteResponse.class, mapper = Proto2MapperImpl.class)
public class RDFExecuteResponse extends RDFProtoStruct<ExecuteResponse> {
    @Getter
    @Setter
    @ProtoField
    private RDFExecuteRequest request;
    @Getter
    @Setter
    @ProtoField
    private Integer exitStatus;
    @Getter
    @Setter
    @ProtoField(converter = ByteStringConverterImpl.class)
    private byte[] stdout;
    @Getter
    @Setter
    @ProtoField(converter = ByteStringConverterImpl.class)
    private byte[] stderr;
    @Getter
    @Setter
    @ProtoField
    private Integer timeUsed;
}
