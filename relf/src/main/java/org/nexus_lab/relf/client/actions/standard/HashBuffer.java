package org.nexus_lab.relf.client.actions.standard;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;

import android.content.Context;

import org.nexus_lab.relf.client.actions.Action;
import org.nexus_lab.relf.client.actions.ActionCallback;
import org.nexus_lab.relf.client.actions.UsesPermission;
import org.nexus_lab.relf.client.vfs.VFS;
import org.nexus_lab.relf.client.vfs.VFSFile;
import org.nexus_lab.relf.lib.Constants;
import org.nexus_lab.relf.lib.rdfvalues.client.RDFBufferReference;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author Ruipeng Zhang
 */
@UsesPermission(READ_EXTERNAL_STORAGE)
public class HashBuffer implements Action<RDFBufferReference, RDFBufferReference> {
    @Override
    public void execute(Context context, RDFBufferReference reference, ActionCallback<RDFBufferReference> callback) {
        if (reference.getLength() > Constants.CLIENT_MAX_BUFFER_SIZE) {
            throw new RuntimeException("Can not read buffers this large.");
        }
        try {
            VFSFile file = VFS.open(reference.getPathspec(), callback::onUpdate);
            file.seek(reference.getOffset(), 0);
            byte[] data = file.read(reference.getLength());
            if (data == null) {
                throw new RuntimeException("No more data to read");
            }

            MessageDigest hasher = MessageDigest.getInstance("SHA-256");
            hasher.update(data);
            byte[] hash = hasher.digest();

            RDFBufferReference response = new RDFBufferReference();
            response.setData(hash);
            response.setOffset(reference.getOffset());
            response.setLength((long) data.length);
            callback.onResponse(response);
            callback.onComplete();
        } catch (IOException | NoSuchAlgorithmException e) {
            callback.onError(e);
        }
    }
}
