package org.nexus_lab.relf.lib.rdfvalues.client;

import org.nexus_lab.relf.lib.rdfvalues.structs.RDFProtoStruct;
import org.nexus_lab.relf.proto.PathSpec;
import org.nexus_lab.relf.proto.StatFSRequest;

import net.badata.protobuf.converter.annotation.ProtoClass;
import net.badata.protobuf.converter.annotation.ProtoField;
import net.badata.protobuf.converter.mapping.Proto2MapperImpl;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * RDF wrapper of {@link StatFSRequest}
 *
 * @author Ruipeng Zhang
 */
@ProtoClass(value = StatFSRequest.class, mapper = Proto2MapperImpl.class)
public class RDFStatFSRequest extends RDFProtoStruct<StatFSRequest> {
    @Getter
    @Setter
    @ProtoField
    private List<String> pathList;
    @Getter
    @Setter
    @ProtoField
    private PathSpec.PathType pathtype;
}
