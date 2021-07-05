package org.nexus_lab.relf.client;

import android.content.Context;
import android.net.TrafficStats;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.system.Os;
import android.system.OsConstants;
import android.util.Log;

import org.nexus_lab.relf.client.actions.Action;
import org.nexus_lab.relf.client.actions.ActionCallback;
import org.nexus_lab.relf.client.actions.ActionRegistry;
import org.nexus_lab.relf.client.actions.admin.GetClientStats;
import org.nexus_lab.relf.client.actions.admin.SendStartupInfo;
import org.nexus_lab.relf.client.exceptions.CPUExceededException;
import org.nexus_lab.relf.client.exceptions.NetworkBytesExceededException;
import org.nexus_lab.relf.client.exceptions.PermissionException;
import org.nexus_lab.relf.lib.config.ConfigLib;
import org.nexus_lab.relf.lib.rdfvalues.RDFDatetime;
import org.nexus_lab.relf.lib.rdfvalues.RDFRegistry;
import org.nexus_lab.relf.lib.rdfvalues.RDFValue;
import org.nexus_lab.relf.lib.rdfvalues.SessionID;
import org.nexus_lab.relf.lib.rdfvalues.TransferStore;
import org.nexus_lab.relf.lib.rdfvalues.client.RDFClientStats;
import org.nexus_lab.relf.lib.rdfvalues.client.RDFCpuSeconds;
import org.nexus_lab.relf.lib.rdfvalues.client.RDFGetClientStatsRequest;
import org.nexus_lab.relf.lib.rdfvalues.client.RDFIterator;
import org.nexus_lab.relf.lib.rdfvalues.flows.RDFGrrMessage;
import org.nexus_lab.relf.lib.rdfvalues.flows.RDFGrrStatus;
import org.nexus_lab.relf.lib.rdfvalues.protodict.RDFDataBlob;
import org.nexus_lab.relf.proto.GrrMessage;
import org.nexus_lab.relf.proto.GrrStatus;
import org.nexus_lab.relf.utils.os.Process;
import org.nexus_lab.relf.utils.store.SizeQueue;

import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Getter;

import static org.nexus_lab.relf.utils.PermissionUtils.checkPermissions;

/**
 * Control center of outgoing messages and incoming actions
 */
public class RelfWorker extends HandlerThread {
    private static final String TAG = RelfWorker.class.getSimpleName();

    private static final int TRAFFIC_STATS_TAG = 2;
    /**
     * Network traffic statistics for each session
     */
    private final Map<SessionID, Long> sessionTraffic = new HashMap<>();
    /**
     * ReLF client process
     */
    private final Process process = Process.currentProcess();
    /**
     * Android application context.
     */
    private final Context context;
    /**
     * ReLF communication client
     */
    private final RelfClient client;
    /**
     * indicates if the worker is handling some actions
     */
    @Getter
    private boolean isActive;
    /**
     * Number of incoming messages needed to be handled
     */
    @Getter
    private int queueSize;
    /**
     * If the progress used up the memory quota
     */
    private boolean memoryExceeded;
    /**
     * A handler for handling incoming jobs
     */
    private Handler handler;
    /**
     * A temporary queue to store messages before handler initates
     */
    private List<RDFGrrMessage> tempQueue = new ArrayList<>();
    /**
     * Time when the last client stats are sent
     */
    private RDFDatetime lastStatsSentTime;
    /**
     * If true, ClientStats will be forcibly sent to server during next {@link RelfWorker#checkStats()} call, if less
     * than 60s has passed since last stats notification was sent.
     */
    private boolean sendStatsOnCheck;

    /**
     * @param context Application context
     * @param client  ReLF client
     */
    public RelfWorker(Context context, RelfClient client) {
        super(RelfWorker.class.getSimpleName());
        TrafficStats.setThreadStatsTag(TRAFFIC_STATS_TAG);
        this.context = context;
        this.client = client;
    }

    /**
     * Validate if the incoming message contains the right type of arguments for the action.
     *
     * @param action        The action to be executed.
     * @param parameterName The parameter name.
     */
    @SuppressWarnings("unchecked")
    private static void validateParameterType(Action action, String parameterName) {
        Class parameterClass = action.getRequestType();
        if (parameterName != null && parameterClass == null) {
            throw new RuntimeException("Did not expect arguments, got " + parameterName + ".");
        } else if (parameterClass != null) {
            String expectedParameter = RDFRegistry.getName(parameterClass);
            if (parameterName == null) {
                throw new RuntimeException("Expect argument " + expectedParameter + ", got null.");
            }
            if (!parameterName.equals(expectedParameter)) {
                throw new RuntimeException(String.format("Unexpected argument type %s != %s.",
                        parameterName, expectedParameter));
            }
        }
    }

    /**
     * Check if we have enough permissions to execute the action.
     *
     * @param action the action to be checked.
     */
    private void validatePermissions(Action action) {
        String[][] missingPermissions = checkPermissions(context, action.getClass());
        if (missingPermissions != null) {
            throw new PermissionException(missingPermissions);
        }
    }

    /**
     * Execute an action at the request of the server.
     *
     * @param message the action and its arguments and context data.
     */
    @SuppressWarnings("unchecked")
    private void executeJob(RDFGrrMessage message) {
        DefaultActionCallback callback = new DefaultActionCallback(message);
        try {
            queueSize--;
            isActive = true;
            Action action = ActionRegistry.create(message.getName());
            validatePermissions(action);
            validateParameterType(action, message.getArgsRdfName());
            action.execute(context, message.getRDFValue(), callback);
        } catch (CPUExceededException e) {
            alert("CPU limit exceed."); // we can continue if cpu limit exceeds.
        } catch (Exception e) {
            Log.w(message.getName(), ExceptionUtils.getStackTrace(e));
            callback.onError(e);
        }
    }

    /**
     * Update session total traffic and see if the network traffic limit exceeds
     *
     * @param sessionId target session id
     * @param size      additional traffic in bytes
     * @param limit     network traffic limit in bytes
     */
    private synchronized void chargeSessionTraffic(SessionID sessionId, long size, long limit) {
        Long traffic = sessionTraffic.get(sessionId);
        traffic = (traffic == null ? 0 : traffic) + size;
        sessionTraffic.put(sessionId, traffic);
        if (limit > 0 && traffic > limit) {
            alert("Network limit exceeded.");
            throw new NetworkBytesExceededException("Action exceeded network send limit.");
        }
    }

    /**
     * Queue incoming messages for handling as actions
     *
     * @param message incoming message from server or resend
     */
    public void enqueue(RDFGrrMessage message) {
        queueSize++;
        if (handler != null) {
            handler.obtainMessage(0, message).sendToTarget();
        } else {
            tempQueue.add(message);
        }
    }

    /**
     * Push a reply to the output queue, which will be sent to the server later
     *
     * @param response response to be pushed to server
     * @param blocking whether we should wait until the response is enqueued when queue is full
     */
    public void reply(RDFGrrMessage response, boolean blocking) {
        if (response.getSessionId() != null) {
            chargeSessionTraffic(response.getSessionId(), response.serialize().length, 0);
            if (response.getType() == GrrMessage.Type.STATUS) {
                Long traffic = sessionTraffic.get(response.getSessionId());
                if (traffic != null) {
                    RDFGrrStatus status = (RDFGrrStatus) response.getRDFValue();
                    status.setNetworkBytesSent(traffic);
                    response.setRDFValue(status);
                }
            }
        }
        try {
            client.enqueue(response, response.getPriority(), blocking);
        } catch (SizeQueue.QueueFullException e) {
            if (!blocking) {
                throw new RuntimeException(e);
            }
            Log.i(TAG, "Queue is full, dropping messages.");
        }
    }

    /**
     * Push an alert message to the input queue, which will be sent to the server later.
     *
     * @param message alert content.
     */
    public void alert(String message) {
        RDFDataBlob blob = new RDFDataBlob();
        blob.setString(message);
        RDFGrrMessage reply = new RDFGrrMessage();
        reply.setRDFValue(blob);
        reply.setSessionId(SessionID.fromFlow("ClientAlert"));
        reply.setPriority(GrrMessage.Priority.LOW_PRIORITY);
        reply.setRequireFastpoll(false);
        reply(reply, true);
    }

    /**
     * Check if the last transmission of client stats is too long ago and send stats if so.
     */
    public void checkStats() {
        if (lastStatsSentTime == null) {
            lastStatsSentTime = RDFDatetime.now();
        }

        long timeSince = RDFDatetime.now().asSecondsFromEpoch() - lastStatsSentTime.asSecondsFromEpoch();
        // send client stats at most every minute
        if (timeSince < 60) {
            return;
        }
        // send client stats at least every 50 minutes
        if (timeSince > 50 * 60 || isActive() || sendStatsOnCheck) {
            sendStatsOnCheck = false;
            Log.i(TAG, "Sending back client statistics to the server.");
            GetClientStats action = new GetClientStats();
            RDFGetClientStatsRequest request = new RDFGetClientStatsRequest();
            request.setStartTime(lastStatsSentTime);
            action.execute(context, request, new GetClientStatsCallback());
            lastStatsSentTime = RDFDatetime.now();
        }
    }

    /**
     * @return if the progress used up its memory quota
     */
    public boolean isMemoryExceeded() {
        if (memoryExceeded) {
            return true;
        }
        long vmSize = process.statm().resident() * Os.sysconf(OsConstants._SC_PAGESIZE) / 1024 / 1024 / 1024;
        return vmSize > ConfigLib.get("Client.rss_max", Long.MAX_VALUE);
    }

    /**
     * Set the worker to a memory-exceeded state
     */
    public void forceMemoryExceeded() {
        memoryExceeded = true;
    }

    /**
     * ReLF worker start up jobs
     */
    @Override
    protected void onLooperPrepared() {
        handler = new Handler(getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                executeJob((RDFGrrMessage) msg.obj);
            }
        };
        for (RDFGrrMessage message : tempQueue) {
            enqueue(message);
        }
        tempQueue.clear();
        SendStartupInfo action = (SendStartupInfo) ActionRegistry.create("SendStartupInfo");
        action.execute(context, null, new StartupActionCallback());
    }

    /**
     * {@link SendStartupInfo} action callback. Startup information will be sent to server,
     * otherwise an {@link RuntimeException} will be thrown.
     */
    private class StartupActionCallback implements ActionCallback {
        @Override
        public void onResponse(RDFValue response) {
            RDFGrrMessage reply = new RDFGrrMessage();
            reply.setRDFValue(response);
            reply.setSessionId(SendStartupInfo.WELL_KNOWN_SESSION_ID);
            reply.setRequestId(0L);
            reply.setResponseId(0L);
            reply.setPriority(GrrMessage.Priority.LOW_PRIORITY);
            reply.setType(GrrMessage.Type.MESSAGE);
            reply.setRequireFastpoll(false);
            reply.setTtl(1);
            reply(reply, true);
        }
    }

    private class GetClientStatsCallback implements ActionCallback {
        @Override
        public void onResponse(RDFValue response) {
            RDFValue rep = response;
            if (rep instanceof RDFClientStats) {
                rep = ((RDFClientStats) rep).downSample(60);
            }
            RDFGrrMessage reply = new RDFGrrMessage();
            reply.setRDFValue(rep);
            reply.setSessionId(GetClientStats.WELL_KNOWN_SESSION_ID);
            reply.setRequestId(0L);
            reply.setResponseId(0L);
            reply.setPriority(GrrMessage.Priority.LOW_PRIORITY);
            reply.setType(GrrMessage.Type.MESSAGE);
            reply.setRequireFastpoll(false);
            reply(reply, true);
        }
    }

    /**
     * Default {@link ActionCallback} for actions.
     * For every action, execution results as well as error status will be sent to the ReLF server.
     */
    private class DefaultActionCallback implements ActionCallback {
        private long lastUpdate;
        private float[] cpuStart;
        private long responseId;
        private boolean isCompleted;
        private RDFGrrMessage message;

        DefaultActionCallback(RDFGrrMessage message) {
            this.responseId = 1;
            this.message = message;
            Process.Stat stat = process.stat();
            this.cpuStart = new float[]{stat.utime(), stat.stime()};
        }

        /**
         * Check if CPU usage exceeds the limits set by the server for this action
         */
        private void checkIfCpuExceeded() {
            if (cpuStart != null) {
                Process.Stat stat = process.stat();
                float usedCpu = stat.utime() - cpuStart[0] + stat.stime() - cpuStart[1];
                if (usedCpu / Os.sysconf(OsConstants._SC_CLK_TCK) > message.getCpuLimit()) {
                    throw new CPUExceededException(
                            String.format("Action %s exceeded CPU limit.", message.getName()));
                }
            }
        }

        /**
         * Mark the action (actually it's the callback) as complete.
         * An error will be thrown if the action is already completed.
         * This means a action can not call both {@link ActionCallback#onComplete()} and {@link
         * ActionCallback#onError(Throwable)}.
         */
        private void markCompleted() {
            if (isCompleted) {
                throw new Error("The action execution is already completed! " +
                        "Cannot send the status information twice.");
            } else {
                isCompleted = true;
                isActive = false;
                // We want to send ClientStats when client action is complete.
                sendStatsOnCheck = true;
            }
        }

        /**
         * Transfer a chunk of data to the server.
         *
         * @param response the data to be transferred.
         * @param length   the data length.
         */
        private void transferStore(RDFValue response, long length) {
            RDFGrrMessage reply = new RDFGrrMessage();
            reply.setRDFValue(response);
            reply.setSessionId(SessionID.fromFlow("TransferStore"));
            long limit;
            if (message.getNetworkBytesLimit() == null) {
                limit = GrrMessage.getDefaultInstance().getNetworkBytesLimit();
            } else {
                limit = message.getNetworkBytesLimit();
            }
            chargeSessionTraffic(message.getSessionId(), length, limit);
            reply(reply, true);
        }

        /**
         * Set the CPU usage to a ReLF status message.
         *
         * @param status CPU time to execute this action.
         */
        private void setCpuStats(RDFGrrStatus status) {
            if (cpuStart == null) {
                return;
            }
            Process.Stat stat = process.stat();
            float[] cpuUsed = new float[]{stat.utime() - cpuStart[0], stat.stime() - cpuStart[1]};
            RDFCpuSeconds cpuSeconds = new RDFCpuSeconds();
            cpuSeconds.setUserCpuTime(cpuUsed[0] / (float) Os.sysconf(OsConstants._SC_CLK_TCK));
            cpuSeconds.setSystemCpuTime(cpuUsed[1] / (float) Os.sysconf(OsConstants._SC_CLK_TCK));
            status.setCpuTimeUsed(cpuSeconds);
        }

        /**
         * Wrap the raw {@link RDFValue} with {@link RDFGrrMessage} and reply to the client.
         *
         * @param response {@link RDFValue} to be sent to the server.
         */
        private void wrapAndReply(RDFValue response) {
            RDFGrrMessage reply = new RDFGrrMessage();
            if (response instanceof RDFGrrStatus) {
                reply.setType(GrrMessage.Type.STATUS);
            } else if (response instanceof RDFIterator) {
                reply.setType(GrrMessage.Type.ITERATOR);
            } else {
                reply.setType(GrrMessage.Type.MESSAGE);
            }
            reply.setRDFValue(response);
            reply.setResponseId(responseId);
            reply.setName(message.getName());
            reply.setTaskId(message.getTaskId());
            reply.setSessionId(message.getSessionId());
            reply.setRequestId(message.getRequestId());
            reply.setPriority(message.getPriority());
            reply.setRequireFastpoll(message.getRequireFastpoll());
            reply(reply, true);
            responseId++;
        }

        @Override
        public void onResponse(RDFValue response) {
            if (response instanceof TransferStore) {
                TransferStore data = (TransferStore) response;
                transferStore(data.getValue(), data.getLength());
            } else {
                wrapAndReply(response);
            }
        }

        @Override
        public void onComplete() {
            markCompleted();
            RDFGrrStatus status = new RDFGrrStatus();
            status.setStatus(GrrStatus.ReturnedStatus.OK);
            setCpuStats(status);
            wrapAndReply(status);
        }

        @Override
        public void onError(Throwable e) {
            markCompleted();
            RDFGrrStatus status = new RDFGrrStatus();
            status.setErrorMessage(e.getMessage());
            status.setBacktrace(ExceptionUtils.getStackTrace(e));
            if (e instanceof NetworkBytesExceededException) {
                status.setStatus(GrrStatus.ReturnedStatus.NETWORK_LIMIT_EXCEEDED);
            } else if (e instanceof IOException) {
                status.setStatus(GrrStatus.ReturnedStatus.IOERROR);
            } else {
                status.setStatus(GrrStatus.ReturnedStatus.GENERIC_ERROR);
            }
            setCpuStats(status);
            wrapAndReply(status);
        }

        @Override
        public void onUpdate() {
            long now = System.currentTimeMillis();
            if (now - lastUpdate <= 2 * 1000) {
                return;
            }
            lastUpdate = now;
            checkIfCpuExceeded();
//            ClientUtils.keepAlive(); TODO
        }
    }
}