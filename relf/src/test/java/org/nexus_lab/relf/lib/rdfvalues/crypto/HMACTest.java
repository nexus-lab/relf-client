package org.nexus_lab.relf.lib.rdfvalues.crypto;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.nexus_lab.relf.utils.ByteUtils;

import org.junit.Test;

public class HMACTest {
    @Test
    public void sign() throws Exception {
        HMAC hmac = new HMAC(EncryptionKey.fromHexString("64fc3175fc63e5e39dc888f46abd223c"));
        byte[] signature = hmac.sign("Hello".getBytes());
        assertEquals("0dd9722950c2f3d1434de1d75c9d9abe3a97c846",
                ByteUtils.toHexString(signature));
    }

    @Test
    public void verify() throws Exception {
        byte[] signature = ByteUtils.fromHexString("0dd9722950c2f3d1434de1d75c9d9abe3a97c846");
        HMAC hmac = new HMAC(EncryptionKey.fromHexString("64fc3175fc63e5e39dc888f46abd223c"));
        assertTrue(hmac.verify("Hello".getBytes(), signature));
    }

}