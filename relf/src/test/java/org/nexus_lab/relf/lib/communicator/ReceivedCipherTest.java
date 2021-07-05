package org.nexus_lab.relf.lib.communicator;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.nexus_lab.relf.Constants;
import org.nexus_lab.relf.lib.rdfvalues.flows.RDFClientCommunication;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.IOException;

/**
 * @author Ruipeng Zhang
 */
public class ReceivedCipherTest {
    private static final RDFClientCommunication PACKET;
    private ReceivedCipher cipher;

    static {
        try {
            byte[] bytes = IOUtils.toByteArray(new FileInputStream(Constants.fspath("/tmp/packet")));
            PACKET = (RDFClientCommunication) new RDFClientCommunication().parse(bytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Before
    public void setup() throws Exception {
        cipher = ReceivedCipher.from(PACKET, Constants.CLIENT_PRIVATE_KEY);
    }

    @Test
    public void from() {
        assertNotNull(cipher.getCipher());
        assertNotNull(cipher.getMetadata());
        assertArrayEquals(PACKET.getEncryptedCipher(),
                cipher.getEncryptedCipher());
        assertArrayEquals(PACKET.getEncryptedCipherMetadata(),
                cipher.getEncryptedMetadata());
    }

    @Test
    public void verifyHmac() throws Exception {
        assertTrue(cipher.verifyHmac(PACKET));
    }

    @Test
    public void verifySignature() throws Exception {
        assertTrue(cipher.verifySignature(Constants.SERVER_CERTIFICATE.getPublicKey()));
        cipher.setMetadata(null);
        assertFalse(cipher.verifySignature(Constants.SERVER_CERTIFICATE.getPublicKey()));
    }
}