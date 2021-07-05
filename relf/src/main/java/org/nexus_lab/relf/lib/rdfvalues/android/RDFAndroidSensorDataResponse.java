package org.nexus_lab.relf.lib.rdfvalues.android;

import org.nexus_lab.relf.lib.rdfvalues.RDFDatetime;
import org.nexus_lab.relf.lib.rdfvalues.structs.RDFProtoStruct;
import org.nexus_lab.relf.proto.AndroidSensorDataResponse;
import org.nexus_lab.relf.proto.AndroidSensorType;

import net.badata.protobuf.converter.annotation.ProtoClass;
import net.badata.protobuf.converter.annotation.ProtoField;
import net.badata.protobuf.converter.mapping.Proto2MapperImpl;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Ruipeng Zhang
 */
@ProtoClass(value = AndroidSensorDataResponse.class, mapper = Proto2MapperImpl.class)
public class RDFAndroidSensorDataResponse extends RDFProtoStruct<AndroidSensorDataResponse> {
    @Getter
    @Setter
    @ProtoField
    private AndroidSensorType type;
    @Getter
    @Setter
    @ProtoField(converter = RDFDatetime.Converter.class)
    private RDFDatetime startTime;
    @Getter
    @Setter
    @ProtoField
    private List<RDFAndroidSensorData> data;
}
