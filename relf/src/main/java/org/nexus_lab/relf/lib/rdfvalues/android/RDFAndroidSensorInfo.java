package org.nexus_lab.relf.lib.rdfvalues.android;

import org.nexus_lab.relf.lib.rdfvalues.structs.RDFProtoStruct;
import org.nexus_lab.relf.proto.AndroidSensorInfo;
import org.nexus_lab.relf.proto.AndroidSensorType;

import net.badata.protobuf.converter.annotation.ProtoClass;
import net.badata.protobuf.converter.annotation.ProtoField;
import net.badata.protobuf.converter.mapping.Proto2MapperImpl;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Ruipeng Zhang
 */
@ProtoClass(value = AndroidSensorInfo.class, mapper = Proto2MapperImpl.class)
public class RDFAndroidSensorInfo extends RDFProtoStruct<AndroidSensorInfo> {
    @Getter
    @Setter
    @ProtoField
    private String name;
    @Getter
    @Setter
    @ProtoField
    private AndroidSensorType type;
    @Getter
    @Setter
    @ProtoField
    private String vendor;
    @Getter
    @Setter
    @ProtoField
    private Integer version;
    @Getter
    @Setter
    @ProtoField
    private Float power;
    @Getter
    @Setter
    @ProtoField
    private Float resolution;
    @Getter
    @Setter
    @ProtoField
    private Integer minDelay;
    @Getter
    @Setter
    @ProtoField
    private Integer maxDelay;
    @Getter
    @Setter
    @ProtoField
    private Float maxRange;
    @Getter
    @Setter
    @ProtoField
    private AndroidSensorInfo.ReportingMode reportingMode;
    @Getter
    @Setter
    @ProtoField
    private Integer fifoMaxEventCount;
    @Getter
    @Setter
    @ProtoField
    private Integer fifoReservedEventCount;
    @Getter
    @Setter
    @ProtoField
    private AndroidSensorInfo.ReportingRateLevel highestDirectReportRateLevel;
}
