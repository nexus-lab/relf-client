package org.nexus_lab.relf.lib.rdfvalues.android;

import org.nexus_lab.relf.lib.rdfvalues.structs.RDFProtoStruct;
import org.nexus_lab.relf.proto.AndroidVpnProfile;

import net.badata.protobuf.converter.annotation.ProtoClass;
import net.badata.protobuf.converter.annotation.ProtoField;
import net.badata.protobuf.converter.mapping.Proto2MapperImpl;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Ruipeng Zhang
 */
@ProtoClass(value = AndroidVpnProfile.class, mapper = Proto2MapperImpl.class)
public class RDFAndroidVpnProfile extends RDFProtoStruct<AndroidVpnProfile> {
    @Getter
    @Setter
    @ProtoField
    private String name;
    @Getter
    @Setter
    @ProtoField
    private AndroidVpnProfile.Type type;
    @Getter
    @Setter
    @ProtoField
    private String server;
    @Getter
    @Setter
    @ProtoField
    private String username;
    @Getter
    @Setter
    @ProtoField
    private String password;
    @Getter
    @Setter
    @ProtoField
    private String dnsServers;
    @Getter
    @Setter
    @ProtoField
    private String searchDomains;
    @Getter
    @Setter
    @ProtoField
    private String routes;
    @Getter
    @Setter
    @ProtoField
    private Boolean isMppe;
    @Getter
    @Setter
    @ProtoField(name = "l2TpSecret")
    private String l2tpSecret;
    @Getter
    @Setter
    @ProtoField
    private String ipsecIdentifier;
    @Getter
    @Setter
    @ProtoField
    private String ipsecSecret;
    @Getter
    @Setter
    @ProtoField
    private String ipsecUserCert;
    @Getter
    @Setter
    @ProtoField
    private String ipsecCaCert;
    @Getter
    @Setter
    @ProtoField
    private String ipsecServerCert;
}
