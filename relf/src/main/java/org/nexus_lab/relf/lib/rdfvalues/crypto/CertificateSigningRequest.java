package org.nexus_lab.relf.lib.rdfvalues.crypto;

import org.nexus_lab.relf.lib.exceptions.CertificateException;
import org.nexus_lab.relf.lib.exceptions.DecodeException;
import org.nexus_lab.relf.lib.exceptions.VerificationException;
import org.nexus_lab.relf.lib.rdfvalues.RDFURN;

import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x500.style.IETFUtils;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.crypto.params.RSAKeyParameters;
import org.bouncycastle.crypto.util.PublicKeyFactory;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaContentVerifierProviderBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.pkcs.PKCS10CertificationRequestBuilder;
import org.bouncycastle.pkcs.PKCSException;

import java.io.IOException;
import java.io.StringReader;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author Ruipeng Zhang
 */
@NoArgsConstructor
public class CertificateSigningRequest extends RDFSecurityObject<PKCS10CertificationRequest> {
    @Getter
    @Setter
    private PKCS10CertificationRequest value;

    /**
     * @param value PEM string of the CSR
     * @throws DecodeException cannot decode CSR from the string
     */
    public CertificateSigningRequest(String value) throws DecodeException {
        super(value);
    }

    /**
     * @param commonName CSR common name
     * @param privateKey private key for signing the CSR
     */
    public CertificateSigningRequest(String commonName, RSAPrivateKey privateKey) {
        setValue(generate(commonName, privateKey));
    }

    /**
     * @param commonName CSR common name
     * @param privateKey private key for signing the CSR
     */
    public CertificateSigningRequest(RDFURN commonName, RSAPrivateKey privateKey) {
        this(commonName.stringify(), privateKey);
    }

    /**
     * Generate a new {@link CertificateSigningRequest}.
     *
     * @param commonName CSR common name
     * @param privateKey private key for signing the CSR
     */
    private static PKCS10CertificationRequest generate(String commonName,
            RSAPrivateKey privateKey) {
        try {
            X500Name subjectDN = new X500Name("CN=" + commonName);
            Signature signature = Signature.getInstance(RSAPrivateKey.SIGNATURE_ALGORITHM);
            signature.initSign(privateKey.getValue());
            SubjectPublicKeyInfo publicKeyInfo = SubjectPublicKeyInfo.getInstance(
                    ASN1Sequence.getInstance(privateKey.getPublicKey().serialize()));
            ContentSigner contentSigner = new JcaContentSignerBuilder(
                    RSAPrivateKey.SIGNATURE_ALGORITHM)
                    .build(privateKey.getValue());
            return new PKCS10CertificationRequestBuilder(subjectDN, publicKeyInfo).build(
                    contentSigner);
        } catch (NoSuchAlgorithmException | InvalidKeyException | OperatorCreationException e) {
            throw new RuntimeException("Failed to generate CSR.", e);
        }
    }

    /**
     * @return common name of the CSR
     */
    public String getCN() {
        RDN rdn = getValue().getSubject().getRDNs(BCStyle.CN)[0];
        return IETFUtils.valueToString(rdn.getFirst().getValue());
    }

    /**
     * @return public key of the CSR
     * @throws CertificateException CSR does not have a valid public key
     */
    public RSAPublicKey getPublicKey() throws CertificateException {
        try {
            RSAKeyParameters key = (RSAKeyParameters) PublicKeyFactory.createKey(
                    getValue().getSubjectPublicKeyInfo());
            RSAPublicKeySpec rsaSpec = new RSAPublicKeySpec(key.getModulus(), key.getExponent());
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return new RSAPublicKey(
                    (java.security.interfaces.RSAPublicKey) kf.generatePublic(rsaSpec));
        } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new CertificateException("Can not generate public key from CSR.", e);
        }
    }

    /**
     * Verify if the CSR has a valid signature.
     *
     * @param publicKey public for verification
     * @return true if CSR is valid
     * @throws VerificationException cannot verify CSR using the given key
     */
    public boolean verify(RSAPublicKey publicKey) throws VerificationException {
        try {
            return getValue().isSignatureValid(new JcaContentVerifierProviderBuilder()
                    .setProvider("BC").build(publicKey.getValue()));
        } catch (OperatorCreationException | PKCSException e) {
            throw new VerificationException(
                    String.format("Cannot verify CSR using public key %s", publicKey), e);
        }
    }

    @Override
    public CertificateSigningRequest parse(String string) throws DecodeException {
        try {
            PEMParser parser = new PEMParser(new StringReader(string));
            Object pem = parser.readObject();
            if (pem instanceof PKCS10CertificationRequest) {
                setValue((PKCS10CertificationRequest) pem);
            }
            return this;
        } catch (IOException e) {
            throw new DecodeException(String.format("Cannot parse CSR: %s", string));
        }
    }

    @Override
    public byte[] serialize() {
        try {
            return getValue().getEncoded();
        } catch (IOException e) {
            throw new RuntimeException("Failed to encode certificate request.");
        }
    }
}
