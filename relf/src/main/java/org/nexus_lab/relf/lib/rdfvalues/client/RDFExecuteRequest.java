package org.nexus_lab.relf.lib.rdfvalues.client;

import org.nexus_lab.relf.lib.rdfvalues.structs.RDFProtoStruct;
import org.nexus_lab.relf.proto.ExecuteRequest;

import net.badata.protobuf.converter.annotation.ProtoClass;
import net.badata.protobuf.converter.annotation.ProtoField;
import net.badata.protobuf.converter.mapping.Proto2MapperImpl;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * RDF wrapper of {@link ExecuteRequest}
 *
 * @author Ruipeng Zhang
 */
@ProtoClass(value = ExecuteRequest.class, mapper = Proto2MapperImpl.class)
public class RDFExecuteRequest extends RDFProtoStruct<ExecuteRequest> {
    @Getter
    @Setter
    @ProtoField
    private String cmd;
    @Getter
    @Setter
    @ProtoField
    private List<String> args;
    @Getter
    @Setter
    @ProtoField
    private Integer timeLimit;
}
