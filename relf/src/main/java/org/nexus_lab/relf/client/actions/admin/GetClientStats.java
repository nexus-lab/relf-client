package org.nexus_lab.relf.client.actions.admin;

import android.content.Context;
import android.os.SystemClock;
import android.system.Os;
import android.system.OsConstants;

import androidx.annotation.Nullable;

import org.nexus_lab.relf.client.ClientStatsCollector;
import org.nexus_lab.relf.client.RelfClient;
import org.nexus_lab.relf.client.actions.Action;
import org.nexus_lab.relf.client.actions.ActionCallback;
import org.nexus_lab.relf.lib.rdfvalues.RDFURN;
import org.nexus_lab.relf.lib.rdfvalues.SessionID;
import org.nexus_lab.relf.lib.rdfvalues.client.RDFClientStats;
import org.nexus_lab.relf.lib.rdfvalues.client.RDFCpuSample;
import org.nexus_lab.relf.lib.rdfvalues.client.RDFGetClientStatsRequest;
import org.nexus_lab.relf.lib.rdfvalues.client.RDFIOSample;
import org.nexus_lab.relf.utils.os.Memory;
import org.nexus_lab.relf.utils.os.Process;

/**
 * @author Ruipeng Zhang
 */
public class GetClientStats implements Action<RDFGetClientStatsRequest, RDFClientStats> {
    /**
     * Start up well-known session ID
     */
    public static final SessionID WELL_KNOWN_SESSION_ID = SessionID.fromFlow(new RDFURN("S"), "Stats");

    @Override
    public void execute(Context context,
                        @Nullable RDFGetClientStatsRequest request,
                        ActionCallback<RDFClientStats> callback) {
        RDFGetClientStatsRequest req = request;
        if (req == null) {
            req = new RDFGetClientStatsRequest();
        }

        Process process = Process.currentProcess();
        Process.Statm memoryInfo = process.statm();
        RDFClientStats response = new RDFClientStats();

        long pageSize = Os.sysconf(OsConstants._SC_PAGESIZE);

        response.setVmsSize(memoryInfo.size() * pageSize);
        response.setRssSize(memoryInfo.resident() * pageSize);
        double memoryPercent = (double) memoryInfo.resident() * pageSize / new Memory().memTotal() * 100;
        response.setMemoryPercent((float) memoryPercent);
        response.setBytesReceived(RelfClient.getTotalBytesReceived());
        response.setBytesSent(RelfClient.getTotalBytesSent());
        response.setBootTime((long) (process.stat().starttime() / Os.sysconf(OsConstants._SC_CLK_TCK) * 1E6));
        response.setBootTime(System.currentTimeMillis() - SystemClock.elapsedRealtime());

        ClientStatsCollector collector = RelfClient.getStatsCollector();
        if (collector != null) {
            for (RDFCpuSample sample : collector.getCpuSamples()) {
                long timestamp = sample.getTimestamp().asMicrosecondsFromEpoch();
                if (timestamp > req.getStartTime().asMicrosecondsFromEpoch()
                        && timestamp < req.getEndTime().asMicrosecondsFromEpoch()) {
                    response.addCpuSample(sample);
                }
            }
            for (RDFIOSample sample : collector.getIoSamples()) {
                long timestamp = sample.getTimestamp().asMicrosecondsFromEpoch();
                if (timestamp > req.getStartTime().asMicrosecondsFromEpoch()
                        && timestamp < req.getEndTime().asMicrosecondsFromEpoch()) {
                    response.addIOSample(sample);
                }
            }
        }

        callback.onResponse(response);
        callback.onComplete();
    }
}
