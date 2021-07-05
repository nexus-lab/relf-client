package org.nexus_lab.relf.lib.rdfvalues.client;

import org.nexus_lab.relf.lib.exceptions.DecodeException;
import org.nexus_lab.relf.lib.rdfvalues.RDFURN;
import org.nexus_lab.relf.lib.rdfvalues.crypto.RSAPrivateKey;
import org.nexus_lab.relf.lib.rdfvalues.crypto.RSAPublicKey;
import org.nexus_lab.relf.utils.ByteUtils;
import org.nexus_lab.relf.utils.PathUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.NoArgsConstructor;

/**
 * @author Ruipeng Zhang
 */
@NoArgsConstructor
public class ClientURN extends RDFURN {
    private static final Pattern CLIENT_ID_RE = Pattern.compile(
            "^(aff4:)?/?(([cC])\\.[0-9a-fA-F]{16})$");

    /**
     * @param value URN path
     */
    public ClientURN(String value) {
        super(value);
    }

    /**
     * @param value URN path
     */
    public ClientURN(RDFURN value) {
        parse(value.getValue());
        setAge(value.getAge());
    }

    /**
     * Create client URN from public key.
     *
     * @param key public key
     * @return a new {@link ClientURN} instance
     * @throws DecodeException cannot hash the information
     */
    public static ClientURN fromPublicKey(RSAPublicKey key) throws DecodeException {
        byte[] n = Arrays.copyOfRange(key.getN().toByteArray(), 1, 257);
        byte[] head = ByteBuffer.allocate(4).putInt(n.length + 1).array();
        ByteArrayOutputStream mpiStream = new ByteArrayOutputStream();
        try {
            mpiStream.write(head);
            mpiStream.write(new byte[]{0});
            mpiStream.write(n);
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(mpiStream.toByteArray());
            byte[] bytes = Arrays.copyOfRange(hash, 0, 8);
            return new ClientURN("C." + ByteUtils.toHexString(bytes));
        } catch (NoSuchAlgorithmException | IOException e) {
            throw new DecodeException(
                    String.format("Cannot create client ID from public key: %s", key.toString()),
                    e);
        }
    }

    /**
     * Create client URN from public key, which eventually uses the public key of the private key.
     *
     * @param key private key
     * @return a new {@link ClientURN} instance
     * @throws DecodeException cannot hash the information
     */
    public static ClientURN fromPrivateKey(RSAPrivateKey key) throws DecodeException {
        return fromPublicKey(key.getPublicKey());
    }

    @Override
    public ClientURN parse(String string) {
        super.parse(string.trim());
        Matcher matcher = CLIENT_ID_RE.matcher(getValue());
        if (!matcher.find()) {
            throw new IllegalArgumentException(String.format("Client urn malformed: %s", string));
        }

        String clientId = matcher.group(2);
        String correctClientId = clientId.substring(0, 1).toUpperCase() +
                clientId.substring(1).toLowerCase();
        setValue(getValue().replace(clientId, correctClientId));
        return this;
    }

    /**
     * Append a path segment to the end of {@link ClientURN}.
     *
     * @param path path segment to be appended
     * @return new {@link RDFURN} instance
     */
    public RDFURN add(String path) {
        RDFURN result = new RDFURN(PathUtils.concat(getValue(), path));
        result.setAge(getAge());
        return result;
    }
}
