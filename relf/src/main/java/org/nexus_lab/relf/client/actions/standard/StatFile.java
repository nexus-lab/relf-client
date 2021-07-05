package org.nexus_lab.relf.client.actions.standard;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;

import android.content.Context;

import org.nexus_lab.relf.client.actions.Action;
import org.nexus_lab.relf.client.actions.ActionCallback;
import org.nexus_lab.relf.client.actions.UsesPermission;
import org.nexus_lab.relf.client.vfs.VFS;
import org.nexus_lab.relf.client.vfs.VFSFile;
import org.nexus_lab.relf.lib.rdfvalues.client.RDFListDirRequest;
import org.nexus_lab.relf.lib.rdfvalues.client.RDFStatEntry;

import java.io.IOException;

/**
 * @author Ruipeng Zhang
 */
@UsesPermission(READ_EXTERNAL_STORAGE)
public class StatFile implements Action<RDFListDirRequest, RDFStatEntry> {

    @Override
    public void execute(Context context, RDFListDirRequest request, ActionCallback<RDFStatEntry> callback) {
        try {
            VFSFile file = VFS.open(request.getPathspec(), callback::onUpdate);
            if (file != null) {
                callback.onResponse(file.stat());
            }
            callback.onComplete();
        } catch (IOException e) {
            callback.onError(e);
        }
    }
}
