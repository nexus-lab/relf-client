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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Ruipeng Zhang
 */
@UsesPermission(READ_EXTERNAL_STORAGE)
public class ListDirectory implements Action<RDFListDirRequest, RDFStatEntry> {
    @Override
    public void execute(Context context, RDFListDirRequest request, ActionCallback<RDFStatEntry> callback) {
        try {
            VFSFile directory = VFS.open(request.getPathspec(), callback::onUpdate);
            if (directory != null) {
                List<VFSFile> files = Arrays.asList(directory.listFiles());
                Collections.sort(files, (a, b) ->
                        String.CASE_INSENSITIVE_ORDER.compare(a.getPathspec().collapse(), b.getPathspec().collapse()));
                for (VFSFile file : files) {
                    callback.onResponse(file.stat());
                }
            }
            callback.onComplete();
        } catch (IOException e) {
            callback.onError(e);
        }
    }
}
