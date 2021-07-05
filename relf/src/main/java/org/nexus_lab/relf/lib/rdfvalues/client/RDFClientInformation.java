package org.nexus_lab.relf.lib.rdfvalues.client;

import org.nexus_lab.relf.lib.rdfvalues.structs.RDFProtoStruct;
import org.nexus_lab.relf.proto.ClientInformation;

import net.badata.protobuf.converter.annotation.ProtoClass;
import net.badata.protobuf.converter.annotation.ProtoField;
import net.badata.protobuf.converter.mapping.Proto2MapperImpl;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * RDF wrapper of {@link ClientInformation}
 *
 * @author Ruipeng Zhang
 */
@ProtoClass(value = ClientInformation.class, mapper = Proto2MapperImpl.class)
public class RDFClientInformation extends RDFProtoStruct<ClientInformation> {
    @Getter
    @Setter
    @ProtoField
    private String clientName;
    @Getter
    @Setter
    @ProtoField
    private Integer clientVersion;
    @Getter
    @Setter
    @ProtoField
    private Long revision;
    @Getter
    @Setter
    @ProtoField
    private String buildTime;
    @Getter
    @Setter
    @ProtoField
    private String clientDescription;
    @Getter
    @Setter
    @ProtoField
    private List<String> labels;

    /**
     * @param label add a client to the client
     */
    public void addLabel(String label) {
        if (this.labels == null) {
            this.labels = new ArrayList<>();
        }
        this.labels.add(label);
    }
}
