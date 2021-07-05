package org.nexus_lab.relf.lib.rdfvalues.client;

import static org.nexus_lab.relf.proto.NetworkConnection.State.UNKNOWN;
import static org.nexus_lab.relf.proto.NetworkConnection.Type.UNKNOWN_SOCKET;

import org.nexus_lab.relf.lib.rdfvalues.structs.RDFProtoStruct;
import org.nexus_lab.relf.proto.NetworkConnection;
import org.nexus_lab.relf.proto.NetworkEndpoint;

import net.badata.protobuf.converter.annotation.ProtoClass;
import net.badata.protobuf.converter.annotation.ProtoField;
import net.badata.protobuf.converter.inspection.DefaultValue;
import net.badata.protobuf.converter.mapping.Proto2MapperImpl;

import lombok.Getter;
import lombok.Setter;

/**
 * RDF wrapper of {@link NetworkConnection}
 *
 * @author Ruipeng Zhang
 */
@ProtoClass(value = NetworkConnection.class, mapper = Proto2MapperImpl.class)
public class RDFNetworkConnection extends RDFProtoStruct<NetworkConnection> {
    @Getter
    @Setter
    @ProtoField
    private NetworkConnection.Family family;
    @Getter
    @Setter
    @ProtoField(defaultValue = TypeDefaultValueImpl.class)
    private NetworkConnection.Type type = UNKNOWN_SOCKET;
    @Getter
    @Setter
    @ProtoField
    private NetworkEndpoint localAddress;
    @Getter
    @Setter
    @ProtoField
    private NetworkEndpoint remoteAddress;
    @Getter
    @Setter
    @ProtoField(defaultValue = StateDefaultValueImpl.class)
    private NetworkConnection.State state = UNKNOWN;
    @Getter
    @Setter
    @ProtoField
    private Integer pid;
    @Getter
    @Setter
    @ProtoField
    private Long ctime;
    @Getter
    @Setter
    @ProtoField
    private String processName;

    /**
     * Default value generator for {@link RDFNetworkConnection#type}
     */
    public static class TypeDefaultValueImpl implements DefaultValue {
        @Override
        public Object generateValue(Class<?> type) {
            return UNKNOWN_SOCKET;
        }
    }

    /**
     * Default value generator for {@link RDFNetworkConnection#state}
     */
    public static class StateDefaultValueImpl implements DefaultValue {
        @Override
        public Object generateValue(Class<?> type) {
            return UNKNOWN;
        }
    }
}
