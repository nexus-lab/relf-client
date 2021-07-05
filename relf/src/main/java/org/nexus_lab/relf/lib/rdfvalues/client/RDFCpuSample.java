package org.nexus_lab.relf.lib.rdfvalues.client;

import org.nexus_lab.relf.lib.rdfvalues.RDFDatetime;
import org.nexus_lab.relf.lib.rdfvalues.structs.RDFProtoStruct;
import org.nexus_lab.relf.proto.CpuSample;

import net.badata.protobuf.converter.annotation.ProtoClass;
import net.badata.protobuf.converter.annotation.ProtoField;
import net.badata.protobuf.converter.mapping.Proto2MapperImpl;

import lombok.Getter;
import lombok.Setter;

/**
 * RDF wrapper of {@link org.nexus_lab.relf.proto.CpuSample}
 *
 * @author Ruipeng Zhang
 */
@ProtoClass(value = CpuSample.class, mapper = Proto2MapperImpl.class)
public class RDFCpuSample extends RDFProtoStruct<CpuSample> {
    @Getter
    @Setter
    @ProtoField
    private Float userCpuTime;
    @Getter
    @Setter
    @ProtoField
    private Float systemCpuTime;
    @Getter
    @Setter
    @ProtoField
    private Float cpuPercent;
    @Getter
    @Setter
    @ProtoField(converter = RDFDatetime.Converter.class)
    private RDFDatetime timestamp;

    private int totalSamples = 1;

    /**
     * Average current sample value with another sample
     *
     * @param sample sample to be average with
     */
    public void average(RDFCpuSample sample) {
        timestamp = sample.timestamp;
        userCpuTime = sample.userCpuTime;
        systemCpuTime = sample.systemCpuTime;

        cpuPercent = (cpuPercent * totalSamples + sample.cpuPercent) / (totalSamples + 1);
        totalSamples++;
    }
}
