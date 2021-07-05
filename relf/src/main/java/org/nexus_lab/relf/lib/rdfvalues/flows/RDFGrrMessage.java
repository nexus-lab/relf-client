package org.nexus_lab.relf.lib.rdfvalues.flows;

import static org.nexus_lab.relf.proto.GrrMessage.getDefaultInstance;

import androidx.annotation.NonNull;

import org.nexus_lab.relf.lib.exceptions.DecodeException;
import org.nexus_lab.relf.lib.rdfvalues.RDFDatetime;
import org.nexus_lab.relf.lib.rdfvalues.RDFRegistry;
import org.nexus_lab.relf.lib.rdfvalues.RDFURN;
import org.nexus_lab.relf.lib.rdfvalues.RDFValue;
import org.nexus_lab.relf.lib.rdfvalues.SessionID;
import org.nexus_lab.relf.lib.rdfvalues.structs.RDFProtoStruct;
import org.nexus_lab.relf.proto.GrrMessage;

import net.badata.protobuf.converter.annotation.ProtoClass;
import net.badata.protobuf.converter.annotation.ProtoField;
import net.badata.protobuf.converter.mapping.Proto2MapperImpl;
import net.badata.protobuf.converter.type.ByteStringConverterImpl;

import lombok.Getter;
import lombok.Setter;

/**
 * RDF wrapper of {@link GrrMessage}
 *
 * @author Ruipeng Zhang
 */
@ProtoClass(value = GrrMessage.class, mapper = Proto2MapperImpl.class)
public class RDFGrrMessage extends RDFProtoStruct<GrrMessage> {
    @Getter
    @Setter
    @ProtoField(converter = SessionID.Converter.class)
    private SessionID sessionId = new SessionID("W:0");
    @Getter
    @Setter
    @ProtoField
    private Long requestId = 0L;
    @Getter
    @Setter
    @ProtoField
    private Long responseId = 0L;
    @Getter
    @Setter
    @ProtoField
    private String name;
    @Getter
    @Setter
    @ProtoField(converter = ByteStringConverterImpl.class)
    private byte[] args;
    @Getter
    @Setter
    @ProtoField(converter = RDFURN.Converter.class)
    private RDFURN source;
    @Getter
    @Setter
    @ProtoField
    private GrrMessage.AuthorizationState authState = getDefaultInstance().getAuthState();
    @Getter
    @Setter
    @ProtoField
    private GrrMessage.Type type = getDefaultInstance().getType();
    @Getter
    @Setter
    @ProtoField
    private GrrMessage.Priority priority = getDefaultInstance().getPriority();
    @Getter
    @Setter
    @ProtoField
    private Integer ttl = getDefaultInstance().getTtl();
    @Getter
    @Setter
    @ProtoField
    private Boolean requireFastpoll = getDefaultInstance().getRequireFastpoll();
    @Getter
    @Setter
    @ProtoField
    private Float cpuLimit = getDefaultInstance().getCpuLimit();
    @Getter
    @Setter
    @ProtoField(converter = RDFDatetime.Converter.class)
    private RDFDatetime argsAge;
    @Getter
    @Setter
    @ProtoField
    private String argsRdfName;
    @Getter
    @Setter
    @ProtoField
    private Long taskId;
    @Getter
    @Setter
    @ProtoField
    private Integer taskTtl = getDefaultInstance().getTaskTtl();
    @Getter
    @Setter
    @ProtoField(converter = RDFURN.Converter.class)
    private RDFURN queue;
    @Getter
    @Setter
    @ProtoField(converter = RDFDatetime.Converter.class)
    private RDFDatetime eta;
    @Getter
    @Setter
    @ProtoField
    private String lastLease;
    @Getter
    @Setter
    @ProtoField
    private Long networkBytesLimit = getDefaultInstance().getNetworkBytesLimit();
    @Getter
    @Setter
    @ProtoField(converter = RDFDatetime.Converter.class)
    private RDFDatetime timestamp;

    /**
     * @return The RDF value embedded in the GRR message
     */
    public RDFValue getRDFValue() {
        if (getArgsRdfName() != null && getArgs() != null) {
            try {
                RDFValue value = RDFRegistry.create(getArgsRdfName());
                value.parse(getArgs());
                value.setAge(getArgsAge() == null ? new RDFDatetime(0) : getArgsAge());
                return value;
            } catch (DecodeException e) {
                throw new RuntimeException(
                        String.format("Cannot decode %s from message with arguments %s",
                                getArgsRdfName(), new String(getArgs())));
            }
        }
        return null;
    }

    /**
     * Set the embedded RDF value. The following fields will be set:
     * {@link #setArgs(byte[])}
     * {@link #setArgsAge(RDFDatetime)}
     * {@link #setArgsRdfName(String)}
     *
     * @param value the RDF value to be embedded
     */
    public void setRDFValue(@NonNull RDFValue value) {
        setArgs(value.serialize());
        setArgsAge(value.getAge());
        setArgsRdfName(RDFRegistry.getName(value.getClass()));
    }
}
