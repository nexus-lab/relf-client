package org.nexus_lab.relf.lib.rdfvalues.cloud;

import org.nexus_lab.relf.lib.rdfvalues.structs.RDFProtoStruct;
import org.nexus_lab.relf.proto.CloudMetadataRequests;

import net.badata.protobuf.converter.annotation.ProtoClass;
import net.badata.protobuf.converter.annotation.ProtoField;
import net.badata.protobuf.converter.mapping.Proto2MapperImpl;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * RDF wrapper of {@link CloudMetadataRequests}
 *
 * @author Ruipeng Zhang
 */
@ProtoClass(value = CloudMetadataRequests.class, mapper = Proto2MapperImpl.class)
public class RDFCloudMetadataRequests extends RDFProtoStruct<CloudMetadataRequests> {
    @Setter
    @Getter
    @ProtoField
    private List<RDFCloudMetadataRequest> requests;
}
