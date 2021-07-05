package org.nexus_lab.relf.lib.rdfvalues.client;

import org.nexus_lab.relf.lib.rdfvalues.structs.RDFProtoStruct;
import org.nexus_lab.relf.proto.Interface;

import net.badata.protobuf.converter.annotation.ProtoClass;
import net.badata.protobuf.converter.annotation.ProtoField;
import net.badata.protobuf.converter.mapping.Proto2MapperImpl;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * RDF wrapper of {@link Interface}
 *
 * @author Ruipeng Zhang
 */
@ProtoClass(value = Interface.class, mapper = Proto2MapperImpl.class)
public class RDFInterface extends RDFProtoStruct<Interface> {
    @Getter
    @Setter
    @ProtoField(converter = MacAddress.Converter.class)
    private MacAddress macAddress;
    @Getter
    @Setter
    @ProtoField
    private String ifname;
    @Getter
    @Setter
    @ProtoField
    private List<RDFNetworkAddress> addresses;
    @Getter
    @Setter
    @ProtoField
    private Long dhcpLeaseExpires;
    @Getter
    @Setter
    @ProtoField
    private Long dhcpLeaseObtained;
    @Getter
    @Setter
    @ProtoField
    private List<RDFNetworkAddress> dhcpServerList;
    @Getter
    @Setter
    @ProtoField
    private List<RDFNetworkAddress> ipGatewayList;

    /**
     * @param address add a network address
     */
    public void addAddress(RDFNetworkAddress address) {
        if (this.addresses == null) {
            this.addresses = new ArrayList<>();
        }
        this.addresses.add(address);
    }
}
