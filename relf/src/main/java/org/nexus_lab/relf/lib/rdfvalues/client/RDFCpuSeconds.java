package org.nexus_lab.relf.lib.rdfvalues.client;

import org.nexus_lab.relf.lib.rdfvalues.structs.RDFProtoStruct;
import org.nexus_lab.relf.proto.CpuSeconds;

import net.badata.protobuf.converter.annotation.ProtoClass;
import net.badata.protobuf.converter.annotation.ProtoField;
import net.badata.protobuf.converter.mapping.Proto2MapperImpl;

import lombok.Getter;
import lombok.Setter;

/**
 * RDF wrapper of {@link CpuSeconds}
 *
 * @author Ruipeng Zhang
 */
@ProtoClass(value = CpuSeconds.class, mapper = Proto2MapperImpl.class)
public class RDFCpuSeconds extends RDFProtoStruct<CpuSeconds> {
    @Getter
    @Setter
    @ProtoField
    private Float userCpuTime;
    @Getter
    @Setter
    @ProtoField
    private Float systemCpuTime;
}
