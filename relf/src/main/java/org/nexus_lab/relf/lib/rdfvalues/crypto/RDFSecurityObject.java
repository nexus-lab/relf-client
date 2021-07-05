package org.nexus_lab.relf.lib.rdfvalues.crypto;

import org.nexus_lab.relf.lib.exceptions.DecodeException;
import org.nexus_lab.relf.lib.rdfvalues.RDFDatetime;
import org.nexus_lab.relf.lib.rdfvalues.RDFValue;
import org.nexus_lab.relf.utils.ByteUtils;

import org.bouncycastle.openssl.jcajce.JcaPEMWriter;

import java.io.IOException;
import java.io.StringWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.util.Objects;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Base class for security related {@link RDFValue}.
 *
 * @param <T> wrapped value type
 * @author Ruipeng Zhang
 */
@NoArgsConstructor
public abstract class RDFSecurityObject<T> implements RDFValue<T> {
    static {
        // Bypass Android P's BC provider deprecation by removing it first
        Security.removeProvider("BC");
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
    }

    @Getter
    @Setter
    private RDFDatetime age = new RDFDatetime(0);

    /**
     * @param value PEM string of the security object
     * @throws DecodeException cannot decode {@link RDFSecurityObject} from given string
     */
    public RDFSecurityObject(String value) throws DecodeException {
        parse(value);
    }

    @Override
    public RDFSecurityObject<T> parse(byte[] value) throws DecodeException {
        parse(new String(value));
        return this;
    }

    @Override
    public String stringify() {
        if (getValue() == null) {
            return "";
        }
        StringWriter writer = new StringWriter();
        JcaPEMWriter pemWriter = new JcaPEMWriter(writer);
        try {
            pemWriter.writeObject(getValue());
            pemWriter.close();
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException("Cannot encode object into PEM.", e);
        }
        return writer.toString().replace("\r", "");
    }

    @Override
    public String toString() {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(stringify().getBytes());
            return String.format("<%s> %s", getClass().getSimpleName(),
                    ByteUtils.toHexString(hash));
        } catch (NoSuchAlgorithmException e) {
            return String.format("<%s>", getClass().getSimpleName());
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !getClass().isInstance(obj)) {
            return false;
        }
        RDFSecurityObject other = (RDFSecurityObject) obj;
        return Objects.equals(other.getAge().getValue(), getAge().getValue())
                && Objects.equals(getValue(), other.getValue());
    }
}
