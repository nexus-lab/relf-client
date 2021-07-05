package org.nexus_lab.relf.lib.rdfvalues.android;

import org.nexus_lab.relf.lib.rdfvalues.structs.RDFProtoStruct;
import org.nexus_lab.relf.proto.AndroidOsBuildInfo;

import net.badata.protobuf.converter.annotation.ProtoClass;
import net.badata.protobuf.converter.annotation.ProtoField;
import net.badata.protobuf.converter.mapping.Proto2MapperImpl;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Ruipeng Zhang
 */
@ProtoClass(value = AndroidOsBuildInfo.class, mapper = Proto2MapperImpl.class)
public class RDFAndroidOsBuildInfo extends RDFProtoStruct<AndroidOsBuildInfo> {
    @Getter
    @Setter
    @ProtoField
    private String id;
    @Getter
    @Setter
    @ProtoField
    private String number;
    @Getter
    @Setter
    @ProtoField
    private AndroidOsBuildInfo.BuildType type;
    @Getter
    @Setter
    @ProtoField
    private String user;
    @Getter
    @Setter
    @ProtoField
    private Long time;
    @Getter
    @Setter
    @ProtoField
    private String host;
    @Getter
    @Setter
    @ProtoField
    private String fingerprint;
    @Getter
    @Setter
    @ProtoField
    private List<String> tags;
}
