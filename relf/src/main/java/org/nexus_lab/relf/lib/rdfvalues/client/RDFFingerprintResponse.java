package org.nexus_lab.relf.lib.rdfvalues.client;

import org.nexus_lab.relf.lib.rdfvalues.crypto.RDFHash;
import org.nexus_lab.relf.lib.rdfvalues.paths.RDFPathSpec;
import org.nexus_lab.relf.lib.rdfvalues.protodict.RDFDict;
import org.nexus_lab.relf.lib.rdfvalues.structs.RDFProtoStruct;
import org.nexus_lab.relf.proto.FingerprintResponse;
import org.nexus_lab.relf.proto.FingerprintTuple;

import net.badata.protobuf.converter.annotation.ProtoClass;
import net.badata.protobuf.converter.annotation.ProtoField;
import net.badata.protobuf.converter.mapping.Proto2MapperImpl;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * RDF wrapper of {@link FingerprintResponse}
 *
 * @author Ruipeng Zhang
 */
@ProtoClass(value = FingerprintResponse.class, mapper = Proto2MapperImpl.class)
public class RDFFingerprintResponse extends RDFProtoStruct<FingerprintResponse> {
    @Getter
    @Setter
    @ProtoField
    private FingerprintTuple.Type matchingTypes;
    @Getter
    @Setter
    @ProtoField
    private List<RDFDict> results;
    @Getter
    @Setter
    @ProtoField
    private RDFPathSpec pathspec;
    @Getter
    @Setter
    @ProtoField
    private RDFHash hash;
    @Getter
    @Setter
    @ProtoField
    private Long bytesRead;
}
