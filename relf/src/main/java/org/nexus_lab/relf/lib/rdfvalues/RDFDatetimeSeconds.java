package org.nexus_lab.relf.lib.rdfvalues;

import net.badata.protobuf.converter.type.TypeConverter;

import java.util.Date;

import lombok.NoArgsConstructor;

/**
 * @author Ruipeng Zhang
 */
@NoArgsConstructor
public class RDFDatetimeSeconds extends RDFDatetime {
    public RDFDatetimeSeconds(long value) {
        super(value);
    }

    /**
     * Create a {@link RDFDatetimeSeconds} from total seconds after epoch
     *
     * @param value total seconds from the epoch time
     * @return a new {@link RDFDatetimeSeconds}
     */
    public static RDFDatetimeSeconds fromSecondsFromEpoch(long value) {
        return new RDFDatetimeSeconds(value);
    }

    /**
     * @return a {@link RDFDatetimeSeconds} of the current time
     */
    public static RDFDatetimeSeconds now() {
        return new RDFDatetimeSeconds(System.currentTimeMillis() / 1000);
    }

    @Override
    public long asSecondsFromEpoch() {
        return getValue();
    }

    @Override
    public long asMicrosecondsFromEpoch() {
        return getValue() * 1000000;
    }

    @Override
    public Date asDate() {
        return new Date(getValue() * 1000);
    }

    /**
     * {@link TypeConverter} for inter-conversion between {@link Long} and {@link
     * RDFDatetimeSeconds}
     * during protobuf conversion.
     */
    public static class Converter implements TypeConverter<RDFDatetimeSeconds, Long> {
        @Override
        public RDFDatetimeSeconds toDomainValue(Object instance) {
            if (instance instanceof Long) {
                return new RDFDatetimeSeconds((Long) instance);
            }
            throw new RuntimeException("Failed to convert protobuf values to rdf values.");
        }

        @Override
        public Long toProtobufValue(Object instance) {
            if (instance instanceof RDFDatetimeSeconds) {
                return ((RDFDatetimeSeconds) instance).getValue();
            }
            throw new RuntimeException("Failed to convert rdf values to protobuf values.");
        }
    }
}
