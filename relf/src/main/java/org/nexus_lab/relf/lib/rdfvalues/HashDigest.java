package org.nexus_lab.relf.lib.rdfvalues;

import com.google.protobuf.ByteString;
import org.nexus_lab.relf.utils.ByteUtils;

import net.badata.protobuf.converter.type.TypeConverter;

import lombok.NoArgsConstructor;

/**
 * @author Ruipeng Zhang
 */
@NoArgsConstructor
public class HashDigest extends RDFBytes {
    public HashDigest(byte[] value) {
        super(value);
    }

    /**
     * @return hexadecimal representation of the hash digest
     */
    public String getHexDigest() {
        return ByteUtils.toHexString(getValue());
    }

    /**
     * {@link TypeConverter} for inter-conversion between {@link ByteString} and {@link HashDigest}
     * during protobuf conversion.
     */
    public static class Converter implements TypeConverter<HashDigest, ByteString> {
        @Override
        public HashDigest toDomainValue(Object instance) {
            if (instance instanceof HashDigest) {
                return new HashDigest(((ByteString) instance).toByteArray());
            }
            throw new RuntimeException("Failed to convert protobuf values to rdf values.");
        }

        @Override
        public ByteString toProtobufValue(Object instance) {
            if (instance instanceof HashDigest) {
                return ByteString.copyFrom(((HashDigest) instance).serialize());
            }
            throw new RuntimeException("Failed to convert rdf values to protobuf values.");
        }
    }
}
