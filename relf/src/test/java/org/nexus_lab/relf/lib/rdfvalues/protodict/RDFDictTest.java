package org.nexus_lab.relf.lib.rdfvalues.protodict;

import static org.junit.Assert.assertEquals;

import org.nexus_lab.relf.proto.DataBlob;
import org.nexus_lab.relf.proto.Dict;
import org.nexus_lab.relf.proto.KeyValue;

import org.junit.Test;

public class RDFDictTest {
    @Test
    public void get() throws Exception {
        Dict protobuf = Dict.newBuilder()
                .addDat(KeyValue.newBuilder().setK(DataBlob.newBuilder().setInteger(1))
                        .setV(DataBlob.newBuilder().setInteger(2)))
                .build();
        RDFDict dict = new RDFDict();
        dict.parse(protobuf.toByteArray());
        assertEquals(2L, dict.get(1L));
    }

    @Test
    public void put() throws Exception {
        RDFDict dict = new RDFDict();
        dict.put(1, 2);
        Dict protobuf = Dict.parseFrom(dict.serialize());
        assertEquals(1, protobuf.getDat(0).getK().getInteger());
        assertEquals(2, protobuf.getDat(0).getV().getInteger());
    }
}