package org.nexus_lab.relf.lib.communicator;

import org.nexus_lab.relf.lib.exceptions.DecryptionException;
import org.nexus_lab.relf.lib.exceptions.VerificationException;
import org.nexus_lab.relf.lib.rdfvalues.crypto.HMAC;
import org.nexus_lab.relf.lib.rdfvalues.crypto.RSAPrivateKey;
import org.nexus_lab.relf.lib.rdfvalues.crypto.RSAPublicKey;
import org.nexus_lab.relf.lib.rdfvalues.flows.RDFCipherMetadata;
import org.nexus_lab.relf.lib.rdfvalues.flows.RDFCipherProperties;
import org.nexus_lab.relf.lib.rdfvalues.flows.RDFClientCommunication;
import org.nexus_lab.relf.proto.CipherProperties;

/**
 * Received server cipher
 *
 * @author Ruipeng Zhang
 */
public class ReceivedCipher extends Cipher {
    private static final int KEY_SIZE = 128;
    private static final int IV_SIZE = 128;

    private byte[] serializedCipher;

    ReceivedCipher(RDFCipherProperties cipher) {
        super(cipher);
    }

    private static RDFCipherProperties createCipher(byte[] serializedCipher)
            throws DecryptionException {
        RDFCipherProperties cipher = new RDFCipherProperties();
        cipher.parse(serializedCipher);
        if (cipher.getKey().length() != KEY_SIZE || cipher.getMetadataIv().length() != IV_SIZE
                || cipher.getHmacKey().length() != KEY_SIZE) {
            throw new DecryptionException("Invalid cipher");
        }
        return cipher;
    }

    /**
     * Restore cipher from a received packet
     *
     * @param packet     received packet from server
     * @param privateKey client private key
     * @return decrypted cipher
     * @throws DecryptionException   failed to decrypt received packet
     * @throws VerificationException failed to validate cipher's HMAC
     */
    public static ReceivedCipher from(RDFClientCommunication packet, RSAPrivateKey privateKey)
            throws DecryptionException, VerificationException {
        if (packet.getApiVersion() != 3) {
            throw new DecryptionException(String.format("Unsupported api version: %s, expected 3",
                    packet.getApiVersion()));
        }
        if (packet.getEncryptedCipher() == null) {
            throw new DecryptionException("Server is not encrypted");
        }

        byte[] serializedCipher = privateKey.decrypt(packet.getEncryptedCipher());
        RDFCipherProperties cipher = createCipher(serializedCipher);
        ReceivedCipher result = new ReceivedCipher(cipher);
        result.setEncryptedCipher(packet.getEncryptedCipher());
        result.setEncryptedMetadata(packet.getEncryptedCipherMetadata());
        result.serializedCipher = serializedCipher;

        if (result.verifyHmac(packet)) {
            byte[] serializedMetadata = result.decrypt(packet.getEncryptedCipherMetadata(),
                    cipher.getMetadataIv());
            result.setMetadata(
                    (RDFCipherMetadata) new RDFCipherMetadata().parse(serializedMetadata));
        }
        return result;
    }

    /**
     * Verify packet's HMAC against calculated HMAC using this cipher
     *
     * @param packet packet to be verified
     * @return if packet's HMAC is same as the calculated HMAC
     * @throws VerificationException HMAC type is not supported
     */
    public boolean verifyHmac(RDFClientCommunication packet) throws VerificationException {
        byte[] hmac;
        byte[] message;
        if (getHmacType() == CipherProperties.HMACType.SIMPLE_HMAC) {
            message = packet.getEncrypted();
            hmac = packet.getHmac();
        } else if (getHmacType() == CipherProperties.HMACType.FULL_HMAC) {
            message = packet.getFullEncoded();
            hmac = packet.getFullHmac();
        } else {
            throw new VerificationException("HMAC type no supported.");
        }
        HMAC signature = new HMAC(getCipher().getHmacKey());
        return signature.verify(message, hmac);
    }

    /**
     * Verify packet's signature using server's public key
     *
     * @param remotePublicKey server's public key
     * @return if the signature is valid
     * @throws VerificationException failed to verify data using the given public key
     */
    public boolean verifySignature(RSAPublicKey remotePublicKey) throws VerificationException {
        if (getMetadata() != null && getMetadata().getSignature() != null
                && remotePublicKey != null) {
            return remotePublicKey.verify(serializedCipher, getMetadata().getSignature());
        }
        return false;
    }
}
