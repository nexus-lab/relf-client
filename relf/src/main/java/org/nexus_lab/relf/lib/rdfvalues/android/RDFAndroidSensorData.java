package org.nexus_lab.relf.lib.rdfvalues.android;

import org.nexus_lab.relf.lib.rdfvalues.structs.RDFProtoStruct;
import org.nexus_lab.relf.proto.AndroidSensorData;

import net.badata.protobuf.converter.annotation.ProtoClass;
import net.badata.protobuf.converter.annotation.ProtoField;
import net.badata.protobuf.converter.mapping.Proto2MapperImpl;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Ruipeng Zhang
 */
@ProtoClass(value = AndroidSensorData.class, mapper = Proto2MapperImpl.class)
public class RDFAndroidSensorData extends RDFProtoStruct<AndroidSensorData> {
    @Getter
    @Setter
    @ProtoField
    private AndroidSensorData.Accuracy accuracy;
    @Getter
    @Setter
    @ProtoField
    private Long timestamp;
    @Getter
    @Setter
    @ProtoField
    private List<Float> values;
}
