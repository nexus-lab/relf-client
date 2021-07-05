package org.nexus_lab.relf.lib.rdfvalues.client;

import org.nexus_lab.relf.lib.rdfvalues.RDFDatetime;
import org.nexus_lab.relf.lib.rdfvalues.structs.RDFProtoStruct;
import org.nexus_lab.relf.proto.User;

import net.badata.protobuf.converter.annotation.ProtoClass;
import net.badata.protobuf.converter.annotation.ProtoField;
import net.badata.protobuf.converter.mapping.Proto2MapperImpl;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * RDF wrapper of {@link User}
 *
 * @author Ruipeng Zhang
 */
@ProtoClass(value = User.class, mapper = Proto2MapperImpl.class)
public class RDFUser extends RDFProtoStruct<User> {
    @Getter
    @Setter
    @ProtoField
    private String username;
    @Getter
    @Setter
    @ProtoField
    private String temp;
    @Getter
    @Setter
    @ProtoField
    private String desktop;
    @Getter
    @Setter
    @ProtoField(converter = RDFDatetime.Converter.class)
    private RDFDatetime lastLogon;
    @Getter
    @Setter
    @ProtoField
    private String fullName;
    @Getter
    @Setter
    @ProtoField
    private String userdomain;
    @Getter
    @Setter
    @ProtoField
    private String sid;
    @Getter
    @Setter
    @ProtoField
    private String userprofile;
    @Getter
    @Setter
    @ProtoField
    private String appdata;
    @Getter
    @Setter
    @ProtoField
    private String localappdata;
    @Getter
    @Setter
    @ProtoField
    private String internetCache;
    @Getter
    @Setter
    @ProtoField
    private String cookies;
    @Getter
    @Setter
    @ProtoField
    private String recent;
    @Getter
    @Setter
    @ProtoField
    private String personal;
    @Getter
    @Setter
    @ProtoField
    private String startup;
    @Getter
    @Setter
    @ProtoField
    private String localappdataLow;
    @Getter
    @Setter
    @ProtoField
    private String homedir;
    @Getter
    @Setter
    @ProtoField
    private Integer uid;
    @Getter
    @Setter
    @ProtoField
    private Integer gid;
    @Getter
    @Setter
    @ProtoField
    private String shell;
    @Getter
    @Setter
    @ProtoField
    private List<Integer> gids;
}
