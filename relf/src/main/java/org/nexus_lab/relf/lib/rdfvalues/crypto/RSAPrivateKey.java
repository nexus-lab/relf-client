package org.nexus_lab.relf.lib.rdfvalues.crypto;

import org.nexus_lab.relf.lib.exceptions.DecodeException;
import org.nexus_lab.relf.lib.exceptions.DecryptionException;

import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;

import java.io.IOException;
import java.io.StringReader;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAKeyGenParameterSpec;
import java.security.spec.RSAPublicKeySpec;

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
public class RSAPrivateKey extends RDFSecurityObject<RSAPrivateCrtKey> {
    /**
     * RSA signature algorithm. Default value is SHA256withRSA.
     */
    public static final String SIGNATURE_ALGORITHM = "SHA256withRSA";

    @Getter
    @Setter
    private RSAPrivateCrtKey value;
    private RSAPublicKey publicKey;

    public RSAPrivateKey(String value) throws DecodeException {
        super(value);
    }

    /**
     * @param value     private key
     * @param publicKey corresponding public key
     */
    public RSAPrivateKey(RSAPrivateCrtKey value, RSAPublicKey publicKey) {
        setValue(value);
        this.publicKey = publicKey;
    }

    /**
     * Generate a new private key.
     *
     * @return generate private key
     */
    public static RSAPrivateKey generate() {
        return generate(2048, 65537);
    }

    /**
     * Generate a new private key with certain key size.
     *
     * @param keySize key size in bits
     * @return generate private key
     */
    public static RSAPrivateKey generate(int keySize) {
        return generate(keySize, 65537);
    }

    /**
     * Generate a new private key with certain key size and public-exponent value.
     *
     * @param keySize   key size in bits
     * @param exponents the public exponent
     * @return generate private key
     */
    public static RSAPrivateKey generate(int keySize, int exponents) {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            RSAKeyGenParameterSpec spec = new RSAKeyGenParameterSpec(keySize,
                    new BigInteger(String.valueOf(exponents)));
            generator.initialize(spec);
            KeyPair keyPair = generator.generateKeyPair();
            return new RSAPrivateKey((RSAPrivateCrtKey) keyPair.getPrivate(),
                    new RSAPublicKey((java.security.interfaces.RSAPublicKey) keyPair.getPublic()));
        } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException e) {
            throw new RuntimeException("Can not generate a private key.");
        }
    }

    /**
     * @return the public key to the private key
     */
    public RSAPublicKey getPublicKey() {
        if (getValue() == null) {
            return null;
        }
        if (publicKey != null) {
            return publicKey;
        }
        RSAPublicKeySpec spec = new RSAPublicKeySpec(getValue().getModulus(),
                getValue().getPublicExponent());
        try {
            KeyFactory factory = KeyFactory.getInstance("RSA");
            PublicKey key = factory.generatePublic(spec);
            publicKey = new RSAPublicKey((java.security.interfaces.RSAPublicKey) key);
            return publicKey;
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException(
                    String.format("Cannot extract public key from private key: %s", toString()));
        }
    }

    /**
     * Sign some data with this private key
     *
     * @param message data to be signed
     * @return data's signature
     * @throws SignatureException cannot sign the data due to algorithm error
     */
    public byte[] sign(byte[] message) throws SignatureException {
        try {
            Signature sig = Signature.getInstance(SIGNATURE_ALGORITHM);
            sig.initSign(getValue());
            sig.update(message);
            return sig.sign();
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            throw new SignatureException(
                    String.format("Cannot sign message using private key %s", toString()), e);
        }
    }

    /**
     * Decrypt data using this private key
     *
     * @param message data to be decrypted
     * @return decrypted data
     * @throws DecryptionException cannot decrypt data due to algorithm error
     */
    public byte[] decrypt(byte[] message) throws DecryptionException {
        if (getValue() == null) {
            throw new IllegalArgumentException("Can't decrypt with empty key.");
        }
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA1AndMGF1Padding");
            cipher.init(Cipher.DECRYPT_MODE, getValue());
            return cipher.doFinal(message);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException
                | InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
            throw new DecryptionException(
                    String.format("Cannot decrypt message using private key %s", toString()), e);
        }
    }

    @Override
    public RSAPrivateKey parse(String string) throws DecodeException {
        try {
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");
            PEMParser parser = new PEMParser(new StringReader(string));
            Object pem = parser.readObject();
            if (pem instanceof PEMKeyPair) {
                KeyPair keyPair = converter.getKeyPair((PEMKeyPair) pem);
                setValue((RSAPrivateCrtKey) keyPair.getPrivate());
                publicKey = new RSAPublicKey(
                        (java.security.interfaces.RSAPublicKey) keyPair.getPublic());
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
