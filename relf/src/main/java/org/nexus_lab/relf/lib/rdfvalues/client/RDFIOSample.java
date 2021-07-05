package org.nexus_lab.relf.lib.rdfvalues.client;

import org.nexus_lab.relf.lib.rdfvalues.RDFDatetime;
import org.nexus_lab.relf.lib.rdfvalues.structs.RDFProtoStruct;
import org.nexus_lab.relf.proto.IOSample;

import net.badata.protobuf.converter.annotation.ProtoClass;
import net.badata.protobuf.converter.annotation.ProtoField;
import net.badata.protobuf.converter.mapping.Proto2MapperImpl;

import lombok.Getter;
import lombok.Setter;

/**
 * RDF wrapper of {@link IOSample}
 *
 * @author Ruipeng Zhang
 */
@ProtoClass(value = IOSample.class, mapper = Proto2MapperImpl.class)
public class RDFIOSample extends RDFProtoStruct<IOSample> {
    @Getter
    @Setter
    @ProtoField
    private Long readCount;
    @Getter
    @Setter
    @ProtoField
    private Long writeCount;
    @Getter
    @Setter
    @ProtoField
    private Long readBytes;
    @Getter
    @Setter
    @ProtoField
    private Long writeBytes;
    @Getter
    @Setter
    @ProtoField(converter = RDFDatetime.Converter.class)
    private RDFDatetime timestamp;

    /**
     * Average current sample value with another sample
     *
     * @param sample sample to be average with
     */
    public void average(RDFIOSample sample) {
        timestamp = sample.timestamp;
        readBytes = sample.readBytes;
        writeBytes = sample.writeBytes;
    }
}
