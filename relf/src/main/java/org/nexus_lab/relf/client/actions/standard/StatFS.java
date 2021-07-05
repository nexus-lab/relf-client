package org.nexus_lab.relf.client.actions.standard;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;

import android.content.Context;
import android.system.StructStatVfs;

import org.nexus_lab.relf.client.actions.Action;
import org.nexus_lab.relf.client.actions.ActionCallback;
import org.nexus_lab.relf.client.actions.UsesPermission;
import org.nexus_lab.relf.client.actions.linux.EnumerateFilesystems;
import org.nexus_lab.relf.lib.rdfvalues.client.RDFFilesystem;
import org.nexus_lab.relf.lib.rdfvalues.client.RDFStatFSRequest;
import org.nexus_lab.relf.lib.rdfvalues.client.RDFUnixVolume;
import org.nexus_lab.relf.lib.rdfvalues.client.RDFVolume;
import org.nexus_lab.relf.lib.rdfvalues.paths.RDFPathSpec;
import org.nexus_lab.relf.service.RelfService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Ruipeng Zhang
 */
@UsesPermission(READ_EXTERNAL_STORAGE)
public class StatFS implements Action<RDFStatFSRequest, RDFVolume> {
    private List<String> mountPoints = new ArrayList<>();
    private ActionCallback<RDFFilesystem> fsCallback = response -> mountPoints.add(response.getMountPoint());

    private String findMountPoint(String path) {
        String result = null;
        for (String mp : mountPoints) {
            if (path.startsWith(mp) && (result == null || result.length() < mp.length())) {
                result = mp;
            }
        }
        return result;
    }

    @Override
    public void execute(Context context, RDFStatFSRequest request, ActionCallback<RDFVolume> callback) {
        new EnumerateFilesystems().execute(context, null, fsCallback);
        try {
            for (String path : request.getPathList()) {
                RDFPathSpec pathspec = new RDFPathSpec();
                pathspec.setPath(path);
                pathspec.setPathtype(request.getPathtype());
                RDFVolume volume = new RDFVolume();

                StructStatVfs stat = RelfService.statvfs(path);
                volume.setBytesPerSector(stat.f_bsize);
                volume.setSectorsPerAllocationUnit(1L);
                volume.setTotalAllocationUnits(stat.f_blocks);
                volume.setActualAvailableAllocationUnits(stat.f_bavail);

                RDFUnixVolume unixVolume = new RDFUnixVolume();
                unixVolume.setMountPoint(findMountPoint(path));

                volume.setUnixvolume(unixVolume);
                callback.onResponse(volume);
            }
            callback.onComplete();
        } catch (IOException e) {
            callback.onError(e);
        }
    }
}
