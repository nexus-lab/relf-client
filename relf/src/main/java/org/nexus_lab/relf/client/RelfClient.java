package org.nexus_lab.relf.client;

import android.content.Context;
import android.net.TrafficStats;
import android.util.Log;

import org.nexus_lab.relf.client.exceptions.MemoryExceededException;
import org.nexus_lab.relf.lib.config.ConfigLib;
import org.nexus_lab.relf.lib.exceptions.DecodeException;
import org.nexus_lab.relf.lib.exceptions.EncryptionException;
import org.nexus_lab.relf.lib.rdfvalues.RDFDatetime;
import org.nexus_lab.relf.lib.rdfvalues.RDFURN;
import org.nexus_lab.relf.lib.rdfvalues.SessionID;
import org.nexus_lab.relf.lib.rdfvalues.client.ClientURN;
import org.nexus_lab.relf.lib.rdfvalues.crypto.CertificateSigningRequest;
import org.nexus_lab.relf.lib.rdfvalues.crypto.RDFCertificate;
import org.nexus_lab.relf.lib.rdfvalues.crypto.RDFX509Cert;
import org.nexus_lab.relf.lib.rdfvalues.crypto.RSAPrivateKey;
import org.nexus_lab.relf.lib.rdfvalues.flows.RDFClientCommunication;
import org.nexus_lab.relf.lib.rdfvalues.flows.RDFGrrMessage;
import org.nexus_lab.relf.lib.rdfvalues.flows.RDFMessageList;
import org.nexus_lab.relf.lib.rdfvalues.protodict.RDFDataBlob;
import org.nexus_lab.relf.proto.Certificate;
import org.nexus_lab.relf.proto.GrrMessage;
import org.nexus_lab.relf.utils.store.SizeQueue;

import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.IOException;
import java.net.Proxy;
import java.security.SignatureException;
import java.util.Collection;
import java.util.List;

import lombok.Getter;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * The entry point for the ReLF module
 *
 * @author Ruipeng Zhang
 */
public class RelfClient {
    private static final String TAG = RelfClient.class.getSimpleName();

    private static final int TRAFFIC_STATS_TAG = 1;
    private static final String SERVER_API_ENDPOINT = "control?api=" + ConfigLib.get("Network.api", 3);

    @Getter
    private static ClientStatsCollector statsCollector;
    @Getter
    private static long totalBytesSent;
    @Getter
    private static long totalBytesReceived;

    @Getter
    private Timer timer;
    private Foreman foreman;
    private Enroller enroller;
    private HttpClient httpClient;
    private RelfWorker worker;
    private ClientCommunicator communicator;
    /**
     * For messages meant to be sent to server.
     */
    private SizeQueue<RDFGrrMessage> queue;

    /**
     * @param context application context
     */
    public RelfClient(Context context) {
        TrafficStats.setThreadStatsTag(TRAFFIC_STATS_TAG);
        totalBytesSent = 0;
        totalBytesReceived = 0;

        Integer apiVersion = ConfigLib.get("Network.api", 3);
        if (apiVersion != 3) {
            throw new RuntimeException(
                    String.format("Unsupported api version: %s, expected 3.", apiVersion));
        }
        timer = new Timer();
        foreman = new Foreman();
        enroller = new Enroller();
        queue = new SizeQueue<>(ConfigLib.get("Client.max_out_queue", 51200000));

        httpClient = new HttpClient();
        Object endpoints = ConfigLib.get("Client.server_urls");
        if (endpoints instanceof String) {
            httpClient.addEndpoint((String) endpoints);
        } else if (endpoints instanceof List) {
            //noinspection unchecked
            for (String endpoint : (List<String>) endpoints) {
                httpClient.addEndpoint(endpoint);
            }
        }
        Object proxies = ConfigLib.get("Client.proxy_servers");
        if (proxies instanceof String) {
            httpClient.addProxy((String) proxies);
        } else if (proxies instanceof List) {
            //noinspection unchecked
            for (String proxy : (List<String>) proxies) {
                httpClient.addProxy(proxy);
            }
        }
        httpClient.addProxy(Proxy.NO_PROXY);

        worker = new RelfWorker(context, this);
        statsCollector = new ClientStatsCollector(worker, 10);
        worker.start();
        statsCollector.start();
    }

    /**
     * Push a message into the input queue for handling
     *
     * @param message  input message
     * @param priority message priority, see {@link GrrMessage.Priority}
     * @param blocking if the output queue is full, block until there is space.
     * @throws SizeQueue.QueueFullException output queue cannot take more messages
     */
    public void enqueue(RDFGrrMessage message, GrrMessage.Priority priority, boolean blocking)
            throws SizeQueue.QueueFullException {
        queue.put(message, priority, blocking);
    }

    private RSAPrivateKey getOrCreatePrivateKey() throws DecodeException {
        RSAPrivateKey privateKey = ConfigLib.get("Client.private_key", RSAPrivateKey.class);
        if (privateKey == null) {
            privateKey = RSAPrivateKey.generate(ConfigLib.get("Client.rsa_key_length", 2048));
            ConfigLib.set("Client.private_key", privateKey.stringify());
            ConfigLib.write();
            Log.i(TAG, "Client pending enrolment " + ClientURN.fromPrivateKey(privateKey).stringify());
        } else {
            Log.i(TAG, "Starting client " + ClientURN.fromPrivateKey(privateKey).stringify());
        }
        return privateKey;
    }

    private boolean initCommunicator() {
        try {
            Request base = new Request.Builder().url("http://localhost")
                    .method("GET", null).build();
            httpClient.request(base, "server.pem", verifiable -> {
                RDFX509Cert caCertificate = ConfigLib.get("CA.certificate", RDFX509Cert.class);
                RDFX509Cert serverCertificate = new RDFX509Cert(verifiable.body().string());
                communicator = ClientCommunicator.from(caCertificate, serverCertificate, getOrCreatePrivateKey());
            });
            return true;
        } catch (IOException e) {
            Log.w(TAG, e);
            timer.slowPoll();
            return false;
        }
    }

    /**
     * Send http request and decode response
     */
    private Pair<Response, RDFMessageList> sendPayload(byte[] payload, RDFDatetime nonce)
            throws IOException {
        totalBytesSent += payload.length;
        Request base = new Request.Builder()
                .url("http://localhost")
                .method("POST", RequestBody.create(MediaType.parse("binary/octet-stream"), payload))
                .build();
        Pair<Response, RDFMessageList> result = new Pair<>();
        result.first = httpClient.request(base, SERVER_API_ENDPOINT, verifiable -> {
            if (verifiable.code() == 200) {
                byte[] data = verifiable.body().bytes();
                totalBytesReceived += data.length;
                RDFClientCommunication packet = (RDFClientCommunication) new RDFClientCommunication().parse(data);
                result.second = communicator.decode(packet, nonce);
                if (!result.second.getSource().equals(communicator.getServerName())) {
                    throw new DecodeException(String.format(
                            "Received a message not from the server %s, expected %s.",
                            result.second.getSource(), communicator.getServerName()));
                }
            }
        });
        return result;
    }

    /**
     * Fetch response messages for the server
     */
    private RDFMessageList fetchReplies() {
        RDFMessageList messages = new RDFMessageList();
        if (httpClient.isNotFailing()) {
            long size = 0;
            for (RDFGrrMessage message : queue.get()) {
                messages.addJob(message);
                size += message.serialize().length;
                if (size > ConfigLib.get("Client.max_post_size", 40000000)) {
                    break;
                }
            }
        }
        return messages;
    }

    /**
     * Loop through messages to see if the any message need fast polling
     */
    private void updatePollInterval(Collection<RDFGrrMessage> messages) {
        for (RDFGrrMessage message : messages) {
            if (message.getRequireFastpoll() != null && message.getRequireFastpoll()) {
                timer.fastPoll();
                break;
            }
        }
    }

    /**
     * Requeue failed messages with high priority and low poll frequency, and drop dead messages
     */
    private void retryFailedReplies(Collection<RDFGrrMessage> messages) {
        for (RDFGrrMessage message : messages) {
            int ttl = message.getTtl() - 1;
            if (ttl > 0) {
                message.setPriority(GrrMessage.Priority.HIGH_PRIORITY);
                message.setRequireFastpoll(false);
                message.setTtl(ttl);
                try {
                    enqueue(message, GrrMessage.Priority.HIGH_PRIORITY, true);
                } catch (SizeQueue.QueueFullException ignored) {
                }
            } else {
                Log.i(TAG, "Dropped message due to retransmissions.");
            }
        }
    }

    /**
     * Sending queued messages to server and pull queued messages from it.
     * <p>
     * Steps:
     * 1. Get server certificate and initialize the communicator. Try later if fails.
     * 2. Fetch some outgoing replies from the queue and update polling interval.
     * 3. Encrypt and encode the replies, and make HTTP request.
     * 4. If client is not enrolled according to the server reply, enroll the client.
     * 5. If the HTTP request failed or server response cannot be verified, enqueue the failed
     * replies and try later.
     * 6. If the HTTP request succeed, enqueue the server response in the ReLF worker.
     */
    private void polling() throws EncryptionException, SignatureException {
        if (communicator == null && !initCommunicator()) {
            return;
        }
        RDFMessageList messages = fetchReplies();
        updatePollInterval(messages.getJobs());

        RDFDatetime nonce = RDFDatetime.now();
        RDFClientCommunication packet = communicator.encode(messages, nonce);
        if (worker.isMemoryExceeded()) {
            Log.w(TAG, "Memory exceeded, will not retrieve jobs.");
            packet.setQueueSize(1000000);
        } else {
            packet.setQueueSize(worker.getQueueSize());
        }
        try {
            long startTime = System.currentTimeMillis();
            Pair<Response, RDFMessageList> response = sendPayload(packet.serialize(), nonce);
            if (response.first.code() == 406) {
                Log.i(TAG, "sending enrollment request");
                enroller.enroll();
            } else {
                updatePollInterval(response.second.getJobs());
                for (RDFGrrMessage message : response.second.getJobs()) {
                    worker.enqueue(message);
                }
                //noinspection ConstantConditions
                Log.i(TAG, String.format(
                        "%s: Sending %s(%s), Received %s messages in %f sec. Sleeping for %s sec.",
                        communicator.getClientName().stringify(),
                        messages.getJobCount(),
                        response.first.body().contentLength(),
                        response.second.getJobCount(),
                        (System.currentTimeMillis() - startTime) / 1000.0,
                        timer.sleepTime));
            }
        } catch (IOException e) {
            communicator = null;
            retryFailedReplies(messages.getJobs());
        }
    }

    /**
     * Start the ReLF client
     */
    public void run() {
        if (httpClient.isErrorLimitReached()) {
            throw new RuntimeException("HTTP error limit is reached.");
        }
        foreman.check();
        try {
            polling();
        } catch (Exception e) {
            Log.e(TAG, "Uncaught exception.\n" + ExceptionUtils.getStackTrace(e));
        }
        if (worker.isMemoryExceeded() && !worker.isActive() && worker.getQueueSize() == 0
                && queue.size() == 0) {
            try {
                Log.e(TAG, "Memory exceeded - exiting.");
                worker.alert("Memory limit exceeded, exiting.");
                worker.forceMemoryExceeded();
                polling();
            } catch (EncryptionException | SignatureException ignored) {
            }
            throw new MemoryExceededException();
        }
    }

    /**
     * A helper container class for passing two elements at the same time
     *
     * @param <U> type of the first element
     * @param <V> type of the second element
     */
    private class Pair<U, V> {
        private U first;
        private V second;
    }

    /**
     * A timer for controlling communication frequency
     */
    public class Timer {
        private static final float PULL_SLEW = 1.15F;
        private final float minPollInterval;
        private final float maxPollInterval;
        private float sleepTime;

        Timer() {
            this.minPollInterval = ConfigLib.get("Client.poll_min", 1F);
            this.maxPollInterval = ConfigLib.get("Client.poll_max", 60F);
            this.sleepTime = this.minPollInterval;
        }

        /**
         * Speed up polling by setting the interval to the minimal.
         */
        void fastPoll() {
            sleepTime = minPollInterval;
        }

        /**
         * Slow down polling by setting the interval to the maximal.
         */
        void slowPoll() {
            sleepTime = maxPollInterval;
        }

        /**
         * @return the delay till next polling.
         */
        public float getNextDelay() {
            sleepTime = Math.min(maxPollInterval, Math.max(minPollInterval, sleepTime) * PULL_SLEW);
            return sleepTime;
        }
    }

    /**
     * Foreman checker
     */
    private class Foreman {
        private long interval;
        private long timestamp;

        Foreman() {
            interval = ConfigLib.get("Client.foreman_check_frequency", 1800);
        }

        /**
         * Check server foreman to see if there are any pending jobs
         */
        void check() {
            long now = System.currentTimeMillis();
            if (now > timestamp + interval) {
                RDFGrrMessage reply = new RDFGrrMessage();
                reply.setRDFValue(new RDFDataBlob());
                reply.setSessionId(SessionID.fromFlow("Foreman"));
                reply.setPriority(GrrMessage.Priority.LOW_PRIORITY);
                reply.setRequireFastpoll(false);
                worker.reply(reply, false);
                timestamp = now;
            }
        }
    }

    /**
     * Client enrollment helper
     */
    private class Enroller {
        private long timestamp;
        private long interval = 10 * 60;

        void enroll() {
            long now = System.currentTimeMillis();
            if (now > timestamp + interval) {
                if (timestamp == 0) {
                    timer.fastPoll();
                }

                CertificateSigningRequest csr = new CertificateSigningRequest(
                        communicator.getClientName(),
                        communicator.getLocalPrivateKey());
                RDFCertificate certificate = new RDFCertificate();
                certificate.setType(Certificate.Type.CSR);
                certificate.setPem(csr.stringify().getBytes());

                RDFGrrMessage reply = new RDFGrrMessage();
                reply.setRDFValue(certificate);
                reply.setSessionId(SessionID.fromFlow(new RDFURN("E"), "Enrol"));
                worker.reply(reply, true);

                timestamp = now;
            }
        }
    }
}
