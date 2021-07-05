package org.nexus_lab.relf.client.actions.standard;

import android.content.Context;
import android.os.SystemClock;
import android.system.Os;
import android.system.OsConstants;
import android.util.Pair;

import org.nexus_lab.relf.client.actions.Action;
import org.nexus_lab.relf.client.actions.ActionCallback;
import org.nexus_lab.relf.client.actions.network.Netstat;
import org.nexus_lab.relf.lib.rdfvalues.RDFNull;
import org.nexus_lab.relf.lib.rdfvalues.client.RDFNetworkConnection;
import org.nexus_lab.relf.lib.rdfvalues.client.RDFProcess;
import org.nexus_lab.relf.utils.os.Process;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Ruipeng Zhang
 */
public class ListProcesses implements Action<RDFNull, RDFProcess> {
    private static final Map<String, String> PROCESS_STATUS = new HashMap<>();

    static {
        PROCESS_STATUS.put("R", "runnin");
        PROCESS_STATUS.put("S", "sleeping");
        PROCESS_STATUS.put("D", "disk-sleep");
        PROCESS_STATUS.put("Z", "zombie");
        PROCESS_STATUS.put("T", "stopped");
        PROCESS_STATUS.put("t", "tracing-stop");
        PROCESS_STATUS.put("X", "dead");
        PROCESS_STATUS.put("x", "dead");
        PROCESS_STATUS.put("K", "wake-kill");
        PROCESS_STATUS.put("W", "waking");
        PROCESS_STATUS.put("P", "parked");
    }

    /**
     * Create the {@link RDFProcess} result of the given process.
     *
     * @param process process to be checked.
     * @return the {@link RDFProcess} result containing detailed information about the process.
     */
    RDFProcess createRDFProcess(Process process) {
        RDFProcess result = new RDFProcess();
        result.setPid(process.pid());
        result.setName(process.name());
        result.setExe(process.exe());
        result.setUsername(process.username());
        result.setTerminal(process.terminal());
        result.setCwd(process.cwd());
        for (String cmdline : process.cmdline()) {
            result.addCmdline(cmdline);
        }

        long clockTick = Os.sysconf(OsConstants._SC_CLK_TCK);
        long pageSize = Os.sysconf(OsConstants._SC_PAGESIZE);

        Process.Stat stat = process.stat();
        result.setPpid(stat.ppid() > -1 ? stat.ppid() : null);
        result.setNice(stat.nice() != Integer.MIN_VALUE ? stat.nice() : null);
        if (stat.starttime() > -1) {
            result.setCtime(SystemClock.elapsedRealtime() + (long) (stat.starttime() / clockTick * 1e6));
        }
        String processStatus = stat.status();
        if (processStatus != null && PROCESS_STATUS.get(processStatus) != null) {
            result.setStatus(processStatus);
        } else {
            result.setStatus("?");
        }
        result.setNumThreads(stat.num_threads() > -1 ? stat.num_threads() : null);
        result.setSystemCpuTime(stat.stime() > -1 ? stat.stime() / (float) clockTick : null);
        result.setUserCpuTime(stat.utime() > -1 ? stat.utime() / (float) clockTick : null);

        Process.Status status = process.status();
        result.setUids(status.uids());
        result.setGids(status.gids());

        Process.Statm statm = process.statm();
        result.setVmsSize(statm.size() > -1 ? statm.size() * pageSize : null);
        result.setRssSize(statm.resident() > -1 ? statm.resident() * pageSize : null);

        for (Pair<Integer, String> fd : process.fd()) {
            result.addOpenFile(fd.second);
        }

        List<RDFNetworkConnection> connections = Netstat.getInetConnections(process);
        result.setConnections(connections);
        return result;
    }

    @Override
    public void execute(Context context, RDFNull request, ActionCallback<RDFProcess> callback) {
        Process[] processes = Process.allProcesses();
        for (Process process : processes) {
            callback.onResponse(createRDFProcess(process));
            callback.onUpdate();
        }
        callback.onComplete();
    }
}
