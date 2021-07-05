package org.nexus_lab.relf.lib.communicator;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.nexus_lab.relf.Constants;
import org.nexus_lab.relf.lib.rdfvalues.RDFURN;
import org.nexus_lab.relf.lib.rdfvalues.crypto.AES128CBCCipher;
import org.nexus_lab.relf.lib.rdfvalues.crypto.EncryptionKey;
import org.nexus_lab.relf.lib.rdfvalues.crypto.HMAC;
import org.nexus_lab.relf.proto.CipherProperties;

import org.junit.Before;
import org.junit.Test;

public class CipherTest {
    private Cipher cipher;
    private RDFURN source;

    @Before
    public void setup() throws Exception {
        source = new RDFURN().parse("relf");
        cipher = Cipher.create(source, Constants.CLIENT_PRIVATE_KEY, Constants.CLIENT_PUBLIC_KEY);
    }

    @Test
    public void create() throws Exception {
        assertNotNull(cipher.getCipher());
        assertNotNull(cipher.getMetadata());
        assertNotNull(cipher.getEncryptedMetadata());
        byte[] serialized = Constants.CLIENT_PRIVATE_KEY.decrypt(cipher.getEncryptedCipher());
        assertArrayEquals(cipher.getCipher().serialize(), serialized);
    }

    @Test
    public void encrypt() throws Exception {
        EncryptionKey iv = EncryptionKey.generateRandomIV(128);
        byte[] actual = new byte[]{0, 1, 2, 3};
        byte[] encrypted = cipher.encrypt(actual, iv);
        AES128CBCCipher aes = new AES128CBCCipher(cipher.getCipher().getKey(), iv);
        byte[] decrypted = aes.decrypt(encrypted);
        assertArrayEquals(actual, decrypted);
    }

    @Test
    public void decrypt() throws Exception {
        EncryptionKey iv = EncryptionKey.generateRandomIV(128);
        byte[] actual = new byte[]{0, 1, 2, 3};
        AES128CBCCipher aes = new AES128CBCCipher(cipher.getCipher().getKey(), iv);
        byte[] encrypted = aes.encrypt(actual);
        byte[] decrypted = cipher.decrypt(encrypted, iv);
        assertArrayEquals(actual, decrypted);
    }

    @Test
    public void hmac() throws Exception {
        byte[] data = new byte[]{0, 1, 2, 3};
        byte[] actual = cipher.hmac(data);
        HMAC hmac = new HMAC(cipher.getCipher().getHmacKey());
        byte[] expected = hmac.sign(data);
        assertArrayEquals(expected, actual);
    }

    @Test
    public void getSource() throws Exception {
        assertEquals(source, cipher.getSource());
    }

    @Test
    public void getHmacType() throws Exception {
        assertEquals(CipherProperties.HMACType.FULL_HMAC, cipher.getHmacType());
    }

}