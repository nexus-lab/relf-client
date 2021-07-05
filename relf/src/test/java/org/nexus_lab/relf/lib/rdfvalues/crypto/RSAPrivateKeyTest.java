package org.nexus_lab.relf.lib.rdfvalues.crypto;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.nexus_lab.relf.Constants;

import org.junit.Test;

import java.util.Base64;

/**
 * @author Ruipeng Zhang
 */
public class RSAPrivateKeyTest {
    private static final String CLIENT_PRIVATE_KEY_PEM = Constants.CLIENT_PRIVATE_KEY.stringify();
    private static final String CLIENT_PUBLIC_KEY_PEM = Constants.CLIENT_PUBLIC_KEY.stringify();

    @Test
    public void parse() {
        assertEquals(
                CLIENT_PRIVATE_KEY_PEM.replace("\r", "").replace("\n", ""),
                Constants.CLIENT_PRIVATE_KEY.stringify().replace("\r", "").replace("\n", ""));
    }

    @Test
    public void getPublicKey() {
        assertEquals(
                CLIENT_PUBLIC_KEY_PEM.replace("\r", "").replace("\n", ""),
                Constants.CLIENT_PRIVATE_KEY.getPublicKey().stringify().replace("\r", "").replace(
                        "\n", ""));
    }

    @Test
    public void sign() throws Exception {
        byte[] message = "Hello".getBytes();
        byte[] signature = Constants.CLIENT_PRIVATE_KEY.sign(message);
        Constants.CLIENT_PUBLIC_KEY.verify(message, signature, RSAPrivateKey.SIGNATURE_ALGORITHM);
    }

    @Test
    public void decrypt() throws Exception {
        String encoded = "XRHhasJcRtEzn2+QNDUAfGFhau1wOpBbO9i0Uv13kWWWhVzYvlDBs" +
                "6kLCCNteQm5Q+509WAsghyUa0bATysAvqEgvQyrWnI7nhUBVpWV7AeX67LRRZf" +
                "fCDRYqBuXrOwodVcLy//BXmZVrSR1KgBNg4mQiajbFf5SI0zxU/qutSrtJ1IN9" +
                "zLL91ISVdrpPCENBLsqnw1B6oWDonaY1wBRJM/hXUJBdP5S+Nsi+m1yWibl4s5" +
                "6qLq7zGx3UDDwGJm3v2im+wPvOTbrwjm+Zy/s5AY6+JLdIzBDA0G+My4XWD2ip" +
                "g+NRbhJvAkkgPw9i8o6rgAEIcFvL12Jy3QzIkCaIg==";
        byte[] message = Base64.getMimeDecoder().decode(encoded.getBytes());
        assertEquals("Hello", new String(Constants.CLIENT_PRIVATE_KEY.decrypt(message)));
    }

    @Test
    public void generate() throws Exception {
        RSAPrivateKey key = RSAPrivateKey.generate(2048, 65537);
        RSAPublicKey pub1 = key.getPublicKey();
        key = new RSAPrivateKey(key.stringify());
        RSAPublicKey pub2 = key.getPublicKey();
        assertArrayEquals(pub1.serialize(), pub2.serialize());

        byte[] encrypted = key.getPublicKey().encrypt("Hello".getBytes());
        byte[] decrypted = key.decrypt(encrypted);
        assertArrayEquals("Hello".getBytes(), decrypted);
    }

    @Test
    public void equal() throws Exception {
        RSAPrivateKey a = new RSAPrivateKey(CLIENT_PRIVATE_KEY_PEM);
        RSAPrivateKey b = new RSAPrivateKey(CLIENT_PRIVATE_KEY_PEM);
        assertEquals(a, b);
    }
}