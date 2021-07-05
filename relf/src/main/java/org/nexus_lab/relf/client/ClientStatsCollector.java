package org.nexus_lab.relf.client;

import android.system.Os;
import android.system.OsConstants;

import org.nexus_lab.relf.lib.rdfvalues.RDFDatetime;
import org.nexus_lab.relf.lib.rdfvalues.client.RDFCpuSample;
import org.nexus_lab.relf.lib.rdfvalues.client.RDFIOSample;
import org.nexus_lab.relf.utils.os.Process;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;

/**
 * A thread for collecting client CPU and IO usage
 *
 * @author Ruipeng Zhang
 */
public class ClientStatsCollector extends Thread {
    private final int sleepTime;
    private final RelfWorker worker;
    private final Process process;
    @Getter
    private final List<RDFCpuSample> cpuSamples;
    @Getter
    private final List<RDFIOSample> ioSamples;

    private double lastSysCpuTimes = -1;
    private double[] lastProcCpuTimes;

    /**
     * @param worker    ReLF worker
     * @param sleepTime collection interval in seconds
     */
    public ClientStatsCollector(RelfWorker worker, int sleepTime) {
        super();
        this.worker = worker;
        this.sleepTime = sleepTime;
        this.process = Process.currentProcess();
        this.cpuSamples = new ArrayList<>();
        this.ioSamples = new ArrayList<>();
    }

    /**
     * @return Return a float representing the current process CPU
     * utilization as a percentage.
     */
    private float getCpuPercent() {
        Process.Stat stat = process.stat();
        int processors = Runtime.getRuntime().availableProcessors();

        double st1 = lastSysCpuTimes;
        double[] pt1 = lastProcCpuTimes;
        double st2 = System.currentTimeMillis() * processors / 1E3;
        double[] pt2 = new double[]{stat.stime(), stat.utime()};

        if (st1 == -1 || pt1 == null) {
            lastSysCpuTimes = st2;
            lastProcCpuTimes = pt2;
            return 0;
        }

        double deltaPt = (pt2[1] - pt1[1]) + (pt2[0] - pt1[0]);
        double deltaSt = st2 - st1;
        lastSysCpuTimes = st2;
        lastProcCpuTimes = pt2;

        if (deltaSt <= 0) {
            return 0;
        }

        return Math.round(deltaPt / deltaSt * 100 * processors);
    }

    private void collect() {
        Process.Stat stat = process.stat();
        Process.IO io = process.io();
        int keep = 3600 / this.sleepTime;

        RDFCpuSample cpuSample = new RDFCpuSample();
        cpuSample.setSystemCpuTime(stat.stime() / (float) Os.sysconf(OsConstants._SC_CLK_TCK));
        cpuSample.setUserCpuTime(stat.utime() / (float) Os.sysconf(OsConstants._SC_CLK_TCK));
        cpuSample.setTimestamp(RDFDatetime.now());
        cpuSample.setCpuPercent(getCpuPercent());
        cpuSamples.add(cpuSample);

        if (cpuSamples.size() > keep) {
            cpuSamples.subList(0, cpuSamples.size() - keep).clear();
        }

        if (io.rchar() > -1) {
            RDFIOSample ioSample = new RDFIOSample();
            ioSample.setReadBytes(io.read_bytes());
            ioSample.setWriteBytes(io.write_bytes());
            ioSample.setReadCount(io.syscr());
            ioSample.setWriteCount(io.syscw());
            ioSample.setTimestamp(RDFDatetime.now());
            ioSamples.add(ioSample);

            if (ioSamples.size() > keep) {
                ioSamples.subList(0, ioSamples.size() - keep).clear();
            }
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                sleep(sleepTime * 1000);
                collect();
                worker.checkStats();
            } catch (InterruptedException e) {
                break;
            }
        }
    }
}
