package org.nexus_lab.relf.lib.rdfvalues;

import com.google.protobuf.ByteString;

import net.badata.protobuf.converter.type.TypeConverter;

import java.util.Arrays;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author Ruipeng Zhang
 */
@EqualsAndHashCode
@NoArgsConstructor
public class RDFBytes implements RDFValue<byte[]> {
    @Getter
    @Setter
    private byte[] value;
    @Getter
    @Setter
    private RDFDatetime age = new RDFDatetime();

    public RDFBytes(byte[] value) {
        parse(value);
    }

    @Override
    public RDFBytes parse(byte[] value) {
        setValue(Arrays.copyOfRange(value, 0, value.length));
        return this;
    }

    @Override
    public byte[] serialize() {
        return Arrays.copyOfRange(getValue(), 0, getValue().length);
    }

    /**
     * {@link TypeConverter} for inter-conversion between {@link ByteString} and {@link RDFBytes}
     * during protobuf conversion.
     */
    public static class Converter implements TypeConverter<RDFBytes, ByteString> {
        @Override
        public RDFBytes toDomainValue(Object instance) {
            if (instance instanceof ByteString) {
                return new RDFBytes(((ByteString) instance).toByteArray());
            }
            throw new RuntimeException("Failed to convert protobuf values to rdf values.");
        }

        @Override
        public ByteString toProtobufValue(Object instance) {
            if (instance instanceof RDFBytes) {
                return ByteString.copyFrom(((RDFBytes) instance).serialize());
            }
            throw new RuntimeException("Failed to convert rdf values to protobuf values.");
        }
    }
}
