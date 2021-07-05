package org.nexus_lab.relf.lib.rdfvalues.crypto;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.nexus_lab.relf.Constants;
import org.nexus_lab.relf.lib.exceptions.DecodeException;

import org.junit.Before;
import org.junit.Test;

public class CertificateSigningRequestTest {
    private CertificateSigningRequest csr;

    @Before
    public void setup() throws DecodeException {
        csr = new CertificateSigningRequest("aff4:/C.81e760c9aec85ffd",
                Constants.CLIENT_PRIVATE_KEY);
    }

    @Test
    public void getCN() throws Exception {
        assertEquals("aff4:/C.81e760c9aec85ffd", csr.getCN());
    }

    @Test
    public void getPublicKey() throws Exception {
        assertArrayEquals(Constants.CLIENT_PUBLIC_KEY.serialize(),
                csr.getPublicKey().serialize());
    }

    @Test
    public void verify() throws Exception {
        assertTrue(csr.verify(Constants.CLIENT_PUBLIC_KEY));
    }

    @Test
    public void parse() throws Exception {
        csr = new CertificateSigningRequest().parse("-----BEGIN CERTIFICATE REQUEST-----\n" +
                "MIICcTCCAVkCAQAwGzEZMBcGA1UEAwwQdGVzdF9jb21tb25fbmFtZTCCASIwDQYJ\n" +
                "KoZIhvcNAQEBBQADggEPADCCAQoCggEBAMGwRtkv7wx0FpPy4IMPcuNt3v4R6d1a\n" +
                "DZF/xMtMGv8n95RtXDibLfDFKXnYm8J3Pu5nBNZ3HHGJZajV8d+kHtBfWlCsdxEv\n" +
                "HywR7dS8HotNjLTsVPXj1LdUG7cyJTgRS2CoAIujSMdl30djZY3vg10TauGmNPd/\n" +
                "C2xd1h05yzym/LuGHUl6g6dEPGThfnV/UHGqB/Ga8wxud9/DXGGKksqevzTKbf+1\n" +
                "nkcX0heN/Pn2v7H+0W8UsHfuB9BsskviHpZ17twgpZz4/q5o2gH05knEQFNCSxQT\n" +
                "lJy5VN8FRCQfxZ2BAFGdttF9TUa0gIbFTbBCTbybvHvbMGWKjASGMyECAwEAAaAR\n" +
                "MA8GCSqGSIb3DQEJDjECMAAwDQYJKoZIhvcNAQELBQADggEBAGP/p2EKiSCdqc4u\n" +
                "SWbpJnsehVMW2D8F0xWe56mgVeXag9TWZ306YgcLbvDkJ+CxP1/qSI0Z4+v966EK\n" +
                "6TNc06feCUMqP1MaXy10PAz/XYbsNyQLk7Iihc0l/carSclljWOuQe/ZwxAu/uGy\n" +
                "NTgzot2D3x2o5UKaCHcrbSvVVUOk+cM0uyf2oGUS0bcGElWWmNgIrj1aWCVDuogg\n" +
                "WpYFiVtR3hd9mXGbL/IaehY9R6Yk5IZX7LhTNlcpdIJzIJEfuEO/edCulyGOdhEX\n" +
                "tuS08K2V8AAxjfLUQV/O5xQoUFP26N1G3SVzFUNV+HFGvtACDlfRzKVm9uT/8ECn\n" +
                "zXm7rlw=\n" +
                "-----END CERTIFICATE REQUEST-----");
        assertEquals("test_common_name", csr.getCN());
    }
}