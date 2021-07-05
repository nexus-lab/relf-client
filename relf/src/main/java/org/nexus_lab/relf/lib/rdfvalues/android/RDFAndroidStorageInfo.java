package org.nexus_lab.relf.lib.rdfvalues.android;

import org.nexus_lab.relf.lib.rdfvalues.ByteSize;
import org.nexus_lab.relf.lib.rdfvalues.structs.RDFProtoStruct;
import org.nexus_lab.relf.proto.AndroidStorageInfo;

import net.badata.protobuf.converter.annotation.ProtoClass;
import net.badata.protobuf.converter.annotation.ProtoField;
import net.badata.protobuf.converter.mapping.Proto2MapperImpl;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Ruipeng Zhang
 */
@ProtoClass(value = AndroidStorageInfo.class, mapper = Proto2MapperImpl.class)
public class RDFAndroidStorageInfo extends RDFProtoStruct<AndroidStorageInfo> {
    @Getter
    @Setter
    @ProtoField
    private String id;
    @Getter
    @Setter
    @ProtoField
    private String description;
    @Getter
    @Setter
    @ProtoField
    private String fsUuid;
    @Getter
    @Setter
    @ProtoField
    private Boolean isPrimary;
    @Getter
    @Setter
    @ProtoField
    private Boolean isEmulated;
    @Getter
    @Setter
    @ProtoField
    private Boolean isRemovable;
    @Getter
    @Setter
    @ProtoField
    private String path;
    @Getter
    @Setter
    @ProtoField
    private AndroidStorageInfo.StorageState state;
    @Getter
    @Setter
    @ProtoField(converter = ByteSize.Converter.class)
    private ByteSize totalSpace;
    @Getter
    @Setter
    @ProtoField(converter = ByteSize.Converter.class)
    private ByteSize usedSpace;
    @Getter
    @Setter
    @ProtoField(converter = ByteSize.Converter.class)
    private ByteSize maxFileSize;
    @Getter
    @Setter
    @ProtoField(converter = ByteSize.Converter.class)
    private ByteSize mtpReserveSize;
    @Getter
    @Setter
    @ProtoField
    private Boolean isAllowMassStorage;
}
