package org.nexus_lab.relf.lib.rdfvalues.android;

import org.nexus_lab.relf.lib.rdfvalues.structs.RDFProtoStruct;
import org.nexus_lab.relf.proto.AndroidWiFiConfiguration;
import org.nexus_lab.relf.proto.AndroidWiFiConfiguration.AuthAlgorithm;
import org.nexus_lab.relf.proto.AndroidWiFiConfiguration.GroupCipher;
import org.nexus_lab.relf.proto.AndroidWiFiConfiguration.KeyManagement;
import org.nexus_lab.relf.proto.AndroidWiFiConfiguration.PairwiseCipher;
import org.nexus_lab.relf.proto.AndroidWiFiConfiguration.Protocol;

import net.badata.protobuf.converter.annotation.ProtoClass;
import net.badata.protobuf.converter.annotation.ProtoField;
import net.badata.protobuf.converter.mapping.Proto2MapperImpl;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Ruipeng Zhang
 */
@ProtoClass(value = AndroidWiFiConfiguration.class, mapper = Proto2MapperImpl.class)
public class RDFAndroidWiFiConfiguration extends RDFProtoStruct<AndroidWiFiConfiguration> {
    @Getter
    @Setter
    @ProtoField
    private AndroidWiFiConfiguration.Status status;
    @Getter
    @Setter
    @ProtoField
    private String ssid;
    @Getter
    @Setter
    @ProtoField
    private String bssid;
    @Getter
    @Setter
    @ProtoField
    private Boolean isHidden;
    @Getter
    @Setter
    @ProtoField
    private Integer networkId;
    @Getter
    @Setter
    @ProtoField
    private List<AuthAlgorithm> allowedAuthAlgorithms;
    @Getter
    @Setter
    @ProtoField
    private List<GroupCipher> allowedGroupCiphers;
    @Getter
    @Setter
    @ProtoField
    private List<KeyManagement> allowedKeyManagement;
    @Getter
    @Setter
    @ProtoField
    private List<PairwiseCipher> allowedPairwiseCiphers;
    @Getter
    @Setter
    @ProtoField
    private List<Protocol> allowedProtocols;
}
