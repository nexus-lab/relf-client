package org.nexus_lab.relf.lib.rdfvalues.client;

import org.nexus_lab.relf.lib.rdfvalues.protodict.RDFDict;
import org.nexus_lab.relf.lib.rdfvalues.structs.RDFProtoStruct;
import org.nexus_lab.relf.proto.Iterator;

import net.badata.protobuf.converter.annotation.ProtoClass;
import net.badata.protobuf.converter.annotation.ProtoField;
import net.badata.protobuf.converter.mapping.Proto2MapperImpl;

import lombok.Getter;
import lombok.Setter;

/**
 * RDF wrapper of {@link Iterator}
 *
 * @author Ruipeng Zhang
 */
@ProtoClass(value = Iterator.class, mapper = Proto2MapperImpl.class)
public class RDFIterator extends RDFProtoStruct<Iterator> {
    @Getter
    @Setter
    @ProtoField
    private RDFDict clientState;
    @Getter
    @Setter
    @ProtoField
    private Integer skip = 0;
    @Getter
    @Setter
    @ProtoField
    private Integer number = 100;
    @Getter
    @Setter
    @ProtoField
    private Iterator.State state;
}
