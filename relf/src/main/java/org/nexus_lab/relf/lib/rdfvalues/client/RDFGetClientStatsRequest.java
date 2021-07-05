package org.nexus_lab.relf.lib.rdfvalues.client;

import org.nexus_lab.relf.lib.rdfvalues.RDFDatetime;
import org.nexus_lab.relf.lib.rdfvalues.structs.RDFProtoStruct;
import org.nexus_lab.relf.proto.GetClientStatsRequest;

import net.badata.protobuf.converter.annotation.ProtoClass;
import net.badata.protobuf.converter.annotation.ProtoField;
import net.badata.protobuf.converter.inspection.DefaultValue;
import net.badata.protobuf.converter.mapping.Proto2MapperImpl;

import lombok.Getter;
import lombok.Setter;

/**
 * RDF wrapper of {@link GetClientStatsRequest}
 *
 * @author Ruipeng Zhang
 */
@ProtoClass(value = GetClientStatsRequest.class, mapper = Proto2MapperImpl.class)
public class RDFGetClientStatsRequest extends RDFProtoStruct<GetClientStatsRequest> {
    @Getter
    @Setter
    @ProtoField(converter = RDFDatetime.Converter.class, defaultValue = StartTimeDefaultValueImpl.class)
    private RDFDatetime startTime = new RDFDatetime(0);
    @Getter
    @Setter
    @ProtoField(converter = RDFDatetime.Converter.class, defaultValue = EndTimeDefaultValueImpl.class)
    private RDFDatetime endTime = new RDFDatetime(Long.MAX_VALUE);

    /**
     * Default value for {@link RDFGetClientStatsRequest#startTime}.
     */
    public static class StartTimeDefaultValueImpl implements DefaultValue {

        @Override
        public Object generateValue(Class<?> type) {
            return new RDFDatetime(0);
        }
    }

    /**
     * Default value for {@link RDFGetClientStatsRequest#endTime}.
     */
    public static class EndTimeDefaultValueImpl implements DefaultValue {

        @Override
        public Object generateValue(Class<?> type) {
            return new RDFDatetime(Long.MAX_VALUE);
        }
    }
}
