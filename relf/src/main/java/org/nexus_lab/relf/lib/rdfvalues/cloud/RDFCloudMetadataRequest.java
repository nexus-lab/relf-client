package org.nexus_lab.relf.lib.rdfvalues.cloud;

import org.nexus_lab.relf.lib.rdfvalues.protodict.RDFDict;
import org.nexus_lab.relf.lib.rdfvalues.structs.RDFProtoStruct;
import org.nexus_lab.relf.proto.CloudInstance;
import org.nexus_lab.relf.proto.CloudMetadataRequest;

import net.badata.protobuf.converter.annotation.ProtoClass;
import net.badata.protobuf.converter.annotation.ProtoField;
import net.badata.protobuf.converter.mapping.Proto2MapperImpl;

import lombok.Getter;
import lombok.Setter;

/**
 * RDF wrapper of {@link CloudMetadataRequest}
 *
 * @author Ruipeng Zhang
 */
@ProtoClass(value = CloudMetadataRequest.class, mapper = Proto2MapperImpl.class)
public class RDFCloudMetadataRequest extends RDFProtoStruct<CloudMetadataRequest> {
    @Getter
    @Setter
    @ProtoField
    private String biosVersionRegex;
    @Getter
    @Setter
    @ProtoField
    private String serviceNameRegex;
    @Getter
    @Setter
    @ProtoField
    private Float timeout;
    @Getter
    @Setter
    @ProtoField
    private String url;
    @Getter
    @Setter
    @ProtoField
    private RDFDict headers;
    @Getter
    @Setter
    @ProtoField
    private String label;
    @Getter
    @Setter
    @ProtoField
    private CloudInstance.InstanceType instanceType;
}
