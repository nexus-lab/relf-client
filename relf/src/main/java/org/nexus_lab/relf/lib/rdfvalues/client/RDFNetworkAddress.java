package org.nexus_lab.relf.lib.rdfvalues.client;

import org.nexus_lab.relf.lib.rdfvalues.RDFBytes;
import org.nexus_lab.relf.lib.rdfvalues.structs.RDFProtoStruct;
import org.nexus_lab.relf.proto.NetworkAddress;

import net.badata.protobuf.converter.annotation.ProtoClass;
import net.badata.protobuf.converter.annotation.ProtoField;
import net.badata.protobuf.converter.mapping.Proto2MapperImpl;

import lombok.Getter;
import lombok.Setter;

/**
 * RDF wrapper of {@link NetworkAddress}
 *
 * @author Ruipeng Zhang
 */
@ProtoClass(value = NetworkAddress.class, mapper = Proto2MapperImpl.class)
public class RDFNetworkAddress extends RDFProtoStruct<NetworkAddress> {
    @Getter
    @Setter
    @ProtoField
    private NetworkAddress.Family addressType;
    @Getter
    @Setter
    @ProtoField
    private String humanReadable;
    @Getter
    @Setter
    @ProtoField(converter = RDFBytes.Converter.class)
    private RDFBytes packedBytes;
}
