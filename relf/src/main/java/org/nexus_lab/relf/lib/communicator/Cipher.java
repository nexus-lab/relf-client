package org.nexus_lab.relf.lib.communicator;

import org.nexus_lab.relf.lib.exceptions.DecryptionException;
import org.nexus_lab.relf.lib.exceptions.EncryptionException;
import org.nexus_lab.relf.lib.rdfvalues.RDFDatetime;
import org.nexus_lab.relf.lib.rdfvalues.RDFURN;
import org.nexus_lab.relf.lib.rdfvalues.crypto.AES128CBCCipher;
import org.nexus_lab.relf.lib.rdfvalues.crypto.EncryptionKey;
import org.nexus_lab.relf.lib.rdfvalues.crypto.HMAC;
import org.nexus_lab.relf.lib.rdfvalues.crypto.RSAPrivateKey;
import org.nexus_lab.relf.lib.rdfvalues.crypto.RSAPublicKey;
import org.nexus_lab.relf.lib.rdfvalues.flows.RDFCipherMetadata;
import org.nexus_lab.relf.lib.rdfvalues.flows.RDFCipherProperties;
import org.nexus_lab.relf.proto.CipherProperties;

import java.security.SignatureException;

import lombok.Getter;
import lombok.Setter;

/**
 * A wrapper of AES128CBC cipher, encrypted cipher and its metadata
 *
 * @author Ruipeng Zhang
 */
public class Cipher {
    private static final String CIPHER_NAME = "aes_128_cbc";
    private static final int KEY_SIZE = 128;
    private static final int IV_SIZE = 128;

    @Getter
    private RDFDatetime age;
    @Getter
    private RDFCipherProperties cipher;
    @Getter
    @Setter
    private RDFCipherMetadata metadata;
    @Getter
    @Setter
    private byte[] encryptedCipher;
    @Getter
    @Setter
    private byte[] encryptedMetadata;

    /**
     * @param cipher cipher properties
     */
    Cipher(RDFCipherProperties cipher) {
        this.cipher = cipher;
    }

    /**
     * Create a {@link Cipher} from private and public key with the given source.
     *
     * @param source     the common name this cipher should be used to communicate with
     * @param privateKey the private key used to sign the cipher
     * @param publicKey  the server public key used to encrypt cipher
     * @return a new {@link Cipher} instance
     * @throws SignatureException  cannot sign the cipher using provided private key
     * @throws EncryptionException cannot encrypt cipher using provided public key
     */
    public static Cipher create(RDFURN source, RSAPrivateKey privateKey, RSAPublicKey publicKey)
            throws SignatureException, EncryptionException {
        RDFCipherProperties cipher = new RDFCipherProperties();
        cipher.setName(CIPHER_NAME);
        cipher.setKey(EncryptionKey.generateKey(KEY_SIZE));
        cipher.setMetadataIv(EncryptionKey.generateKey(IV_SIZE));
        cipher.setHmacKey(EncryptionKey.generateKey(KEY_SIZE));
        cipher.setHmacType(CipherProperties.HMACType.FULL_HMAC);
        Cipher result = new Cipher(cipher);
        byte[] serializedCipher = cipher.serialize();
        result.metadata = new RDFCipherMetadata();
        result.metadata.setSource(source);
        result.metadata.setSignature(privateKey.sign(serializedCipher));
        result.encryptedCipher = publicKey.encrypt(serializedCipher);
        result.encryptedMetadata = encrypt(result.metadata.serialize(), cipher.getKey(),
                cipher.getMetadataIv());
        result.age = RDFDatetime.now();
        return result;
    }

    private static byte[] encrypt(byte[] data, EncryptionKey key, EncryptionKey iv)
            throws EncryptionException {
        AES128CBCCipher cipher = new AES128CBCCipher(key, iv);
        return cipher.encrypt(data);
    }

    /**
     * Encrypt data using this cipher
     *
     * @param data data to be encrypted
     * @param iv   the initialization vector
     * @return the encrypted data
     * @throws EncryptionException failed to encrypt data
     */
    public byte[] encrypt(byte[] data, EncryptionKey iv) throws EncryptionException {
        return encrypt(data, cipher.getKey(), iv);
    }

    /**
     * Decrypt data using this cipher
     *
     * @param data data to be decrypted
     * @param iv   the initialization vector
     * @return the decrypted data
     * @throws DecryptionException failed to decrypt data
     */
    public byte[] decrypt(byte[] data, EncryptionKey iv) throws DecryptionException {
        AES128CBCCipher cipher = new AES128CBCCipher(this.cipher.getKey(), iv);
        return cipher.decrypt(data);
    }

    /**
     * Create HMAC of byte arrays
     *
     * @param data data to be hashed
     * @return HMAC of the data
     */
    public byte[] hmac(byte[] data) {
        return new HMAC(cipher.getHmacKey()).sign(data);
    }

    /**
     * @return the common name this cipher should be used to communicate with
     */
    public RDFURN getSource() {
        return metadata == null ? null : metadata.getSource();
    }

    /**
     * @return cipher HMAC type
     */
    public CipherProperties.HMACType getHmacType() {
        return cipher.getHmacType();
    }
}
