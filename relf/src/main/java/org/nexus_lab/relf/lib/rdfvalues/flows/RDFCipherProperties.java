package org.nexus_lab.relf.lib.rdfvalues.flows;

import org.nexus_lab.relf.lib.rdfvalues.crypto.EncryptionKey;
import org.nexus_lab.relf.lib.rdfvalues.structs.RDFProtoStruct;
import org.nexus_lab.relf.proto.CipherProperties;

import net.badata.protobuf.converter.annotation.ProtoClass;
import net.badata.protobuf.converter.annotation.ProtoField;
import net.badata.protobuf.converter.mapping.Proto2MapperImpl;

import lombok.Getter;
import lombok.Setter;

/**
 * RDF wrapper of {@link CipherProperties}
 *
 * @author Ruipeng Zhang
 */
@ProtoClass(value = CipherProperties.class, mapper = Proto2MapperImpl.class)
public class RDFCipherProperties extends RDFProtoStruct<CipherProperties> {
    @Getter
    @Setter
    @ProtoField
    private String name;
    @Getter
    @Setter
    @ProtoField(converter = EncryptionKey.Converter.class)
    private EncryptionKey key;
    @Getter
    @Setter
    @ProtoField(converter = EncryptionKey.Converter.class)
    private EncryptionKey metadataIv;
    @Getter
    @Setter
    @ProtoField(converter = EncryptionKey.Converter.class)
    private EncryptionKey hmacKey;
    @Getter
    @Setter
    @ProtoField
    private CipherProperties.HMACType hmacType;
}
