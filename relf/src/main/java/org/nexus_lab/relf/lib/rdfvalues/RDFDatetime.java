package org.nexus_lab.relf.lib.rdfvalues;

import net.badata.protobuf.converter.type.TypeConverter;

import java.util.Date;
import java.util.Objects;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author Ruipeng Zhang
 */
@NoArgsConstructor
public class RDFDatetime implements RDFValue<Long> {
    @Getter
    @Setter
    private Long value = 0L;
    private long age;

    /**
     * @param value time in microseconds
     */
    public RDFDatetime(long value) {
        setValue(value);
    }

    /**
     * Create a {@link RDFDatetime} from total seconds after epoch
     *
     * @param value total seconds from the epoch time
     * @return a new {@link RDFDatetime}
     */
    public static RDFDatetime fromSecondsFromEpoch(long value) {
        return new RDFDatetime(value * 1000000);
    }

    /**
     * @return a {@link RDFDatetime} of the current time
     */
    public static RDFDatetime now() {
        return new RDFDatetime(System.currentTimeMillis() * 1000);
    }

    /**
     * @return total seconds from the epoch time
     */
    public long asSecondsFromEpoch() {
        return getValue() / 1000000;
    }

    /**
     * @return total microseconds from the epoch time
     */
    public long asMicrosecondsFromEpoch() {
        return getValue();
    }

    /**
     * Convert to {@link Date}
     *
     * @return a {@link Date} instance of the {@link RDFDatetime}
     */
    public Date asDate() {
        return new Date(getValue() / 1000);
    }

    @Override
    public RDFDatetime getAge() {
        return new RDFDatetime(age);
    }

    @Override
    public void setAge(RDFDatetime age) {
        this.age = age.age;
    }

    @Override
    public RDFDatetime parse(byte[] value) {
        return parse(new String(value));
    }

    @Override
    public RDFDatetime parse(String value) {
        setValue(Long.valueOf(value));
        return this;
    }

    @Override
    public byte[] serialize() {
        return stringify().getBytes();
    }

    @Override
    public String stringify() {
        return String.valueOf(getValue());
    }

    @Override
    public boolean equals(Object o) {
        if (!getClass().isInstance(o)) {
            return false;
        }
        RDFDatetime other = (RDFDatetime) o;
        return Objects.equals(other.getValue(), getValue()) && other.age == age;
    }

    @Override
    public int hashCode() {
        int prime = 59;
        int result = prime + (getValue() == null ? 43 : getValue().hashCode());
        result = result * prime + Long.valueOf(age).hashCode();
        return result;
    }

    /**
     * {@link TypeConverter} for inter-conversion between {@link Long} and {@link RDFDatetime}
     * during protobuf conversion.
     */
    public static class Converter implements TypeConverter<RDFDatetime, Long> {
        @Override
        public RDFDatetime toDomainValue(Object instance) {
            if (instance instanceof Long) {
                return new RDFDatetime((Long) instance);
            }
            throw new RuntimeException("Failed to convert protobuf values to rdf values.");
        }

        @Override
        public Long toProtobufValue(Object instance) {
            if (instance instanceof RDFDatetime) {
                return ((RDFDatetime) instance).getValue();
            }
            throw new RuntimeException("Failed to convert rdf values to protobuf values.");
        }
    }
}
