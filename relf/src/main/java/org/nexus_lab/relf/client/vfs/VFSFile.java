package org.nexus_lab.relf.client.vfs;

import androidx.annotation.Nullable;

import org.nexus_lab.relf.lib.rdfvalues.client.RDFStatEntry;
import org.nexus_lab.relf.lib.rdfvalues.paths.RDFPathSpec;
import org.nexus_lab.relf.proto.PathSpec;

import java.io.IOException;

import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * Client-side ReLF VFS(Virtual File System) file representation.
 * It is basically a file/directory handler
 */
public abstract class VFSFile {
    /**
     * {@link #seek(long, int)} from start of the file.
     */
    public static final int WHENCE_START = 0;

    /**
     * {@link #seek(long, int)} from last {@link #seek(long, int)} offset of the file.
     */
    public static final int WHENCE_LAST_OFFSET = 1;

    /**
     * {@link #seek(long, int)} from end of the file.
     */
    public static final int WHENCE_END = 2;
    /**
     * Current file path in {@link RDFPathSpec}
     */
    @Getter
    protected final RDFPathSpec pathspec;
    /**
     * File size
     */
    @Getter
    @Accessors(fluent = true)
    protected long size;
    /**
     * A callback to indicate that the open call is still working but needs more time.
     */
    protected ProgressCallback callback;

    /**
     * File cursor offset
     */
    private long offset;

    /**
     * @param parent   parent directory {@link VFSFile} object
     * @param child    child {@link RDFPathSpec} relative to the parent
     * @param callback VFS open operation processing callback
     */
    public VFSFile(VFSFile parent, RDFPathSpec child, ProgressCallback callback) {
        this.pathspec = parent == null ? new RDFPathSpec() : (RDFPathSpec) parent.pathspec.duplicate();
        this.pathspec.setPathtype(getSupportedPathType());
        if (parent != null && !parent.isDirectory()) {
            throw new IllegalArgumentException(
                    "Cannot open the child path from a non-directory parent");
        }
        this.callback = callback;
    }

    /**
     * @return supported {@link PathSpec.PathType} to open
     */
    public abstract PathSpec.PathType getSupportedPathType();

    /**
     * Read content from file for a certain size.
     *
     * @param size read content size.
     * @return file content or null if no more data to read.
     * @throws IOException if failed to read a file.
     */
    @Nullable
    public abstract byte[] read(long size) throws IOException;

    /**
     * Seek to an offset in the file.
     *
     * @param offset seek offset.
     */
    public void seek(long offset) {
        seek(offset, 0);
    }

    /**
     * Seek to an offset in the file.
     *
     * @param offset seek offset
     * @param whence {@link VFSFile#WHENCE_START}: from file start;
     *               {@link VFSFile#WHENCE_LAST_OFFSET}: from current offset;
     *               {@link VFSFile#WHENCE_END}: from file end
     *               default: {@link VFSFile#WHENCE_START}
     */
    public void seek(long offset, int whence) {
        switch (whence) {
            case WHENCE_LAST_OFFSET:
                this.offset += offset;
                break;
            case WHENCE_END:
                this.offset = this.size + offset;
                break;
            case WHENCE_START:
            default:
                this.offset = offset;
                break;
        }
    }

    /**
     * @return current file offset
     */
    public long tell() {
        return offset;
    }

    /**
     * @return file stat record
     */
    public abstract RDFStatEntry stat();

    /**
     * @return whether the current file is a directory
     */
    public abstract boolean isDirectory();

    /**
     * @return current file name
     */
    public abstract String getName();

    /**
     * @return current file absolute path
     */
    public abstract String getAbsolutePath();

    /**
     * @return all files and directory name under current directory
     */
    public abstract String[] list();

    /**
     * An iterator over all VFS files contained in this directory.
     *
     * @return a list of {@link VFSFile} for each file or directory
     */
    public abstract VFSFile[] listFiles();

    /**
     * A callback to indicate that the VFS open call is still working but needs more time.
     */
    public interface ProgressCallback {
        /**
         * Called when progress needs to be updated
         */
        void update();
    }
}
