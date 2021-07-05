package org.nexus_lab.relf.lib.rdfvalues.android;

import org.nexus_lab.relf.lib.rdfvalues.structs.RDFProtoStruct;
import org.nexus_lab.relf.proto.AndroidSensorDataRequest;
import org.nexus_lab.relf.proto.AndroidSensorType;

import net.badata.protobuf.converter.annotation.ProtoClass;
import net.badata.protobuf.converter.annotation.ProtoField;
import net.badata.protobuf.converter.inspection.DefaultValue;
import net.badata.protobuf.converter.mapping.Proto2MapperImpl;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Ruipeng Zhang
 */
@ProtoClass(value = AndroidSensorDataRequest.class, mapper = Proto2MapperImpl.class)
public class RDFAndroidSensorDataRequest extends RDFProtoStruct<AndroidSensorDataRequest> {
    @Getter
    @Setter
    @ProtoField(defaultValue = TypeDefaultValueImpl.class)
    private AndroidSensorType type;
    @Getter
    @Setter
    @ProtoField(defaultValue = SamplingRateDefaultValueImpl.class)
    private Integer samplingRate;
    @Getter
    @Setter
    @ProtoField(defaultValue = DurationDefaultValueImpl.class)
    private Integer duration;

    /**
     * Default value for {@link RDFAndroidSensorDataRequest#type}.
     */
    public static class TypeDefaultValueImpl implements DefaultValue {

        @Override
        public Object generateValue(Class<?> type) {
            return AndroidSensorType.ACCELEROMETER;
        }
    }

    /**
     * Default value for {@link RDFAndroidSensorDataRequest#samplingRate}.
     */
    public static class SamplingRateDefaultValueImpl implements DefaultValue {

        @Override
        public Object generateValue(Class<?> type) {
            return 50;
        }
    }

    /**
     * Default value for {@link RDFAndroidSensorDataRequest#duration}.
     */
    public static class DurationDefaultValueImpl implements DefaultValue {

        @Override
        public Object generateValue(Class<?> type) {
            return 1;
        }
    }
}
