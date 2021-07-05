package org.nexus_lab.relf.lib.rdfvalues.crypto;

import com.google.protobuf.ByteString;
import org.nexus_lab.relf.lib.rdfvalues.RDFBytes;
import org.nexus_lab.relf.utils.ByteUtils;

import net.badata.protobuf.converter.type.TypeConverter;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Locale;
import java.util.Random;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @author Ruipeng Zhang
 */
@NoArgsConstructor
public class EncryptionKey extends RDFBytes {
    @Getter
    @Accessors(fluent = true)
    private int length;

    public EncryptionKey(byte[] value) {
        super(value);
    }

    /**
     * Create {@link EncryptionKey} from hexadecimal string.
     *
     * @param string serialized string representation of an {@link EncryptionKey}
     * @return a new instance of {@link EncryptionKey}
     */
    public static EncryptionKey fromHexString(String string) {
        return new EncryptionKey(ByteUtils.fromHexString(string));
    }

    /**
     * Generate a new {@link EncryptionKey} of the given length.
     *
     * @param length key length in bits
     * @return a new instance of {@link EncryptionKey}
     */
    public static EncryptionKey generateKey(int length) {
        byte[] data = new byte[length / 8];
        new Random().nextBytes(data);
        return new EncryptionKey(data);
    }

    /**
     * Generate a new random initialization vector of the given length.
     *
     * @param length vector length in bits
     * @return a new initialization vector
     */
    public static EncryptionKey generateRandomIV(int length) {
        return generateKey(length);
    }

    @Override
    public void setValue(byte[] value) {
        length = value.length * 8;
        if (length < 128) {
            throw new RuntimeException(String.format(Locale.US, "Key too short (%d): %s.", length,
                    Arrays.toString(value)));
        }
        super.setValue(value);
    }

    @Override
    public EncryptionKey parse(byte[] value) {
        if (value.length % 8 != 0) {
            throw new RuntimeException(
                    String.format(Locale.US, "Invalid key length %d (%s).", value.length,
                            Arrays.toString(value)));
        }
        setValue(value);
        return this;
    }

    @Override
    public String toString() {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(ByteUtils.toHexString(getValue()).getBytes());
            return String.format("<%s> %s", getClass().getSimpleName(),
                    ByteUtils.toHexString(hash));
        } catch (NoSuchAlgorithmException ignored) {
        }
        return String.format("<%s> %s", getClass().getSimpleName(), "");
    }

    /**
     * {@link TypeConverter} for inter-conversion between {@link ByteString} and {@link
     * EncryptionKey}
     * during protobuf conversion.
     */
    public static class Converter implements TypeConverter<EncryptionKey, ByteString> {
        @Override
        public EncryptionKey toDomainValue(Object instance) {
            if (instance instanceof ByteString) {
                return new EncryptionKey(((ByteString) instance).toByteArray());
            }
            throw new RuntimeException("Failed to convert protobuf values to rdf values.");
        }

        @Override
        public ByteString toProtobufValue(Object instance) {
            if (instance instanceof EncryptionKey) {
                return ByteString.copyFrom(((EncryptionKey) instance).serialize());
            }
            throw new RuntimeException("Failed to convert rdf values to protobuf values.");
        }
    }
}
