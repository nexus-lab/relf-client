package org.nexus_lab.relf.client.actions.file;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;

import android.content.Context;

import org.nexus_lab.relf.client.actions.Action;
import org.nexus_lab.relf.client.actions.ActionCallback;
import org.nexus_lab.relf.client.actions.UsesPermission;
import org.nexus_lab.relf.client.vfs.VFS;
import org.nexus_lab.relf.client.vfs.VFSFile;
import org.nexus_lab.relf.lib.fingerprint.Fingerprinter;
import org.nexus_lab.relf.lib.rdfvalues.HashDigest;
import org.nexus_lab.relf.lib.rdfvalues.client.RDFFingerprintRequest;
import org.nexus_lab.relf.lib.rdfvalues.client.RDFFingerprintResponse;
import org.nexus_lab.relf.lib.rdfvalues.client.RDFFingerprintTuple;
import org.nexus_lab.relf.lib.rdfvalues.crypto.RDFHash;
import org.nexus_lab.relf.lib.rdfvalues.protodict.RDFDict;
import org.nexus_lab.relf.proto.FingerprintTuple;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Ruipeng Zhang
 */
@UsesPermission(READ_EXTERNAL_STORAGE)
public class FingerprintFile implements Action<RDFFingerprintRequest, RDFFingerprintResponse> {
    private Map<FingerprintTuple.HashType, MessageDigest> hasherMap = new HashMap<>();

    {
        try {
            hasherMap.put(FingerprintTuple.HashType.MD5, MessageDigest.getInstance("MD5"));
            hasherMap.put(FingerprintTuple.HashType.SHA1, MessageDigest.getInstance("SHA-1"));
            hasherMap.put(FingerprintTuple.HashType.SHA256, MessageDigest.getInstance("SHA-256"));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Cannot instantiate hashers", e);
        }
    }

    private void setResults(RDFFingerprintResponse response, List<Map<String, Object>> results) {
        List<RDFDict> dicts = new ArrayList<>();
        for (Map<String, Object> result : results) {
            String name = (String) result.get("name");
            if (name.equals("generic")) {
                RDFHash hash = new RDFHash();
                byte[] md5 = (byte[]) result.get("md5");
                if (md5 != null) {
                    hash.setMd5(new HashDigest(md5));
                }
                byte[] sha1 = (byte[]) result.get("sha1");
                if (sha1 != null) {
                    hash.setSha1(new HashDigest(sha1));
                }
                byte[] sha256 = (byte[]) result.get("sha256");
                if (sha256 != null) {
                    hash.setSha256(new HashDigest(sha256));
                }
                response.setHash(hash);
            }
            RDFDict dict = new RDFDict();
            dict.putAll(result);
            dicts.add(dict);
        }
        response.setResults(dicts);
    }

    @Override
    public void execute(Context context, RDFFingerprintRequest request,
            ActionCallback<RDFFingerprintResponse> callback) {
        try {
            VFSFile file = VFS.open(request.getPathspec(), callback::onUpdate);
            Fingerprinter fingerprinter = new Fingerprinter(file) {
                @Override
                protected long[] getNextInterval() {
                    callback.onUpdate();
                    return super.getNextInterval();
                }
            };

            RDFFingerprintResponse response = new RDFFingerprintResponse();
            response.setPathspec(file.getPathspec());

            List<RDFFingerprintTuple> tuples;
            if (request.getTuples() != null && request.getTuples().size() > 0) {
                tuples = request.getTuples();
            } else {
                tuples = new ArrayList<>();
                RDFFingerprintTuple tuple = new RDFFingerprintTuple();
                tuple.setFpType(FingerprintTuple.Type.FPT_GENERIC);
                tuples.add(tuple);
            }
            for (RDFFingerprintTuple finger : tuples) {
                List<MessageDigest> hashers = new ArrayList<>();
                for (FingerprintTuple.HashType hashType : finger.getHashers()) {
                    MessageDigest hasher = hasherMap.get(hashType);
                    if (hasher != null) {
                        hashers.add(hasher);
                    }
                }
                switch (finger.getFpType()) {
                    case FPT_GENERIC:
                        fingerprinter.evalGeneric(hashers.toArray(new MessageDigest[0]));
                        response.setMatchingTypes(finger.getFpType());
                        break;
                    case FPT_PE_COFF:
                        // not implemented
                    default:
                        throw new RuntimeException(
                                String.format("Encountered unknown fingerprint type. %s",
                                        finger.getFpType().name()));
                }
            }
            setResults(response, fingerprinter.hash());
            callback.onResponse(response);
            callback.onComplete();
        } catch (IOException e) {
            callback.onError(e);
        }
    }
}
