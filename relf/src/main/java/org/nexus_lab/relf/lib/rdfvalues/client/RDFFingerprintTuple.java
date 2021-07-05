package org.nexus_lab.relf.lib.rdfvalues.client;

import org.nexus_lab.relf.lib.rdfvalues.structs.RDFProtoStruct;
import org.nexus_lab.relf.proto.FingerprintTuple;

import net.badata.protobuf.converter.annotation.ProtoClass;
import net.badata.protobuf.converter.annotation.ProtoField;
import net.badata.protobuf.converter.mapping.Proto2MapperImpl;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * RDF wrapper of {@link FingerprintTuple}
 *
 * @author Ruipeng Zhang
 */
@ProtoClass(value = FingerprintTuple.class, mapper = Proto2MapperImpl.class)
public class RDFFingerprintTuple extends RDFProtoStruct<FingerprintTuple> {
    @Getter
    @Setter
    @ProtoField
    private FingerprintTuple.Type fpType;
    @Getter
    @Setter
    @ProtoField
    private List<FingerprintTuple.HashType> hashers;
}
