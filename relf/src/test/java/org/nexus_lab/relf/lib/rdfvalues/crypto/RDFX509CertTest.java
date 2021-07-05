package org.nexus_lab.relf.lib.rdfvalues.crypto;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.nexus_lab.relf.Constants;

import org.junit.Test;

public class RDFX509CertTest {
    @Test
    public void getCN() throws Exception {
        assertEquals(Constants.CA_CERTIFICATE.getCN(), "grr");
    }

    @Test
    public void verify() throws Exception {
        assertTrue(Constants.CA_CERTIFICATE.verify(Constants.CA_CERTIFICATE.getPublicKey()));
    }

}