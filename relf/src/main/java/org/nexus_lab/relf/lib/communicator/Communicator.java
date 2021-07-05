package org.nexus_lab.relf.lib.communicator;

import org.nexus_lab.relf.lib.exceptions.DecodeException;
import org.nexus_lab.relf.lib.exceptions.DecryptionException;
import org.nexus_lab.relf.lib.exceptions.EncryptionException;
import org.nexus_lab.relf.lib.exceptions.VerificationException;
import org.nexus_lab.relf.lib.rdfvalues.Duration;
import org.nexus_lab.relf.lib.rdfvalues.RDFDatetime;
import org.nexus_lab.relf.lib.rdfvalues.RDFURN;
import org.nexus_lab.relf.lib.rdfvalues.crypto.EncryptionKey;
import org.nexus_lab.relf.lib.rdfvalues.crypto.RSAPrivateKey;
import org.nexus_lab.relf.lib.rdfvalues.crypto.RSAPublicKey;
import org.nexus_lab.relf.lib.rdfvalues.flows.RDFCipherMetadata;
import org.nexus_lab.relf.lib.rdfvalues.flows.RDFClientCommunication;
import org.nexus_lab.relf.lib.rdfvalues.flows.RDFGrrMessage;
import org.nexus_lab.relf.lib.rdfvalues.flows.RDFMessageList;
import org.nexus_lab.relf.lib.rdfvalues.flows.RDFPackedMessageList;
import org.nexus_lab.relf.proto.GrrMessage;
import org.nexus_lab.relf.proto.PackedMessageList;
import org.nexus_lab.relf.utils.ByteUtils;
import org.nexus_lab.relf.utils.store.FastStore;

import java.security.SignatureException;
import java.util.zip.DataFormatException;

import lombok.Getter;

/**
 * A class for encoding/encrypting/compressing and decoding/decrypting/decompressing
 * communication data
 *
 * @author Ruipeng Zhang
 */
public class Communicator {
    private static final long SERVER_CIPHER_EXPIRY = new Duration("1d").getMicroseconds();

    private Cipher serverCipher;
    private FastStore<byte[], ReceivedCipher> receivedCipherCache;

    @Getter
    private RDFURN localName;
    @Getter
    private RDFURN remoteName;
    @Getter
    private RSAPublicKey remotePublicKey;
    @Getter
    private RSAPrivateKey localPrivateKey;

    /**
     * @param remoteName      local communicator name in URN
     * @param localName       remote communicator name in URN
     * @param localPrivateKey RSA private key for outgoing cipher signing and incoming cipher
     *                        decryption
     * @param remotePublicKey RSA public key for outgoing cipher encryption and incoming cipher
     *                        verifying
     */
    public Communicator(RDFURN localName, RDFURN remoteName, RSAPrivateKey localPrivateKey,
            RSAPublicKey remotePublicKey) {
        this.remotePublicKey = remotePublicKey;
        this.localName = localName;
        this.remoteName = remoteName;
        this.localPrivateKey = localPrivateKey;
        receivedCipherCache = new FastStore<>(50000);
    }

    /**
     * @return if the server cipher expired. The cipher is only valid for a period of time
     * ({@link Communicator#SERVER_CIPHER_EXPIRY}ms)
     */
    private boolean isServerCipherExpired() {
        if (serverCipher != null) {
            long expiry = serverCipher.getAge().asMicrosecondsFromEpoch() + SERVER_CIPHER_EXPIRY;
            if (expiry > RDFDatetime.now().asMicrosecondsFromEpoch()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Serialize and compress messages and set the timestamp. If the compressed data length is
     * equal or larger than the uncompressed data, use the latter instead.
     *
     * @param messages  messages to be serialized
     * @param timestamp sending time of the messages
     * @return compressed (or not) and serialized data
     */
    private byte[] compress(RDFMessageList messages, RDFDatetime timestamp) {
        RDFPackedMessageList result = new RDFPackedMessageList();
        result.setTimestamp(timestamp);
        byte[] uncompressed = messages.serialize();
        byte[] compressed = ByteUtils.compress(uncompressed);
        if (compressed.length < uncompressed.length) {
            result.setCompression(PackedMessageList.CompressionType.ZCOMPRESSION);
            result.setMessageList(compressed);
        } else {
            result.setMessageList(uncompressed);
        }
        return result.serialize();
    }

    /**
     * Decompress and deserialize messages
     *
     * @param messages compressed messages
     * @return decompressed and deserialized messages
     * @throws DecodeException cannot decompress messages because compression scheme unknown or
     *                         failed to unzip data
     */
    private RDFMessageList decompress(RDFPackedMessageList messages) throws DecodeException {
        PackedMessageList.CompressionType compression = messages.getCompression();
        byte[] data = messages.getMessageList();
        if (compression == PackedMessageList.CompressionType.ZCOMPRESSION) {
            try {
                data = ByteUtils.decompress(data);
            } catch (DataFormatException e) {
                throw new DecodeException("Failed to decompress messages", e);
            }
        } else if (compression != PackedMessageList.CompressionType.UNCOMPRESSED) {
            throw new DecodeException("Compression scheme not supported.");
        }
        return (RDFMessageList) new RDFMessageList().parse(data);
    }

    /**
     * Serialize, compress and encrypt messages
     *
     * @param messages  messages to be encoded
     * @param timestamp timestamp attached to the message
     * @return encoded data packet
     * @throws EncryptionException cannot encrypt messages using the existing cipher
     * @throws SignatureException  cannot sign cipher using the provided private key
     */
    public RDFClientCommunication encode(RDFMessageList messages, RDFDatetime timestamp)
            throws EncryptionException, SignatureException {
        if (isServerCipherExpired()) {
            serverCipher = Cipher.create(getLocalName(), getLocalPrivateKey(),
                    getRemotePublicKey());
        }

        EncryptionKey iv = EncryptionKey.generateRandomIV(128);
        byte[] encrypted = serverCipher.encrypt(compress(messages, timestamp), iv);

        RDFClientCommunication packet = new RDFClientCommunication();
        packet.setApiVersion(3);
        packet.setPacketIv(iv);
        packet.setEncrypted(encrypted);
        packet.setEncryptedCipher(serverCipher.getEncryptedCipher());
        packet.setEncryptedCipherMetadata(serverCipher.getEncryptedMetadata());
        packet.setHmac(serverCipher.hmac(encrypted));
        packet.setFullHmac(serverCipher.hmac(packet.getFullEncoded()));
        return packet;
    }

    /**
     * Verify the received cipher and message
     *
     * @param packedMessages messages to be examined
     * @param cipher         cipher to be verified
     * @param cipherVerified if the cipher is previously verified
     * @param timestamp      original timestamp attached to the messages when encoded
     * @return if message is authenticated
     * @throws VerificationException cannot verify cipher using the public key
     */
    private GrrMessage.AuthorizationState authenticate(
            RDFPackedMessageList packedMessages,
            ReceivedCipher cipher,
            boolean cipherVerified,
            RDFDatetime timestamp) throws VerificationException {
        GrrMessage.AuthorizationState result = GrrMessage.AuthorizationState.UNAUTHENTICATED;
        if (cipherVerified || cipher.verifySignature(remotePublicKey)) {
            result = GrrMessage.AuthorizationState.AUTHENTICATED;
        }
        // Check for replay attacks. We expect the server to return the same timestamp we sent.
        if (!packedMessages.getTimestamp().equals(timestamp)) {
            result = GrrMessage.AuthorizationState.UNAUTHENTICATED;
        }
        if (cipher.getMetadata() == null) {
            RDFCipherMetadata metadata = new RDFCipherMetadata();
            metadata.setSource(packedMessages.getSource());
            cipher.setMetadata(metadata);
        }
        return result;
    }

    /**
     * Decrypt, decompress, deserialize and verify encrypted messages
     *
     * @param packet    encrypted packet
     * @param timestamp original timestamp attached to the messages when encoded
     * @return decoded messages
     * @throws DecryptionException   cannot decrypted messages using the cipher
     * @throws DecodeException       the received cipher is not from the same server
     * @throws VerificationException cannot verify the received cipher using the public key
     */
    public RDFMessageList decode(RDFClientCommunication packet, RDFDatetime timestamp)
            throws DecryptionException, DecodeException, VerificationException {
        boolean cipherVerified = false;
        ReceivedCipher cipher = receivedCipherCache.get(packet.getEncryptedCipher());

        if (cipher != null && cipher.verifyHmac(packet)) {
            cipherVerified = true;
        } else {
            cipher = ReceivedCipher.from(packet, getLocalPrivateKey());
            if (cipher.verifySignature(remotePublicKey)) {
                receivedCipherCache.put(packet.getEncryptedCipher(), cipher);
                cipherVerified = true;
            }
        }
        if (!cipher.getSource().equals(getRemoteName())) {
            throw new DecodeException(String.format("Client wants to talk to %s, not %s",
                    getRemoteName(), cipher.getSource()));
        }

        byte[] compressed = cipher.decrypt(packet.getEncrypted(), packet.getPacketIv());
        RDFPackedMessageList packedMessages =
                (RDFPackedMessageList) new RDFPackedMessageList().parse(compressed);
        RDFMessageList messages = decompress(packedMessages);
        GrrMessage.AuthorizationState authState = authenticate(packedMessages, cipher,
                cipherVerified, timestamp);

        for (RDFGrrMessage message : messages.getJobs()) {
            message.setAuthState(authState);
            message.setSource(cipher.getMetadata().getSource());
        }
        messages.setSource(cipher.getMetadata().getSource());
        return messages;
    }
}
