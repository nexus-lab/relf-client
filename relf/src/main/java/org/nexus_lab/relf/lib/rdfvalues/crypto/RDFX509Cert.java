package org.nexus_lab.relf.lib.rdfvalues.crypto;

import com.google.protobuf.ByteString;
import org.nexus_lab.relf.lib.exceptions.CertificateException;
import org.nexus_lab.relf.lib.exceptions.DecodeException;
import org.nexus_lab.relf.lib.exceptions.VerificationException;
import org.nexus_lab.relf.lib.rdfvalues.RDFDatetime;

import net.badata.protobuf.converter.type.TypeConverter;

import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x500.style.IETFUtils;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Date;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author Ruipeng Zhang
 */
@NoArgsConstructor
public class RDFX509Cert extends RDFSecurityObject<X509Certificate> {
    @Getter
    @Setter
    private X509Certificate value;

    /**
     * @param value PEM string of the certificate
     * @throws DecodeException unable to parse certificate from given string
     */
    public RDFX509Cert(String value) throws DecodeException {
        super(value);
    }

    /**
     * @return common name of the certificate
     * @throws CertificateException certificate is invalid
     */
    public String getCN() throws CertificateException {
        try {
            X500Name x500name = new JcaX509CertificateHolder(getValue()).getSubject();
            RDN cn = x500name.getRDNs(BCStyle.CN)[0];
            return IETFUtils.valueToString(cn.getFirst().getValue());
        } catch (CertificateEncodingException e) {
            throw new CertificateException("Invalid certificate encoding", e);
        }
    }

    /**
     * @return public key of the certificate
     * @throws CertificateException certificate is invalid
     */
    public RSAPublicKey getPublicKey() throws CertificateException {
        if (getValue().getPublicKey() instanceof java.security.interfaces.RSAPublicKey) {
            return new RSAPublicKey(
                    (java.security.interfaces.RSAPublicKey) getValue().getPublicKey());
        }
        throw new CertificateException("The certificate does not use RSA public key.");
    }

    /**
     * @return serial number of the certificate
     */
    public BigInteger getSerialNumber() {
        return getValue().getSerialNumber();
    }

    /**
     * @return issuer of the certificate
     */
    public Principal getIssuer() {
        return getValue().getIssuerDN();
    }

    /**
     * Verify if the certificate is not expired and the signature is valid.
     *
     * @param publicKey public key for verification
     * @return true if certificate is valid
     * @throws VerificationException cannot verify certificate due to public key error
     * @throws CertificateException  certificate format or encoding is invalid
     */
    public boolean verify(RSAPublicKey publicKey)
            throws VerificationException, CertificateException {
        Date now = RDFDatetime.now().asDate();
        if (now.after(getValue().getNotAfter())) {
            throw new CertificateException("Certificate expired!");
        } else if (now.before(getValue().getNotBefore())) {
            throw new CertificateException("Certificate not yet valid!");
        }
        try {
            return publicKey.verify(getValue().getTBSCertificate(), getValue().getSignature(),
                    getValue().getSigAlgName());
        } catch (CertificateEncodingException e) {
            throw new CertificateException("Certificate encoding error", e);
        }
    }

    @Override
    public RDFX509Cert parse(String string) throws DecodeException {
        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            InputStream stream = new ByteArrayInputStream(
                    string.getBytes(StandardCharsets.UTF_8.name()));
            setValue((X509Certificate) cf.generateCertificate(stream));
        } catch (java.security.cert.CertificateException | UnsupportedEncodingException e) {
            throw new DecodeException(String.format("Invalid certificate %s", string), e);
        }
        return this;
    }

    @Override
    public byte[] serialize() {
        try {
            return getValue().getEncoded();
        } catch (CertificateEncodingException e) {
            throw new RuntimeException("Failed to encode certificate.");
        }
    }

    /**
     * {@link TypeConverter} for inter-conversion between {@link RDFX509Cert} and {@link ByteString}
     * during protobuf conversion.
     */
    public static class Converter implements TypeConverter<RDFX509Cert, ByteString> {

        @Override
        public RDFX509Cert toDomainValue(Object instance) {
            try {
                return new RDFX509Cert().parse(((ByteString) instance).toString("UTF-8"));
            } catch (DecodeException | UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public ByteString toProtobufValue(Object instance) {
            try {
                return ByteString.copyFrom(((RDFX509Cert) instance).stringify(), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
