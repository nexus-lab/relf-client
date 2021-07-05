package org.nexus_lab.relf.lib.rdfvalues.client;

import com.google.protobuf.ByteString;
import org.nexus_lab.relf.lib.rdfvalues.RDFBytes;
import org.nexus_lab.relf.utils.ByteUtils;

import net.badata.protobuf.converter.type.TypeConverter;

import lombok.NoArgsConstructor;

/**
 * @author Ruipeng Zhang
 */
@NoArgsConstructor
public class MacAddress extends RDFBytes {
    public MacAddress(byte[] value) {
        super(value);
    }

    /**
     * @return hexadecimal string of the MAC address
     */
    public String getHumanReadableAddress() {
        return ByteUtils.toMacAddress(getValue()).toUpperCase();
    }

    /**
     * {@link TypeConverter} for inter-conversion between {@link ByteString} and {@link MacAddress}
     * during protobuf conversion.
     */
    public static class Converter implements TypeConverter<MacAddress, ByteString> {
        @Override
        public MacAddress toDomainValue(Object instance) {
            if (instance instanceof ByteString) {
                return new MacAddress(((ByteString) instance).toByteArray());
            }
            throw new RuntimeException("Failed to convert protobuf values to rdf values.");
        }

        @Override
        public ByteString toProtobufValue(Object instance) {
            if (instance instanceof MacAddress) {
                return ByteString.copyFrom(((MacAddress) instance).serialize());
            }
            throw new RuntimeException("Failed to convert rdf values to protobuf values.");
        }
    }
}
