package org.nexus_lab.relf.lib.rdfvalues.crypto;

import org.nexus_lab.relf.lib.exceptions.DecryptionException;
import org.nexus_lab.relf.lib.exceptions.EncryptionException;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * @author Ruipeng Zhang
 */
public class AES128CBCCipher {
    private Cipher decryptor;
    private Cipher encryptor;

    /**
     * @param key encryption key
     * @param iv  initialization vector
     */
    public AES128CBCCipher(EncryptionKey key, EncryptionKey iv) {
        SecretKeySpec keySpec = new SecretKeySpec(key.serialize(), "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(iv.serialize());
        try {
            encryptor = Cipher.getInstance("AES/CBC/PKCS5Padding");
            encryptor.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
            decryptor = Cipher.getInstance("AES/CBC/PKCS5Padding");
            decryptor.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException
                | InvalidAlgorithmParameterException | InvalidKeyException e) {
            throw new RuntimeException("Can not initialize the cipher", e);
        }
    }

    /**
     * Encrypt data using this {@link AES128CBCCipher}.
     *
     * @param data data to be encrypted
     * @return encrypted data
     * @throws EncryptionException unable to encrypt data due to cipher error
     */
    public byte[] encrypt(byte[] data) throws EncryptionException {
        try {
            return encryptor.doFinal(data);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            throw new EncryptionException("Can not encrypt data", e);
        }
    }

    /**
     * Decrypt data using this {@link AES128CBCCipher}.
     *
     * @param data data to be decrypted
     * @return decrypted data
     * @throws DecryptionException unable to decrypt data due to cipher error
     */
    public byte[] decrypt(byte[] data) throws DecryptionException {
        try {
            return decryptor.doFinal(data);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            throw new DecryptionException("Can not decrypt data", e);
        }
    }
}
