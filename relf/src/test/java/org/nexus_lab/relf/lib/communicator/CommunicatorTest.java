package org.nexus_lab.relf.lib.communicator;

import static org.junit.Assert.assertEquals;

import org.nexus_lab.relf.Constants;
import org.nexus_lab.relf.client.ClientCommunicator;
import org.nexus_lab.relf.lib.config.ConfigLib;
import org.nexus_lab.relf.lib.config.InMemoryConfig;
import org.nexus_lab.relf.lib.rdfvalues.RDFDatetime;
import org.nexus_lab.relf.lib.rdfvalues.RDFURN;
import org.nexus_lab.relf.lib.rdfvalues.flows.RDFClientCommunication;
import org.nexus_lab.relf.lib.rdfvalues.flows.RDFGrrMessage;
import org.nexus_lab.relf.lib.rdfvalues.flows.RDFMessageList;

import org.junit.Before;
import org.junit.Test;

public class CommunicatorTest {
    private RDFDatetime nonce;
    private Communicator server;
    private Communicator client;

    @Before
    public void setup() throws Exception {
        InMemoryConfig config = new InMemoryConfig();
        ConfigLib.use(config);
        nonce = RDFDatetime.now();
        client = ClientCommunicator.from(Constants.CA_CERTIFICATE, Constants.SERVER_CERTIFICATE,
                Constants.CLIENT_PRIVATE_KEY);
        server = new Communicator(new RDFURN("grr"), client.getLocalName(),
                Constants.SERVER_PRIVATE_KEY, Constants.CLIENT_PUBLIC_KEY);
    }

    @Test
    public void encode() throws Exception {
        RDFMessageList expected = new RDFMessageList();
        RDFGrrMessage job = new RDFGrrMessage();
        job.setName("Empty");
        expected.addJob(job);

        RDFClientCommunication packet = client.encode(expected, nonce);
        RDFMessageList actual = server.decode(packet, nonce);

        assertEquals(expected.getJobs().get(0).getName(), actual.getJobs().get(0).getName());
    }

    @Test
    public void decode() throws Exception {
        RDFMessageList expected = new RDFMessageList();
        RDFGrrMessage job = new RDFGrrMessage();
        job.setName("Empty");
        expected.addJob(job);
        RDFClientCommunication packet = server.encode(expected, nonce);
        RDFMessageList actual = client.decode(packet, nonce);

        assertEquals(expected.getJobs().get(0).getName(), actual.getJobs().get(0).getName());
    }
}