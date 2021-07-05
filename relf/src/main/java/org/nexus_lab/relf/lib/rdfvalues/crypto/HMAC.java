package org.nexus_lab.relf.lib.rdfvalues.crypto;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * HMAC helper
 *
 * @author Ruipeng Zhang
 */
public class HMAC {
    /**
     * HmacSHA1
     */
    public static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";

    /**
     * HmacSHA256
     */
    public static final String HMAC_SHA256_ALGORITHM = "HmacSHA256";

    private Mac mac;

    /**
     * @param key HMAC hash key
     */
    public HMAC(EncryptionKey key) {
        this(key, HMAC_SHA1_ALGORITHM);
    }

    /**
     * @param key       HMAC hash key
     * @param algorithm HMAC hash algorithm name
     */
    public HMAC(EncryptionKey key, String algorithm) {
        this(key.serialize(), algorithm);
    }

    /**
     * @param key       HMAC hash key in byte array
     * @param algorithm HMAC hash algorithm name
     */
    public HMAC(byte[] key, String algorithm) {
        SecretKeySpec signingKey = new SecretKeySpec(key, algorithm);
        try {
            mac = Mac.getInstance(algorithm);
            mac.init(signingKey);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException(
                    String.format("Failed to init HMAC with key %s and algorithm %s",
                            Arrays.toString(key), algorithm));
        }
    }

    /**
     * Hash message using this HMAC
     *
     * @param message message to be hashed
     * @return message's HMAC
     */
    public byte[] sign(byte[] message) {
        return mac.doFinal(message);
    }

    /**
     * Verify the message's signature against this HMAC
     *
     * @param message   message to be verified
     * @param signature message's HMAC
     * @return if message's signature is valid
     */
    public boolean verify(byte[] message, byte[] signature) {
        byte[] sign = mac.doFinal(message);
        return Arrays.equals(sign, signature);
    }
}
