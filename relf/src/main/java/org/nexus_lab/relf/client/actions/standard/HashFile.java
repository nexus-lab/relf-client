package org.nexus_lab.relf.client.actions.standard;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;

import android.content.Context;

import org.nexus_lab.relf.client.actions.Action;
import org.nexus_lab.relf.client.actions.ActionCallback;
import org.nexus_lab.relf.client.actions.UsesPermission;
import org.nexus_lab.relf.client.vfs.VFS;
import org.nexus_lab.relf.client.vfs.VFSFile;
import org.nexus_lab.relf.lib.Constants;
import org.nexus_lab.relf.lib.rdfvalues.HashDigest;
import org.nexus_lab.relf.lib.rdfvalues.client.RDFFingerprintRequest;
import org.nexus_lab.relf.lib.rdfvalues.client.RDFFingerprintResponse;
import org.nexus_lab.relf.lib.rdfvalues.client.RDFFingerprintTuple;
import org.nexus_lab.relf.lib.rdfvalues.crypto.RDFHash;
import org.nexus_lab.relf.proto.FingerprintTuple;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Ruipeng Zhang
 */
@UsesPermission(READ_EXTERNAL_STORAGE)
public class HashFile implements Action<RDFFingerprintRequest, RDFFingerprintResponse> {
    private Map<String, MessageDigest> hashers = new HashMap<>();

    {
        try {
            hashers.put("md5", MessageDigest.getInstance("MD5"));
            hashers.put("sha1", MessageDigest.getInstance("SHA-1"));
            hashers.put("sha256", MessageDigest.getInstance("SHA-256"));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private long hashFile(VFSFile file, Set<String> hashTypes, long maxLength, ActionCallback callback)
            throws IOException {
        long bytesRead = 0;
        while (bytesRead < maxLength) {
            callback.onUpdate();
            long toRead = Math.min(Constants.CLIENT_MAX_BUFFER_SIZE, maxLength - bytesRead);
            byte[] data = file.read(toRead);
            if (data == null) {
                break;
            }
            for (String type : hashTypes) {
                MessageDigest hasher = hashers.get(type);
                if (hasher != null) {
                    hasher.update(data);
                }
            }
            bytesRead += data.length;
        }
        return bytesRead;
    }

    @Override
    public void execute(Context context, RDFFingerprintRequest request,
            ActionCallback<RDFFingerprintResponse> callback) {
        Set<String> hashTypes = new HashSet<>();
        List<RDFFingerprintTuple> tuples = request.getTuples();
        for (RDFFingerprintTuple tuple : tuples) {
            if (tuple.getHashers() != null) {
                for (FingerprintTuple.HashType hashType : tuple.getHashers()) {
                    hashTypes.add(hashType.name().toLowerCase());
                }
            }
        }
        try {
            VFSFile file = VFS.open(request.getPathspec(), callback::onUpdate);
            long bytesRead = hashFile(file, hashTypes, request.getMaxFilesize(), callback);

            RDFHash hash = new RDFHash();
            MessageDigest md5Hasher = hashers.get("md5");
            if (md5Hasher != null) {
                hash.setMd5(new HashDigest(md5Hasher.digest()));
            }
            MessageDigest sha1Hasher = hashers.get("sha1");
            if (sha1Hasher != null) {
                hash.setSha1(new HashDigest(sha1Hasher.digest()));
            }
            MessageDigest sha256Hasher = hashers.get("sha256");
            if (sha256Hasher != null) {
                hash.setSha256(new HashDigest(sha256Hasher.digest()));
            }

            RDFFingerprintResponse response = new RDFFingerprintResponse();
            response.setBytesRead(bytesRead);
            response.setPathspec(file.getPathspec());
            response.setHash(hash);

            callback.onResponse(response);
            callback.onComplete();
        } catch (IOException e) {
            callback.onError(e);
        } finally {
            for (MessageDigest digest : hashers.values()) {
                digest.reset();
            }
        }
    }
}
