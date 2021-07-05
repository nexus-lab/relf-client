package org.nexus_lab.relf.lib.rdfvalues;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.nexus_lab.relf.lib.rdfvalues.flows.RDFClientCommunication;
import org.nexus_lab.relf.lib.rdfvalues.flows.RDFGrrMessage;
import org.nexus_lab.relf.lib.rdfvalues.flows.RDFMessageList;
import org.nexus_lab.relf.proto.GrrMessage;

import org.junit.Test;

public class RDFProtoStructTest {
    @Test
    public void getName() throws Exception {
        RDFClientCommunication packet = new RDFClientCommunication();
        assertEquals("ClientCommunication", packet.getRDFName());
    }

    @Test
    public void serialize() throws Exception {
        RDFGrrMessage message = new RDFGrrMessage();
        message.setName("message");
        GrrMessage protobuf = GrrMessage.newBuilder().setName("message").build();
        RDFGrrMessage parsedMessage = new RDFGrrMessage();
        parsedMessage.parse(protobuf.toByteArray());
        assertEquals(message.getName(), parsedMessage.getName());
    }

    @Test
    public void parse() throws Exception {
        GrrMessage protobuf = GrrMessage.newBuilder().setName("message").build();
        RDFGrrMessage message = new RDFGrrMessage();
        message.parse(protobuf.toByteArray());
        assertEquals("message", message.getName());
    }

    @Test
    public void equalAndHash() throws Exception {
        RDFGrrMessage message1 = new RDFGrrMessage();
        RDFGrrMessage message2 = new RDFGrrMessage();
        message1.setName("abc");
        message1.setTaskId(123L);
        message2.setName("abc");
        message2.setTaskId(123L);
        assertEquals(message1, message2);
        assertEquals(message1.hashCode(), message2.hashCode());

        RDFMessageList list1 = new RDFMessageList();
        RDFMessageList list2 = new RDFMessageList();
        list1.addJob(message1);
        list2.addJob(message2);
        assertEquals(list1, list2);
        assertEquals(list1.hashCode(), list2.hashCode());

        list1.setAge(new RDFDatetime(1));
        list2.setAge(new RDFDatetime(2));
        assertNotEquals(list1, list2);
        assertNotEquals(list1.hashCode(), list2.hashCode());
    }
}