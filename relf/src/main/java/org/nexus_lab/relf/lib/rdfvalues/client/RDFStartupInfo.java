package org.nexus_lab.relf.lib.rdfvalues.client;

import org.nexus_lab.relf.lib.rdfvalues.structs.RDFProtoStruct;
import org.nexus_lab.relf.proto.StartupInfo;

import net.badata.protobuf.converter.annotation.ProtoClass;
import net.badata.protobuf.converter.annotation.ProtoField;
import net.badata.protobuf.converter.mapping.Proto2MapperImpl;

import lombok.Getter;
import lombok.Setter;

/**
 * RDF wrapper of {@link StartupInfo}
 *
 * @author Ruipeng Zhang
 */
@ProtoClass(value = StartupInfo.class, mapper = Proto2MapperImpl.class)
public class RDFStartupInfo extends RDFProtoStruct<StartupInfo> {
    @Getter
    @Setter
    @ProtoField
    private RDFClientInformation clientInfo;
    @Getter
    @Setter
    @ProtoField
    private Long bootTime;
}
