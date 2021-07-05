package org.nexus_lab.relf.lib.rdfvalues.flows;

import org.nexus_lab.relf.lib.rdfvalues.RDFURN;
import org.nexus_lab.relf.lib.rdfvalues.structs.RDFProtoStruct;
import org.nexus_lab.relf.proto.MessageList;

import net.badata.protobuf.converter.annotation.ProtoClass;
import net.badata.protobuf.converter.annotation.ProtoField;
import net.badata.protobuf.converter.mapping.Proto2MapperImpl;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * RDF wrapper of {@link MessageList}
 *
 * @author Ruipeng Zhang
 */
@ProtoClass(value = MessageList.class, mapper = Proto2MapperImpl.class)
public class RDFMessageList extends RDFProtoStruct<MessageList> {
    @Getter
    @Setter
    private RDFURN source;
    @Getter
    @Setter
    @ProtoField(name = "job")
    private List<RDFGrrMessage> jobs = new ArrayList<>();

    /**
     * @param job a {@link RDFGrrMessage} describing the job
     */
    public void addJob(RDFGrrMessage job) {
        if (this.jobs == null) {
            this.jobs = new ArrayList<>();
        }
        this.jobs.add(job);
    }

    /**
     * @return number of jobs
     */
    public int getJobCount() {
        return this.jobs == null ? 0 : this.jobs.size();
    }
}
