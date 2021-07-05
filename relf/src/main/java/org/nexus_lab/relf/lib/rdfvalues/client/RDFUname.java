package org.nexus_lab.relf.lib.rdfvalues.client;

import org.nexus_lab.relf.lib.rdfvalues.RDFDatetime;
import org.nexus_lab.relf.lib.rdfvalues.structs.RDFProtoStruct;
import org.nexus_lab.relf.proto.Uname;

import net.badata.protobuf.converter.annotation.ProtoClass;
import net.badata.protobuf.converter.annotation.ProtoField;
import net.badata.protobuf.converter.mapping.Proto2MapperImpl;

import lombok.Getter;
import lombok.Setter;

/**
 * RDF wrapper of {@link Uname}
 *
 * @author Ruipeng Zhang
 */
@ProtoClass(value = Uname.class, mapper = Proto2MapperImpl.class)
public class RDFUname extends RDFProtoStruct<Uname> {
    @Getter
    @Setter
    @ProtoField
    private String system;
    @Getter
    @Setter
    @ProtoField
    private String node;
    @Getter
    @Setter
    @ProtoField
    private String release;
    @Getter
    @Setter
    @ProtoField
    private String version;
    @Getter
    @Setter
    @ProtoField
    private String machine;
    @Getter
    @Setter
    @ProtoField
    private String kernel;
    @Getter
    @Setter
    @ProtoField
    private String fqdn;
    @Getter
    @Setter
    @ProtoField(converter = RDFDatetime.Converter.class)
    private RDFDatetime installDate;
    @Getter
    @Setter
    @ProtoField
    private String libcVer;
    @Getter
    @Setter
    @ProtoField
    private String architecture;
    @Getter
    @Setter
    @ProtoField
    private String pep425Tag;
}
