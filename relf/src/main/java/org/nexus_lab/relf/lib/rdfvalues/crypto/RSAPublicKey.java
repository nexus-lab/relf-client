package org.nexus_lab.relf.lib.rdfvalues.crypto;

import org.nexus_lab.relf.lib.exceptions.DecodeException;
import org.nexus_lab.relf.lib.exceptions.EncryptionException;
import org.nexus_lab.relf.lib.exceptions.VerificationException;

import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;

import java.io.IOException;
import java.io.StringReader;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.SignatureException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author Ruipeng Zhang
 */
@NoArgsConstructor
public class RSAPublicKey extends RDFSecurityObject<java.security.interfaces.RSAPublicKey> {
    @Getter
    @Setter
    private java.security.interfaces.RSAPublicKey value;

    /**
     * @param value raw {@link java.security.interfaces.RSAPublicKey} value
     */
    public RSAPublicKey(java.security.interfaces.RSAPublicKey value) {
        setValue(value);
    }

    /**
     * @param value PEM string of the public key
     * @throws DecodeException cannot decode {@link RSAPublicKey} from given string
     */
    public RSAPublicKey(String value) throws DecodeException {
        super(value);
    }

    /**
     * @return the public key moduels
     */
    public BigInteger getN() {
        return getValue() == null ? new BigInteger("0") : getValue().getModulus();
    }

    /**
     * @return key length in bits
     */
    public int getKeyLength() {
        return getValue() == null ? 0 : getValue().getModulus().bitLength();
    }

    /**
     * Encrypt data using this public key.
     *
     * @param message data to be encrypted
     * @return encrypted data
     * @throws EncryptionException cannot encrypt data due to algorithm error
     */
    public byte[] encrypt(byte[] message) throws EncryptionException {
        if (getValue() == null) {
            throw new IllegalArgumentException("Can't encrypt with empty key.");
        }
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA1AndMGF1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, getValue());
            return cipher.doFinal(message);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException
                | BadPaddingException | IllegalBlockSizeException e) {
            throw new EncryptionException(
                    String.format("Cannot encrypt message using public key %s", toString()), e);
        }
    }

    /**
     * Verify data using this public key.
     *
     * @param message   data to be verified
     * @param signature signature to be checked against
     * @return true if the signature is valid
     * @throws VerificationException cannot verify data due to algorithm error
     */
    public boolean verify(byte[] message, byte[] signature) throws VerificationException {
        return verify(message, signature, RSAPrivateKey.SIGNATURE_ALGORITHM);
    }

    /**
     * Verify data using this public key.
     *
     * @param message       data to be verified
     * @param signature     signature to be checked against
     * @param hashAlgorithm signature hash algorithm
     * @return true if the signature is valid
     * @throws VerificationException cannot verify data due to algorithm error
     */
    public boolean verify(byte[] message, byte[] signature, String hashAlgorithm)
            throws VerificationException {
        try {
            Signature sig = Signature.getInstance(hashAlgorithm);
            sig.initVerify(getValue());
            sig.update(message);
            return sig.verify(signature);
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            throw new VerificationException(
                    String.format("Cannot verify message using public key %s", toString()), e);
        }
    }

    @Override
    public RSAPublicKey parse(String string) throws DecodeException {
        try {
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");
            PEMParser parser = new PEMParser(new StringReader(string));
            Object pem = parser.readObject();
            if (pem instanceof SubjectPublicKeyInfo) {
                setValue((java.security.interfaces.RSAPublicKey) converter.getPublicKey(
                        (SubjectPublicKeyInfo) pem));
            }
            return this;
        } catch (IOException e) {
            throw new DecodeException("Cannot parse private key: " + string, e);
        }
    }

    @Override
    public byte[] serialize() {
        return getValue().getEncoded();
    }
}
