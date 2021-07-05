package org.nexus_lab.relf.lib.rdfvalues.android;

import org.nexus_lab.relf.lib.rdfvalues.RDFDatetimeSeconds;
import org.nexus_lab.relf.lib.rdfvalues.client.RDFNetworkAddress;
import org.nexus_lab.relf.lib.rdfvalues.structs.RDFProtoStruct;
import org.nexus_lab.relf.proto.AndroidDhcpInfo;

import net.badata.protobuf.converter.annotation.ProtoClass;
import net.badata.protobuf.converter.annotation.ProtoField;
import net.badata.protobuf.converter.mapping.Proto2MapperImpl;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Ruipeng Zhang
 */
@ProtoClass(value = AndroidDhcpInfo.class, mapper = Proto2MapperImpl.class)
public class RDFAndroidDhcpInfo extends RDFProtoStruct<AndroidDhcpInfo> {
    @Getter
    @Setter
    @ProtoField
    private RDFNetworkAddress ipAddress;
    @Getter
    @Setter
    @ProtoField
    private RDFNetworkAddress gateway;
    @Getter
    @Setter
    @ProtoField
    private RDFNetworkAddress netmask;
    @Getter
    @Setter
    @ProtoField
    private List<RDFNetworkAddress> dns;
    @Getter
    @Setter
    @ProtoField
    private RDFNetworkAddress serverAddress;
    @Getter
    @Setter
    @ProtoField(converter = RDFDatetimeSeconds.Converter.class)
    private RDFDatetimeSeconds leaseDuration;

    /**
     * @param address IP address of the DNS server to be added
     */
    public void addDns(RDFNetworkAddress address) {
        if (dns == null) {
            dns = new ArrayList<>();
        }
        dns.add(address);
    }
}
