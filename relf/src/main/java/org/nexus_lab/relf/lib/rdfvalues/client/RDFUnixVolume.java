package org.nexus_lab.relf.lib.rdfvalues.client;

import org.nexus_lab.relf.lib.rdfvalues.structs.RDFProtoStruct;
import org.nexus_lab.relf.proto.UnixVolume;

import net.badata.protobuf.converter.annotation.ProtoClass;
import net.badata.protobuf.converter.annotation.ProtoField;
import net.badata.protobuf.converter.mapping.Proto2MapperImpl;

import lombok.Getter;
import lombok.Setter;

/**
 * RDF wrapper of {@link UnixVolume}
 *
 * @author Ruipeng Zhang
 */
@ProtoClass(value = UnixVolume.class, mapper = Proto2MapperImpl.class)
public class RDFUnixVolume extends RDFProtoStruct<UnixVolume> {
    @Getter
    @Setter
    @ProtoField
    private String mountPoint;
    @Getter
    @Setter
    @ProtoField
    private String options;
}
