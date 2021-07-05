package org.nexus_lab.relf.lib.rdfvalues.android;

import org.nexus_lab.relf.lib.rdfvalues.RDFDatetime;
import org.nexus_lab.relf.lib.rdfvalues.structs.RDFProtoStruct;
import org.nexus_lab.relf.proto.AndroidLocation;

import net.badata.protobuf.converter.annotation.ProtoClass;
import net.badata.protobuf.converter.annotation.ProtoField;
import net.badata.protobuf.converter.mapping.Proto2MapperImpl;

import lombok.Getter;
import lombok.Setter;

/**
 * RDF wrapper of {@link AndroidLocation}
 *
 * @author Ruipeng Zhang
 */
@ProtoClass(value = AndroidLocation.class, mapper = Proto2MapperImpl.class)
public class RDFAndroidLocation extends RDFProtoStruct<AndroidLocation> {
    @Getter
    @Setter
    @ProtoField(converter = RDFDatetime.Converter.class)
    private RDFDatetime time;
    @Getter
    @Setter
    @ProtoField
    private Double latitude;
    @Getter
    @Setter
    @ProtoField
    private Double longitude;
    @Getter
    @Setter
    @ProtoField
    private Double altitude;
    @Getter
    @Setter
    @ProtoField
    private Float horizontalAccuracy;
    @Getter
    @Setter
    @ProtoField
    private Float verticalAccuracy;
    @Getter
    @Setter
    @ProtoField
    private Float speed;
    @Getter
    @Setter
    @ProtoField
    private Float speedAccuracy;
    @Getter
    @Setter
    @ProtoField
    private Float bearing;
    @Getter
    @Setter
    @ProtoField
    private Float bearingAccuracy;
    @Getter
    @Setter
    @ProtoField
    private String provider;
    @Getter
    @Setter
    @ProtoField
    private Boolean isFromMockProvider;
    @Getter
    @Setter
    @ProtoField
    private String extras;
}
