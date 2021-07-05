package org.nexus_lab.relf.lib.rdfvalues;

import org.nexus_lab.relf.lib.exceptions.DecodeException;

import net.badata.protobuf.converter.type.TypeConverter;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.NoArgsConstructor;

/**
 * @author Ruipeng Zhang
 */
@NoArgsConstructor
public class ByteSize extends RDFInteger {
    public ByteSize(long value) {
        super(value);
    }

    private void parseFromHumanReadable(String value) throws DecodeException {
        Pattern r = Pattern.compile("^([0-9.]+)([kmgi]*)b?$");
        Matcher m = r.matcher(value.toLowerCase());
        if (m.find()) {
            double multiplier = 1;
            String unit = m.group(2);
            double newValue = Double.parseDouble(m.group(1));
            if (unit.equals("k")) {
                multiplier = 1000;
            } else if (unit.equals("m")) {
                multiplier = Math.pow(1000, 2);
            } else if (unit.equals("g")) {
                multiplier = Math.pow(1000, 3);
            } else if (unit.equals("ki")) {
                multiplier = 1024;
            } else if (unit.equals("mi")) {
                multiplier = Math.pow(1024, 2);
            } else if (unit.equals("gi")) {
                multiplier = Math.pow(1024, 3);
            } else if (!unit.equals("")) {
                throw new DecodeException(String.format("Invalid multiplier %s", unit));
            }
            setValue((long) (newValue * multiplier));
        } else {
            throw new DecodeException(
                    String.format("Unknown specification for ByteSize %s.", value));
        }
    }

    @Override
    public ByteSize parse(byte[] value) {
        parse(new String(value));
        return this;
    }

    @Override
    public ByteSize parse(String value) {
        try {
            parseFromHumanReadable(value);
        } catch (DecodeException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    @Override
    public byte[] serialize() {
        return String.valueOf(getValue()).getBytes();
    }

    @Override
    public String stringify() {
        String unit = "b";
        double value = getValue();
        if (getValue() > Math.pow(1024, 3)) {
            unit = "Gb";
            value = getValue() / Math.pow(1024, 3);
        } else if (getValue() > Math.pow(1024, 2)) {
            unit = "Mb";
            value = getValue() / Math.pow(1024, 2);
        } else if (getValue() > 1024) {
            unit = "Kb";
            value = getValue() / 1024;
        }
        return String.format(Locale.getDefault(), "%.1f%s", value, unit);
    }

    /**
     * {@link TypeConverter} for inter-conversion between {@link Long} and {@link ByteSize}
     * during protobuf conversion.
     */
    public static class Converter implements TypeConverter<ByteSize, Long> {
        @Override
        public ByteSize toDomainValue(Object instance) {
            if (instance instanceof Long) {
                return new ByteSize((Long) instance);
            }
            throw new RuntimeException("Failed to convert protobuf values to rdf values.");
        }

        @Override
        public Long toProtobufValue(Object instance) {
            if (instance instanceof ByteSize) {
                return ((ByteSize) instance).getValue();
            }
            throw new RuntimeException("Failed to convert rdf values to protobuf values.");
        }
    }
}
