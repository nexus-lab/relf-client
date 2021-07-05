package org.nexus_lab.relf.lib.rdfvalues.client;

import org.nexus_lab.relf.lib.rdfvalues.protodict.RDFAttributedDict;
import org.nexus_lab.relf.lib.rdfvalues.structs.RDFProtoStruct;
import org.nexus_lab.relf.proto.Filesystem;

import net.badata.protobuf.converter.annotation.ProtoClass;
import net.badata.protobuf.converter.annotation.ProtoField;
import net.badata.protobuf.converter.mapping.Proto2MapperImpl;

import lombok.Getter;
import lombok.Setter;

/**
 * RDF wrapper of {@link Filesystem}
 *
 * @author Ruipeng Zhang
 */
@ProtoClass(value = Filesystem.class, mapper = Proto2MapperImpl.class)
public class RDFFilesystem extends RDFProtoStruct<Filesystem> {
    @Getter
    @Setter
    @ProtoField
    private String device;
    @Getter
    @Setter
    @ProtoField
    private String mountPoint;
    @Getter
    @Setter
    @ProtoField
    private String type;
    @Getter
    @Setter
    @ProtoField
    private String label;
    @Getter
    @Setter
    @ProtoField
    private RDFAttributedDict options;
}
