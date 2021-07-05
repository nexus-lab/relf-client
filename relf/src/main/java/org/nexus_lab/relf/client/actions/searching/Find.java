package org.nexus_lab.relf.client.actions.searching;

import android.content.Context;
import android.util.Log;

import org.nexus_lab.relf.client.actions.Action;
import org.nexus_lab.relf.client.actions.ActionCallback;
import org.nexus_lab.relf.client.actions.UsesPermission;
import org.nexus_lab.relf.client.vfs.VFS;
import org.nexus_lab.relf.client.vfs.VFSFile;
import org.nexus_lab.relf.lib.rdfvalues.RDFValue;
import org.nexus_lab.relf.lib.rdfvalues.client.RDFFindSpec;
import org.nexus_lab.relf.lib.rdfvalues.client.RDFStatEntry;
import org.nexus_lab.relf.lib.rdfvalues.paths.RDFPathSpec;
import org.nexus_lab.relf.lib.rdfvalues.protodict.RDFDict;
import org.nexus_lab.relf.proto.Iterator;

import org.bouncycastle.util.Arrays;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;

/**
 * @author Ruipeng Zhang
 */
@UsesPermission(READ_EXTERNAL_STORAGE)
public class Find implements Action<RDFFindSpec, RDFValue> {
    private static final String TAG = Find.class.getSimpleName();

    private RDFFindSpec request;
    private ActionCallback<RDFValue> callback;
    private Integer filesystemId;

    private List<RDFStatEntry> listDirectory(RDFPathSpec pathspec, RDFDict state, int depth)
            throws IOException {
        List<RDFStatEntry> result = new ArrayList<>();
        if (depth >= request.getMaxDepth()) {
            return result;
        }
        VFSFile file;
        try {
            file = VFS.open(pathspec, callback::onUpdate);
        } catch (IOException e) {
            if (depth == 0) {
                throw e;
            } else {
                Log.w(TAG, "Find action failed to listDirectory for " + pathspec, e);
            }
            return result;
        }
        if (!request.isCrossDevs() && filesystemId == null) {
            filesystemId = file.stat().getStDev();
        }
        int start = (int) (state.get(pathspec.collapse()) == null ? 0 : state.get(
                pathspec.collapse()));
        VFSFile[] fileStats = file.listFiles();
        for (int i = 0; i < fileStats.length; i++) {
            if (i < start) {
                continue;
            }
            RDFStatEntry fileStat = fileStats[i].stat();
            // is it a directory
            if (fileStat.getStMode() != null
                    && (fileStat.getStMode().getValue() & 0170000) == 0040000) {
                if (request.isCrossDevs() || Objects.equals(filesystemId, fileStat.getStDev())) {
                    result.addAll(listDirectory(fileStat.getPathspec(), state, depth + 1));
                }
            }
            state.put(pathspec.collapse(), i + 1);
            result.add(fileStat);
        }
        try {
            state.remove(pathspec.collapse());
        } catch (Exception ignored) {
        }
        return result;
    }

    private boolean testFileContent(RDFStatEntry stat) {
        byte[] data = new byte[0];
        try {
            VFSFile file = VFS.open(stat.getPathspec(), callback::onUpdate);
            while (file.tell() < request.getMaxData()) {
                byte[] dataRead = file.read(1024000);
                if (dataRead == null) {
                    break;
                }
                data = Arrays.concatenate(data, dataRead);
                Matcher matcher = request.getDataRegex().match(new String(data));
                if (matcher != null && matcher.matches()) {
                    return true;
                }
                data = Arrays.copyOfRange(data, Math.max(0, data.length - 100), data.length);
            }
        } catch (IOException ignored) {
        }
        return false;
    }

    private List<Filter> buildFilters(RDFFindSpec request) {
        List<Filter> filters = new ArrayList<>();
        if (request.getStartTime() != null || request.getEndTime() != null) {
            filters.add(stat -> stat.getStMtime() != null
                    && (stat.getStMtime().asSecondsFromEpoch()
                    < request.getStartTime().asSecondsFromEpoch()
                    || stat.getStMtime().asSecondsFromEpoch()
                    > request.getEndTime().asSecondsFromEpoch()));
        }
        if (request.getMinFileSize() != null || request.getMaxFileSize() != null) {
            filters.add(
                    stat -> stat.getStSize() != null && (stat.getStSize() < request.getMinFileSize()
                            || stat.getStSize() > request.getMaxFileSize()));
        }
        if (request.getPermMode() != null) {
            filters.add(stat -> (stat.getStMode().getValue() & request.getPermMask())
                    != request.getPermMode());
        }
        if (request.getUid() != null) {
            filters.add(stat -> stat.getStUid() != request.getUid().intValue());
        }
        if (request.getGid() != null) {
            filters.add(stat -> stat.getStGid() != request.getGid().intValue());
        }
        if (request.getPathRegex() != null) {
            filters.add(stat -> {
                Matcher matcher = request.getPathRegex().match(stat.getPathspec().basename());
                return matcher == null || !matcher.matches();
            });
        }
        if (request.getDataRegex() != null) {
            filters.add(stat -> !testFileContent(stat));
        }
        return filters;
    }

    private void iterate(RDFFindSpec request, RDFDict clientState,
                         ActionCallback<RDFValue> callback) throws IOException {
        this.request = request;
        this.callback = callback;
        List<Filter> filters = buildFilters(request);
        Integer limit = request.getIterator().getNumber();

        List<RDFStatEntry> stats = listDirectory(request.getPathspec(), clientState, 0);
        for (int i = 0; i < stats.size(); i++) {
            callback.onUpdate();
            boolean allPass = true;
            for (Filter filter : filters) {
                if (filter.check(stats.get(i))) {
                    allPass = false;
                    break;
                }
            }
            if (allPass) {
                RDFFindSpec response = new RDFFindSpec();
                response.setHit(stats.get(i));
                callback.onResponse(response);
            }
            if (i >= limit - 1) {
                Log.i(TAG, "Processed " + i + 1 + " entries, quitting...");
                return;
            }
        }
        request.getIterator().setState(Iterator.State.FINISHED);
    }

    @Override
    public void execute(Context context, RDFFindSpec request, ActionCallback<RDFValue> callback) {
        try {
            RDFDict clientState = request.getIterator().getClientState();
            if (clientState == null) {
                clientState = new RDFDict();
            }
            iterate(request, clientState, callback);
            callback.onResponse(request.getIterator());
            callback.onComplete();
        } catch (IOException e) {
            callback.onError(e);
        }
    }

    /**
     * A template for file matching
     */
    private interface Filter {
        /**
         * Check a given file status matches
         *
         * @param stat file status
         * @return true if file matches
         */
        boolean check(RDFStatEntry stat);
    }
}
