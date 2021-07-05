package org.nexus_lab.relf.lib.rdfvalues.client;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.nexus_lab.relf.Constants;

import org.junit.Test;

public class ClientURNTest {
    @Test
    public void parse() throws Exception {
        ClientURN urn = new ClientURN("aff4:/C.f718c60e0a08d021");
        ClientURN urn1 = new ClientURN(urn);

        assertEquals("aff4:/C.f718c60e0a08d021", urn.stringify());
        assertArrayEquals(urn.serialize(), urn1.serialize());
    }

    @Test
    public void fromPublicKey() throws Exception {
        ClientURN urn = ClientURN.fromPublicKey(Constants.CLIENT_PUBLIC_KEY);
        assertEquals("aff4:/C.81e760c9aec85ffd", urn.stringify());
    }

    @Test
    public void add() throws Exception {
        ClientURN urn = new ClientURN("aff4:/C.f718c60e0a08d021");
        assertEquals("aff4:/C.f718c60e0a08d021/tasks", urn.add("tasks").stringify());
    }
}