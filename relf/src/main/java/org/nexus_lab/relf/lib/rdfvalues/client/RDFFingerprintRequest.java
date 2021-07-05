package org.nexus_lab.relf.lib.rdfvalues.client;

import org.nexus_lab.relf.lib.rdfvalues.paths.RDFPathSpec;
import org.nexus_lab.relf.lib.rdfvalues.structs.RDFProtoStruct;
import org.nexus_lab.relf.proto.FingerprintRequest;

import net.badata.protobuf.converter.annotation.ProtoClass;
import net.badata.protobuf.converter.annotation.ProtoField;
import net.badata.protobuf.converter.mapping.Proto2MapperImpl;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * RDF wrapper of {@link FingerprintRequest}
 *
 * @author Ruipeng Zhang
 */
@ProtoClass(value = FingerprintRequest.class, mapper = Proto2MapperImpl.class)
public class RDFFingerprintRequest extends RDFProtoStruct<FingerprintRequest> {
    @Getter
    @Setter
    @ProtoField
    private RDFPathSpec pathspec;
    @Getter
    @Setter
    @ProtoField
    private List<RDFFingerprintTuple> tuples;
    @Getter
    @Setter
    @ProtoField
    private Long maxFilesize;
}
