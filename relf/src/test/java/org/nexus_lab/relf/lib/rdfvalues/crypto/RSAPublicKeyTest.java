package org.nexus_lab.relf.lib.rdfvalues.crypto;

import static org.junit.Assert.assertEquals;

import org.nexus_lab.relf.Constants;

import org.junit.Test;

import java.util.Base64;

/**
 * @author Ruipeng Zhang
 */
public class RSAPublicKeyTest {
    private static final String CLIENT_PUBLIC_KEY_PEM = Constants.CLIENT_PUBLIC_KEY.stringify();

    @Test
    public void parse() {
        assertEquals(
                CLIENT_PUBLIC_KEY_PEM.replace("\r", "").replace("\n", ""),
                Constants.CLIENT_PUBLIC_KEY.stringify().replace("\r", "").replace("\n", ""));
    }

    @Test
    public void encrypt() throws Exception {
        String message = "Hello";
        byte[] encoded = Constants.CLIENT_PUBLIC_KEY.encrypt(message.getBytes());
        assertEquals(message, new String(Constants.CLIENT_PRIVATE_KEY.decrypt(encoded)));
    }

    @Test
    public void verify() throws Exception {
        byte[] message = "Hello".getBytes();
        byte[] signature = Base64.getMimeDecoder().decode(
                "GhwjzYZnIVk+PwFy9q8818wOkpvsFSybKwxsRgfo2mEQWy/EFRTwW2K8d6O8mvuW79xVu"
                        + "+07HWqtHhAUdsBBDw0rpJLvJh0+DwTsA15dPPBIEmYAZCEzATcWBH+MEY7S"
                        + "+iwpSqK9BAfSNqMtI/5Wo4TuIFVfLQhYmTuufO"
                        + "+RtZrKEvbEKZr4cUVsKXp5N7R8rKCkmDkEvQe63Bk9rQLDHiCW9PsyqXVi3Swo3xrnB4J"
                        + "+cU4hO6RXAImb0Fv16uEIiYvu8ndK94K4eyXIJyR9JNv6S49vcSDFVPJTMJzNHkYPuohO"
                        + "AJfzuGH0GwOz5KgUqX4lVvm+uf94XJKeZm1usg==");
        Constants.CLIENT_PUBLIC_KEY.verify(message, signature, RSAPrivateKey.SIGNATURE_ALGORITHM);
    }
}