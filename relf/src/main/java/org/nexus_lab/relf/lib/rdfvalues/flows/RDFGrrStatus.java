package org.nexus_lab.relf.lib.rdfvalues.flows;

import org.nexus_lab.relf.lib.rdfvalues.SessionID;
import org.nexus_lab.relf.lib.rdfvalues.client.RDFCpuSeconds;
import org.nexus_lab.relf.lib.rdfvalues.structs.RDFProtoStruct;
import org.nexus_lab.relf.proto.GrrStatus;

import net.badata.protobuf.converter.annotation.ProtoClass;
import net.badata.protobuf.converter.annotation.ProtoField;
import net.badata.protobuf.converter.mapping.Proto2MapperImpl;

import lombok.Getter;
import lombok.Setter;

/**
 * RDF wrapper of {@link GrrStatus}
 *
 * @author Ruipeng Zhang
 */
@ProtoClass(value = GrrStatus.class, mapper = Proto2MapperImpl.class)
public class RDFGrrStatus extends RDFProtoStruct<GrrStatus> {
    @Getter
    @Setter
    @ProtoField
    private GrrStatus.ReturnedStatus status;
    @Getter
    @Setter
    @ProtoField
    private String errorMessage;
    @Getter
    @Setter
    @ProtoField
    private String backtrace;
    @Getter
    @Setter
    @ProtoField
    private RDFCpuSeconds cpuTimeUsed;
    @Getter
    @Setter
    @ProtoField(converter = SessionID.Converter.class)
    private SessionID childSessionId;
    @Getter
    @Setter
    @ProtoField
    private Long networkBytesSent;
}
