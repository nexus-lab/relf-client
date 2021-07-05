package org.nexus_lab.relf.lib.rdfvalues.crypto;

import org.nexus_lab.relf.lib.rdfvalues.HashDigest;
import org.nexus_lab.relf.lib.rdfvalues.standard.RDFAuthenticodeSignedData;
import org.nexus_lab.relf.lib.rdfvalues.structs.RDFProtoStruct;
import org.nexus_lab.relf.proto.Hash;

import net.badata.protobuf.converter.annotation.ProtoClass;
import net.badata.protobuf.converter.annotation.ProtoField;
import net.badata.protobuf.converter.mapping.Proto2MapperImpl;

import lombok.Getter;
import lombok.Setter;

/**
 * RDF wrapper of {@link Hash}
 *
 * @author Ruipeng Zhang
 */
@ProtoClass(value = Hash.class, mapper = Proto2MapperImpl.class)
public class RDFHash extends RDFProtoStruct<Hash> {
    @Getter
    @Setter
    @ProtoField(converter = HashDigest.Converter.class)
    private HashDigest sha256;
    @Getter
    @Setter
    @ProtoField(converter = HashDigest.Converter.class)
    private HashDigest sha1;
    @Getter
    @Setter
    @ProtoField(converter = HashDigest.Converter.class)
    private HashDigest md5;
    @Getter
    @Setter
    @ProtoField(converter = HashDigest.Converter.class)
    private HashDigest pecoffSha1;
    @Getter
    @Setter
    @ProtoField(converter = HashDigest.Converter.class)
    private HashDigest pecoffMd5;
    @Getter
    @Setter
    @ProtoField(converter = HashDigest.Converter.class)
    private HashDigest pecoffSha256;
    @Getter
    @Setter
    @ProtoField
    private RDFAuthenticodeSignedData signedData;
    @Getter
    @Setter
    @ProtoField
    private Long numBytes;
}
