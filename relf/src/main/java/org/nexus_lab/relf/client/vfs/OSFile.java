package org.nexus_lab.relf.client.vfs;

import android.system.OsConstants;
import android.system.StructStat;

import org.nexus_lab.relf.lib.rdfvalues.RDFDatetimeSeconds;
import org.nexus_lab.relf.lib.rdfvalues.client.RDFStatEntry;
import org.nexus_lab.relf.lib.rdfvalues.client.StatMode;
import org.nexus_lab.relf.lib.rdfvalues.paths.RDFPathSpec;
import org.nexus_lab.relf.proto.PathSpec;
import org.nexus_lab.relf.service.RelfService;
import org.nexus_lab.relf.utils.PathUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;

import lombok.Getter;

/**
 * OS implementation of VFS file
 */
public class OSFile extends VFSFile {
    /**
     * Absolute path of the file to the parent {@link VFSFile}
     */
    private final String path;

    /**
     * Child file names if current {@link VFSFile} is a directory
     */
    private String[] files;

    /**
     * If this {@link VFSFile} is a directory
     */
    @Getter
    private boolean directory;

    /**
     * @inheritDoc
     */
    public OSFile(VFSFile parent, RDFPathSpec child, ProgressCallback callback) {
        super(parent, child, callback);

        path = PathUtils.concat(pathspec.last().getPath(), child.getPath());
        pathspec.last().setPath(path);
        pathspec.last().setPathOptions(PathSpec.Options.CASE_LITERAL);
        if (pathspec.getOffset() != null) {
            seek(pathspec.getOffset());
        }

        directory = RelfService.isdir(path);
        if (directory) {
            this.files = RelfService.listdir(path);
        } else {
            if (child.last().getFileSizeOverride() != null) {
                this.size = child.last().getFileSizeOverride().getValue() - tell();
            } else {
                this.size = RelfService.length(path) - tell();
            }
        }
    }

    @Override
    public PathSpec.PathType getSupportedPathType() {
        return PathSpec.PathType.OS;
    }

    @Override
    public String[] list() {
        return files == null ? new String[0] : files;
    }

    @Override
    public byte[] read(long size) throws IOException {
        if (isDirectory()) {
            throw new IOException("Cannot read a directory.");
        }
        if (callback != null) {
            callback.update();
        }
        int i;
        int read = 0;
        int length = (int) Math.min(size, Math.max(0, size() - tell()));
        byte[] buffer = new byte[length];
        try (FileInputStream in = RelfService.open(path, tell(), length)) {
            do {
                i = in.read(buffer, read, length - read);
                if (i > -1) {
                    read += i;
                    seek(i, WHENCE_LAST_OFFSET);
                }
            } while (i > -1 && read < length);
        }
        return read > 0 ? Arrays.copyOf(buffer, read) : null;
    }

    @Override
    public RDFStatEntry stat() {
        String localPath = PathUtils.normalize(path);
        RDFStatEntry stat = new RDFStatEntry();
        stat.setPathspec(pathspec);
        try {
            StructStat st = RelfService.lstat(localPath);
            stat.setStMode(new StatMode(st.st_mode));
            stat.setStIno((int) st.st_ino);
            stat.setStDev((int) st.st_dev);
            stat.setStNlink((int) st.st_nlink);
            stat.setStUid(st.st_uid);
            stat.setStGid(st.st_gid);
            stat.setStSize(st.st_size);
            stat.setStBlksize((int) st.st_blksize);
            stat.setStBlocks((int) st.st_blocks);
            stat.setStRdev((int) st.st_rdev);
            stat.setStAtime(new RDFDatetimeSeconds(st.st_atime));
            stat.setStMtime(new RDFDatetimeSeconds(st.st_mtime));
            stat.setStCtime(new RDFDatetimeSeconds(st.st_ctime));
            if (OsConstants.S_ISLNK(st.st_mode)) {
                stat.setSymlink(RelfService.readlink(localPath));
            }
        } catch (IOException ignored) {
        }
        return stat;
    }

    @Override
    public VFSFile[] listFiles() {
        String[] children = list();
        VFSFile[] result = new VFSFile[children.length];
        for (int i = 0; i < children.length; i++) {
            RDFPathSpec child = new RDFPathSpec();
            child.setPath(children[i]);
            child.setPathtype(PathSpec.PathType.OS);
            result[i] = new OSFile(this, child, null);
        }
        return result;
    }

    @Override
    public String getName() {
        return PathUtils.getName(getAbsolutePath());
    }

    @Override
    public String getAbsolutePath() {
        return PathUtils.normalize(path);
    }
}
