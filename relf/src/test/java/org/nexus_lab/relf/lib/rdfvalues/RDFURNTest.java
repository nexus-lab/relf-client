package org.nexus_lab.relf.lib.rdfvalues;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class RDFURNTest {
    @Test
    public void split() throws Exception {
        RDFURN urn = new RDFURN("a/b/c/d/e");
        String[] fragments = urn.split(3);
        assertArrayEquals(new String[]{"a", "b", "c"}, fragments);
    }

    @Test
    public void getDirname() throws Exception {
        RDFURN urn = new RDFURN("a/b/c/d/e");
        assertEquals("/a/b/c/d/", urn.getDirname());
    }

    @Test
    public void getBasename() throws Exception {
        RDFURN urn = new RDFURN("a/b/c/d/e");
        assertEquals("e", urn.getBasename());
    }

    @Test
    public void getRelativeName() throws Exception {
        RDFURN urn = new RDFURN("a/b/c/d/e");
        assertEquals("d/e", urn.getRelativeName("aff4:/a/b/c"));
    }

    @Test
    public void parse() throws Exception {
        RDFURN urn = new RDFURN("client/tasks");
        assertEquals("aff4:/client/tasks", urn.stringify());
        urn.parse("another\\urn");
        assertEquals("aff4:/another/urn", urn.stringify());
    }

    @Test
    public void join() throws Exception {
        RDFURN base = new RDFURN("flows");
        RDFURN joined = base.join("E:Enrol");
        assertEquals("aff4:/flows/E:Enrol", joined.stringify());
    }

    @Test
    public void equal() throws Exception {
        RDFURN a = new RDFURN("a");
        RDFURN b = new RDFURN("aff4:/a");
        assertEquals(a, b);
    }
}