package org.nexus_lab.relf.lib.rdfvalues.android;

import org.nexus_lab.relf.lib.rdfvalues.RDFDatetime;
import org.nexus_lab.relf.lib.rdfvalues.structs.RDFProtoStruct;
import org.nexus_lab.relf.proto.AndroidCellInfo;

import net.badata.protobuf.converter.annotation.ProtoClass;
import net.badata.protobuf.converter.annotation.ProtoField;
import net.badata.protobuf.converter.mapping.Proto2MapperImpl;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Ruipeng Zhang
 */
@ProtoClass(value = AndroidCellInfo.class, mapper = Proto2MapperImpl.class)
public class RDFAndroidCellInfo extends RDFProtoStruct<AndroidCellInfo> {
    @Getter
    @Setter
    @ProtoField
    private AndroidCellInfo.CellType type;
    @Getter
    @Setter
    @ProtoField
    private AndroidCellInfo.ConnectionStatus status;
    @Getter
    @Setter
    @ProtoField(converter = RDFDatetime.Converter.class)
    private RDFDatetime timestamp;
    @Getter
    @Setter
    @ProtoField
    private Boolean isRegistered;
    @Getter
    @Setter
    @ProtoField
    private Integer level;
    @Getter
    @Setter
    @ProtoField
    private String identity;
    @Getter
    @Setter
    @ProtoField
    private String signalStrength;
}
