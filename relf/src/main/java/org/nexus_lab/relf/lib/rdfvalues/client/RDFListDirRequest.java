package org.nexus_lab.relf.lib.rdfvalues.client;

import org.nexus_lab.relf.lib.rdfvalues.paths.RDFPathSpec;
import org.nexus_lab.relf.lib.rdfvalues.structs.RDFProtoStruct;
import org.nexus_lab.relf.proto.Iterator;
import org.nexus_lab.relf.proto.ListDirRequest;

import net.badata.protobuf.converter.annotation.ProtoClass;
import net.badata.protobuf.converter.annotation.ProtoField;
import net.badata.protobuf.converter.mapping.Proto2MapperImpl;

import lombok.Getter;
import lombok.Setter;

/**
 * RDF wrapper of {@link ListDirRequest}
 *
 * @author Ruipeng Zhang
 */
@ProtoClass(value = ListDirRequest.class, mapper = Proto2MapperImpl.class)
public class RDFListDirRequest extends RDFProtoStruct<ListDirRequest> {
    @Getter
    @Setter
    @ProtoField
    private RDFPathSpec pathspec;
    @Getter
    @Setter
    @ProtoField
    private Iterator iterator;
}
