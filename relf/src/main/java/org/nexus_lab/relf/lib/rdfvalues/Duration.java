package org.nexus_lab.relf.lib.rdfvalues;

import net.badata.protobuf.converter.type.TypeConverter;

import java.util.Locale;

import lombok.NoArgsConstructor;

/**
 * @author Ruipeng Zhang
 */
@NoArgsConstructor
public class Duration extends RDFInteger {
    /**
     * @param value time in seconds
     */
    public Duration(long value) {
        super(value);
    }

    public Duration(String value) {
        parseFromHumanReadable(value);
    }

    /**
     * Create {@link Duration} from second value
     *
     * @param seconds duration in seconds
     * @return a new {@link Duration} instance
     */
    public static Duration fromSeconds(long seconds) {
        return new Duration(seconds);
    }

    /**
     * Parse duration from descriptive text
     *
     * @param time duration text
     */
    public void parseFromHumanReadable(String time) {
        if (time == null) {
            return;
        }
        int multiplier = 1;
        switch (time.charAt(time.length() - 1)) {
            case 'w':
                multiplier = 7 * 24 * 60 * 60;
                break;
            case 'd':
                multiplier = 24 * 60 * 60;
                break;
            case 'h':
                multiplier = 60 * 60;
                break;
            case 'm':
                multiplier = 60;
                break;
            case 's':
                break;
            default:
                if (Character.isDigit(time.charAt(time.length() - 1))) {
                    setValue(Long.valueOf(time));
                    return;
                }
        }
        setValue(Long.valueOf(time.substring(0, time.length() - 1)) * multiplier);
    }

    /**
     * @return duration in seconds
     */
    public long getSeconds() {
        return getValue();
    }

    /**
     * @return duration in microseconds
     */
    public long getMicroseconds() {
        return getValue() * 1000000;
    }

    /**
     * Return the {@link RDFDatetime} from the given start time plus this duration
     *
     * @param start start time
     * @return expiry time
     */
    public RDFDatetime expiry(RDFDatetime start) {
        RDFDatetime baseTime;
        if (start == null) {
            baseTime = RDFDatetime.now();
        } else {
            baseTime = (RDFDatetime) start.duplicate();
        }
        baseTime = RDFDatetime.fromSecondsFromEpoch(baseTime.asSecondsFromEpoch() + getValue());
        return baseTime;
    }

    @Override
    public Duration parse(byte[] data) {
        parseFromHumanReadable(new String(data));
        return this;
    }

    @Override
    public String stringify() {
        char unit = 's';
        long time = getValue();
        if (time > 7 * 24 * 60 * 60) {
            unit = 'w';
            time /= 7 * 24 * 60 * 60;
        } else if (time > 24 * 60 * 60) {
            unit = 'd';
            time /= 24 * 60 * 60;
        } else if (time > 60 * 60) {
            unit = 'h';
            time /= 60 * 60;
        } else if (time > 60) {
            unit = 'm';
            time /= 60;
        }
        return String.format(Locale.getDefault(), "%d%c", time, unit);
    }

    /**
     * {@link TypeConverter} for inter-conversion between {@link Long} and {@link Duration}
     * during protobuf conversion.
     */
    public static class Converter implements TypeConverter<Duration, Long> {
        @Override
        public Duration toDomainValue(Object instance) {
            if (instance instanceof Long) {
                return new Duration((Long) instance);
            }
            throw new RuntimeException("Failed to convert protobuf values to rdf values.");
        }

        @Override
        public Long toProtobufValue(Object instance) {
            if (instance instanceof Duration) {
                return ((Duration) instance).getValue();
            }
            throw new RuntimeException("Failed to convert rdf values to protobuf values.");
        }
    }
}
