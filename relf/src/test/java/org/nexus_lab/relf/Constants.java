package org.nexus_lab.relf;

import android.system.OsConstants;

import org.nexus_lab.relf.lib.exceptions.DecodeException;
import org.nexus_lab.relf.lib.rdfvalues.crypto.RDFX509Cert;
import org.nexus_lab.relf.lib.rdfvalues.crypto.RSAPrivateKey;
import org.nexus_lab.relf.lib.rdfvalues.crypto.RSAPublicKey;
import org.nexus_lab.relf.utils.PathUtils;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.SystemUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;


/**
 * Test constants.
 *
 * @author Ruipeng Zhang
 */
public class Constants {
    /**
     * Testing ReLF CA certificate
     */
    public static final RDFX509Cert CA_CERTIFICATE = loadcert("/etc/ssl/certs/ca_certificate.pem");
    /**
     * Testing ReLF server certificate
     */
    public static final RDFX509Cert SERVER_CERTIFICATE = loadcert("/etc/ssl/certs/server_certificate.pem");
    /**
     * Testing ReLF server private key
     */
    public static final RSAPrivateKey SERVER_PRIVATE_KEY = loadkey("/etc/ssl/private/server_private_key.pem");
    /**
     * Testing ReLF client private key
     */
    public static final RSAPrivateKey CLIENT_PRIVATE_KEY = loadkey("/etc/ssl/private/client_private_key.pem");
    /**
     * Testing ReLF client public key
     */
    public static final RSAPublicKey CLIENT_PUBLIC_KEY = CLIENT_PRIVATE_KEY.getPublicKey();

    private static RDFX509Cert loadcert(String path) {
        try {
            byte[] bytes = IOUtils.toByteArray(new FileInputStream(fspath(path)));
            return new RDFX509Cert(new String(bytes, StandardCharsets.UTF_8));
        } catch (IOException | DecodeException e) {
            throw new RuntimeException(e);
        }
    }

    private static RSAPrivateKey loadkey(String path) {
        try {
            byte[] bytes = IOUtils.toByteArray(new FileInputStream(fspath(path)));
            return new RSAPrivateKey(new String(bytes, StandardCharsets.UTF_8));
        } catch (IOException | DecodeException e) {
            throw new RuntimeException(e);
        }
    }

    private static void setStaticField(Class clazz, String fieldName, Object fieldValue) {
        try {
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
            field.set(null, fieldValue);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param path file path relative to the test resource folder `fs`.
     * @return absolution path of the test file.
     */
    public static String fspath(String path) {
        String prefix = Constants.class.getClassLoader().getResource("fs").getFile();
        if (SystemUtils.IS_OS_WINDOWS && prefix.startsWith("/")) {
            prefix = prefix.substring(1);
        }
        return path.startsWith(prefix) ? path : PathUtils.normalize(prefix + "/" + path);
    }

    /**
     * Override some {@link android.system.OsConstants} constants.
     */
    public static void overrideOsConstants() {
        setStaticField(OsConstants.class, "AF_UNIX", 1);
        setStaticField(OsConstants.class, "AF_INET", 2);
        setStaticField(OsConstants.class, "AF_INET6", 10);
        setStaticField(OsConstants.class, "SOCK_STREAM", 1);
        setStaticField(OsConstants.class, "SOCK_DGRAM", 2);
    }
}
