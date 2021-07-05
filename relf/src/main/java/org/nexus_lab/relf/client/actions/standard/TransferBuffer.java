package org.nexus_lab.relf.client.actions.standard;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;

import android.content.Context;

import org.nexus_lab.relf.client.actions.Action;
import org.nexus_lab.relf.client.actions.ActionCallback;
import org.nexus_lab.relf.client.actions.UsesPermission;
import org.nexus_lab.relf.client.vfs.VFS;
import org.nexus_lab.relf.client.vfs.VFSFile;
import org.nexus_lab.relf.lib.Constants;
import org.nexus_lab.relf.lib.rdfvalues.RDFValue;
import org.nexus_lab.relf.lib.rdfvalues.TransferStore;
import org.nexus_lab.relf.lib.rdfvalues.client.RDFBufferReference;
import org.nexus_lab.relf.lib.rdfvalues.protodict.RDFDataBlob;
import org.nexus_lab.relf.proto.DataBlob;
import org.nexus_lab.relf.utils.ByteUtils;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author Ruipeng Zhang
 */
@UsesPermission(READ_EXTERNAL_STORAGE)
public class TransferBuffer implements Action<RDFBufferReference, RDFValue> {
    @Override
    public void execute(Context context, RDFBufferReference request, ActionCallback<RDFValue> callback) {
        if (request.getLength() > Constants.CLIENT_MAX_BUFFER_SIZE) {
            throw new RuntimeException("Can not read buffers this large.");
        }
        try {
            VFSFile file = VFS.open(request.getPathspec(), callback::onUpdate);
            file.seek(request.getOffset(), 0);
            byte[] data = file.read(request.getLength());
            if (data == null) {
                throw new RuntimeException("No more data to read");
            }
            RDFDataBlob result = new RDFDataBlob();
            result.setData(ByteUtils.compress(data));
            result.setCompression(DataBlob.CompressionType.ZCOMPRESSION);
            callback.onResponse(new TransferStore(result, data.length));

            MessageDigest hasher = MessageDigest.getInstance("SHA-256");
            hasher.update(data);

            RDFBufferReference reference = new RDFBufferReference();
            reference.setData(hasher.digest());
            reference.setLength(data.length);
            reference.setOffset(request.getOffset());
            callback.onResponse(reference);
            callback.onComplete();
        } catch (IOException | NoSuchAlgorithmException e) {
            callback.onError(e);
        }
    }
}
