package org.nexus_lab.relf.lib.rdfvalues.android;

import org.nexus_lab.relf.lib.rdfvalues.structs.RDFProtoStruct;
import org.nexus_lab.relf.proto.AndroidOsInfo;

import net.badata.protobuf.converter.annotation.ProtoClass;
import net.badata.protobuf.converter.annotation.ProtoField;
import net.badata.protobuf.converter.mapping.Proto2MapperImpl;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Ruipeng Zhang
 */
@ProtoClass(value = AndroidOsInfo.class, mapper = Proto2MapperImpl.class)
public class RDFAndroidOsInfo extends RDFProtoStruct<AndroidOsInfo> {
    @Getter
    @Setter
    @ProtoField
    private Integer version;
    @Getter
    @Setter
    @ProtoField
    private String versionName;
    @Getter
    @Setter
    @ProtoField
    private String versionCodename;
    @Getter
    @Setter
    @ProtoField
    private String versionIncremental;
    @Getter
    @Setter
    @ProtoField
    private String securityPatch;
    @Getter
    @Setter
    @ProtoField
    private String kernelVersion;
    @Getter
    @Setter
    @ProtoField
    private RDFAndroidOsBuildInfo buildInfo;
}
