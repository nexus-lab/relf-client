package org.nexus_lab.relf.lib.rdfvalues.client;

import org.nexus_lab.relf.lib.rdfvalues.structs.RDFProtoStruct;
import org.nexus_lab.relf.proto.Volume;

import net.badata.protobuf.converter.annotation.ProtoClass;
import net.badata.protobuf.converter.annotation.ProtoField;
import net.badata.protobuf.converter.mapping.Proto2MapperImpl;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * RDF wrapper of {@link Volume}
 *
 * @author Ruipeng Zhang
 */
@ProtoClass(value = Volume.class, mapper = Proto2MapperImpl.class)
public class RDFVolume extends RDFProtoStruct<Volume> {
    @Getter
    @Setter
    @ProtoField
    private Boolean isMounted;
    @Getter
    @Setter
    @ProtoField
    private String name;
    @Getter
    @Setter
    @ProtoField
    private String devicePath;
    @Getter
    @Setter
    @ProtoField
    private String fileSystemType;
    @Getter
    @Setter
    @ProtoField
    private Long totalAllocationUnits;
    @Getter
    @Setter
    @ProtoField
    private Long sectorsPerAllocationUnit;
    @Getter
    @Setter
    @ProtoField
    private Long bytesPerSector;
    @Getter
    @Setter
    @ProtoField
    private Long actualAvailableAllocationUnits;
    @Getter
    @Setter
    @ProtoField
    private Long creationTime;
    @Getter
    @Setter
    @ProtoField
    private List<Volume.VolumeFileSystemFlagEnum> fileSystemFlagList;
    @Getter
    @Setter
    @ProtoField
    private String serialNumber;
    @Getter
    @Setter
    @ProtoField
    private RDFUnixVolume unixvolume;
}
