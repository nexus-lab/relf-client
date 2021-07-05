package org.nexus_lab.relf.lib.rdfvalues.flows;

import org.nexus_lab.relf.lib.rdfvalues.crypto.EncryptionKey;
import org.nexus_lab.relf.lib.rdfvalues.structs.RDFProtoStruct;
import org.nexus_lab.relf.proto.ClientCommunication;
import org.nexus_lab.relf.proto.HttpRequest;
import org.nexus_lab.relf.utils.ByteUtils;

import net.badata.protobuf.converter.annotation.ProtoClass;
import net.badata.protobuf.converter.annotation.ProtoField;
import net.badata.protobuf.converter.mapping.Proto2MapperImpl;
import net.badata.protobuf.converter.type.ByteStringConverterImpl;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import lombok.Getter;
import lombok.Setter;

/**
 * RDF wrapper of {@link ClientCommunication}
 *
 * @author Ruipeng Zhang
 */
@ProtoClass(value = ClientCommunication.class, mapper = Proto2MapperImpl.class)
public class RDFClientCommunication extends RDFProtoStruct<ClientCommunication> {
    @Getter
    @Setter
    @ProtoField(converter = ByteStringConverterImpl.class)
    private byte[] encrypted;
    @Getter
    @Setter
    @ProtoField(converter = ByteStringConverterImpl.class)
    private byte[] encryptedCipher;
    @Getter
    @Setter
    @ProtoField(converter = ByteStringConverterImpl.class)
    private byte[] encryptedCipherMetadata;
    @Getter
    @Setter
    @ProtoField(converter = EncryptionKey.Converter.class)
    private EncryptionKey packetIv;
    @Getter
    @Setter
    @ProtoField
    private HttpRequest origRequest;
    @Getter
    @Setter
    @ProtoField
    private Integer queueSize;
    @Getter
    @Setter
    @ProtoField
    private ClientCommunication.Status status;
    @Getter
    @Setter
    @ProtoField
    private Integer apiVersion;
    @Getter
    @Setter
    @ProtoField(converter = ByteStringConverterImpl.class)
    private byte[] hmac;
    @Getter
    @Setter
    @ProtoField(converter = ByteStringConverterImpl.class)
    private byte[] fullHmac;

    /**
     * @return encrypted data
     */
    public byte[] getEncoded() {
        return getEncrypted();
    }

    /**
     * @return get encrypted data along with cipher, cipher metadata, initialization vector and API
     * version code in
     * one byte array
     */
    public byte[] getFullEncoded() {
        ByteBuffer apiVersionBuffer = ByteBuffer.allocate(4);
        apiVersionBuffer.order(ByteOrder.LITTLE_ENDIAN);
        apiVersionBuffer.putInt(apiVersion);

        return ByteUtils.concat(getEncrypted(),
                getEncryptedCipher(),
                getEncryptedCipherMetadata(),
                getPacketIv().serialize(),
                apiVersionBuffer.array());
    }
}
