package org.nexus_lab.relf.lib.rdfvalues.client;

import org.nexus_lab.relf.lib.rdfvalues.structs.RDFProtoStruct;
import org.nexus_lab.relf.proto.ClientStats;

import net.badata.protobuf.converter.annotation.ProtoClass;
import net.badata.protobuf.converter.annotation.ProtoField;
import net.badata.protobuf.converter.mapping.Proto2MapperImpl;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * RDF wrapper of {@link ClientStats}
 *
 * @author Ruipeng Zhang
 */
@ProtoClass(value = ClientStats.class, mapper = Proto2MapperImpl.class)
public class RDFClientStats extends RDFProtoStruct<ClientStats> {
    @Getter
    @Setter
    @ProtoField
    private List<RDFCpuSample> cpuSamples;
    @Getter
    @Setter
    @ProtoField(name = "RSSSize")
    private Long rssSize;
    @Getter
    @Setter
    @ProtoField(name = "VMSSize")
    private Long vmsSize;
    @Getter
    @Setter
    @ProtoField
    private Float memoryPercent;
    @Getter
    @Setter
    @ProtoField
    private Long bytesReceived;
    @Getter
    @Setter
    @ProtoField
    private Long bytesSent;
    @Getter
    @Setter
    @ProtoField
    private List<RDFIOSample> ioSamples;
    @Getter
    @Setter
    @ProtoField
    private Long createTime;
    @Getter
    @Setter
    @ProtoField
    private Long bootTime;

    /**
     * @param sample add a CPU sample to the CPU sample list
     */
    public void addCpuSample(RDFCpuSample sample) {
        if (this.cpuSamples == null) {
            this.cpuSamples = new ArrayList<>();
        }
        this.cpuSamples.add(sample);
    }

    /**
     * @param sample add an IO sample to the IO sample list
     */
    public void addIOSample(RDFIOSample sample) {
        if (this.ioSamples == null) {
            this.ioSamples = new ArrayList<>();
        }
        this.ioSamples.add(sample);
    }

    private static <T> List<T> downSampleList(List<T> samples, int interval) {
        T lastSampleSeen = null;
        long currentTimeBin = -1;
        List<T> results = new ArrayList<>();

        for (T sample : samples) {
            long timestamp;
            if (sample instanceof RDFCpuSample) {
                timestamp = ((RDFCpuSample) sample).getTimestamp().asSecondsFromEpoch();
            } else if (sample instanceof RDFIOSample) {
                timestamp = ((RDFIOSample) sample).getTimestamp().asSecondsFromEpoch();
            } else {
                throw new RuntimeException("unsupported type of sample");
            }
            long timeBin = timestamp - (timestamp % interval);
            if (currentTimeBin < 0) {
                currentTimeBin = timeBin;
                lastSampleSeen = sample;
            } else if (currentTimeBin != timeBin) {
                results.add(lastSampleSeen);
                currentTimeBin = timeBin;
                lastSampleSeen = sample;
            } else {
                if (lastSampleSeen instanceof RDFCpuSample) {
                    ((RDFCpuSample) lastSampleSeen).average((RDFCpuSample) sample);
                } else {
                    ((RDFIOSample) lastSampleSeen).average((RDFIOSample) sample);
                }
            }
        }
        if (lastSampleSeen != null) {
            results.add(lastSampleSeen);
        }

        return results;
    }

    /**
     * Down-samples the data to save space.
     *
     * @param interval The sampling interval in seconds.
     * @return New {@link ClientStats} object with cpu and IO samples down-sampled.
     */
    public RDFClientStats downSample(int interval) {
        RDFClientStats clone = new RDFClientStats();
        clone.copyFrom(this);
        clone.setCpuSamples(downSampleList(clone.cpuSamples, interval));
        clone.setIoSamples(downSampleList(clone.ioSamples, interval));
        return clone;
    }
}
