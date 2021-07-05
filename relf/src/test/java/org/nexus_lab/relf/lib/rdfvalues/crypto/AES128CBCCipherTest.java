package org.nexus_lab.relf.lib.rdfvalues.crypto;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.nexus_lab.relf.utils.ByteUtils;

import org.junit.Before;
import org.junit.Test;

public class AES128CBCCipherTest {
    private AES128CBCCipher cipher;

    @Before
    public void setup() {
        EncryptionKey key = EncryptionKey.fromHexString("c681027b6c307c4ea4ad561d3ca21656");
        EncryptionKey iv = EncryptionKey.fromHexString("6dd1051a03b3490fa551eaea5b8767de");
        cipher = new AES128CBCCipher(key, iv);
    }

    @Test
    public void encrypt() throws Exception {
        byte[] encrypted = cipher.encrypt("Hello".getBytes());
        assertEquals("7fe5c649748832ac1cae6496705de338", ByteUtils.toHexString(encrypted));
    }

    @Test
    public void decrypt() throws Exception {
        byte[] encrypted = ByteUtils.fromHexString("7fe5c649748832ac1cae6496705de338");
        byte[] decrypted = cipher.decrypt(encrypted);
        assertArrayEquals("Hello".getBytes(), decrypted);
    }

}