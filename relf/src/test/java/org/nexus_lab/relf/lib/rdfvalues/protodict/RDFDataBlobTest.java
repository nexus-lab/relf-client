package org.nexus_lab.relf.lib.rdfvalues.protodict;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.nexus_lab.relf.lib.rdfvalues.RDFDatetime;
import org.nexus_lab.relf.lib.rdfvalues.RDFInteger;
import org.nexus_lab.relf.lib.rdfvalues.RDFString;

import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RDFDataBlobTest {
    @Test
    public void getBlobValue() throws Exception {
        RDFDataBlob blob = new RDFDataBlob();
        blob.setActualValue(null);
        assertEquals(null, blob.getActualValue());

        blob = new RDFDataBlob();
        long time = System.currentTimeMillis() * 1000;
        blob.setActualValue(new RDFDatetime(time));
        assertEquals(time, ((RDFDatetime) blob.getActualValue()).asMicrosecondsFromEpoch());

        blob = new RDFDataBlob();
        blob.setActualValue(Arrays.asList(1L, 2L, 3L));
        assertArrayEquals(Arrays.asList(1L, 2L, 3L).toArray(),
                ((List) blob.getActualValue()).toArray());

        blob = new RDFDataBlob();
        blob.setActualValue(new HashSet<>(Arrays.asList(1L, 2L, 3L)));
        assertArrayEquals(new HashSet<>(Arrays.asList(1L, 2L, 3L)).toArray(),
                ((Set) blob.getActualValue()).toArray());

        blob = new RDFDataBlob();
        blob.setActualValue(1);
        assertEquals(1L, blob.getActualValue());

        blob = new RDFDataBlob();
        blob.setActualValue(0.5);
        assertEquals(0.5F, blob.getActualValue());

        blob = new RDFDataBlob();
        blob.setActualValue("Hello");
        assertEquals("Hello", blob.getActualValue());

        blob = new RDFDataBlob();
        blob.setActualValue(new byte[]{1, 2, 3});
        assertArrayEquals(new byte[]{1, 2, 3}, (byte[]) blob.getActualValue());

        blob = new RDFDataBlob();
        blob.setActualValue(true);
        assertEquals(true, blob.getActualValue());

        RDFDict dict = new RDFDict();
        dict.put(1L, 1L);
        blob = new RDFDataBlob();
        blob.setActualValue(dict);
        assertEquals(dict, blob.getActualValue());
    }

    @Test
    public void setBlobValue() throws Exception {
        RDFDataBlob blob = new RDFDataBlob();
        blob.setActualValue(null);
        blob.setActualValue(Long.MAX_VALUE);
        blob.setActualValue(Float.MAX_VALUE);
        blob.setActualValue(Boolean.TRUE);
        blob.setActualValue("Hello world");
        blob.setActualValue(new byte[]{1, 2, 3, 4});
        blob.setActualValue(new RDFInteger(1));
        blob.setActualValue(new RDFString[]{new RDFString("hello"), new RDFString("world")});
        blob.setActualValue(Arrays.asList(new RDFString("i love"), new RDFString("java")));
        blob.setActualValue(
                new HashSet<>(Arrays.asList(new RDFString("hello"), new RDFString("world"))));

        RDFDict dict = new RDFDict();
        dict.put(1, 2);
        blob.setActualValue(dict);

        assertEquals("None", blob.getNone());
        assertEquals((Object) Long.MAX_VALUE, blob.getInteger());
        assertEquals((Object) Float.MAX_VALUE, blob.getFloatValue());
        assertEquals(Boolean.TRUE, blob.getBooleanValue());
        assertEquals("Hello world", blob.getString());
        assertArrayEquals(new byte[]{1, 2, 3, 4}, blob.getData());

        RDFInteger rdfInteger = new RDFInteger();
        rdfInteger.parse(blob.getRdfValue().getData());
        assertEquals(new RDFInteger(1), rdfInteger);

        assertEquals(4, blob.getList().getContent().size());
        assertEquals("hello", new String(blob.getList().getContent().get(
                0).getRdfValue().getData()));
        assertEquals("world", new String(blob.getList().getContent().get(
                1).getRdfValue().getData()));
        assertEquals("i love", new String(blob.getList().getContent().get(
                2).getRdfValue().getData()));
        assertEquals("java", new String(blob.getList().getContent().get(
                3).getRdfValue().getData()));

        assertEquals(2, blob.getSet().getContent().size());
    }

}