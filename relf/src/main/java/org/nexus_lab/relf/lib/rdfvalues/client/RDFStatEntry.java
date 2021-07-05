package org.nexus_lab.relf.lib.rdfvalues.client;

import org.nexus_lab.relf.lib.rdfvalues.RDFDatetimeSeconds;
import org.nexus_lab.relf.lib.rdfvalues.paths.RDFPathSpec;
import org.nexus_lab.relf.lib.rdfvalues.structs.RDFProtoStruct;
import org.nexus_lab.relf.proto.StatEntry;

import net.badata.protobuf.converter.annotation.ProtoClass;
import net.badata.protobuf.converter.annotation.ProtoField;
import net.badata.protobuf.converter.mapping.Proto2MapperImpl;
import net.badata.protobuf.converter.type.ByteStringConverterImpl;

import lombok.Getter;
import lombok.Setter;

/**
 * RDF wrapper of {@link StatEntry}
 *
 * @author Ruipeng Zhang
 */
@ProtoClass(value = StatEntry.class, mapper = Proto2MapperImpl.class)
public class RDFStatEntry extends RDFProtoStruct<StatEntry> {
    @Getter
    @Setter
    @ProtoField(converter = StatMode.Converter.class)
    private StatMode stMode;
    @Getter
    @Setter
    @ProtoField
    private Integer stIno;
    @Getter
    @Setter
    @ProtoField
    private Integer stDev;
    @Getter
    @Setter
    @ProtoField
    private Integer stNlink;
    @Getter
    @Setter
    @ProtoField
    private Integer stUid;
    @Getter
    @Setter
    @ProtoField
    private Integer stGid;
    @Getter
    @Setter
    @ProtoField
    private Long stSize;
    @Getter
    @Setter
    @ProtoField(converter = RDFDatetimeSeconds.Converter.class)
    private RDFDatetimeSeconds stAtime;
    @Getter
    @Setter
    @ProtoField(converter = RDFDatetimeSeconds.Converter.class)
    private RDFDatetimeSeconds stMtime;
    @Getter
    @Setter
    @ProtoField(converter = RDFDatetimeSeconds.Converter.class)
    private RDFDatetimeSeconds stCtime;
    @Getter
    @Setter
    @ProtoField
    private Integer stBlocks;
    @Getter
    @Setter
    @ProtoField
    private Integer stBlksize;
    @Getter
    @Setter
    @ProtoField
    private Integer stRdev;
    @Getter
    @Setter
    @ProtoField
    private String symlink;
    @Getter
    @Setter
    @ProtoField(converter = ByteStringConverterImpl.class)
    private byte[] resident;
    @Getter
    @Setter
    @ProtoField
    private RDFPathSpec pathspec;
    @Getter
    @Setter
    @ProtoField(converter = RDFDatetimeSeconds.Converter.class)
    private RDFDatetimeSeconds stCrtime;
}
