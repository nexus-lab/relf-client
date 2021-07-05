package org.nexus_lab.relf.service;

import android.annotation.SuppressLint;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import android.system.StructStat;
import android.system.StructStatVfs;
import android.util.Log;

import androidx.annotation.Nullable;

import org.nexus_lab.relf.service.parcel.ParcelStructStat;
import org.nexus_lab.relf.service.parcel.ParcelStructStatvfs;
import org.nexus_lab.relf.service.struct.StructPasswd;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;

import static org.nexus_lab.relf.utils.ReflectionUtils.getFieldValue;

/**
 * ReLF native service helper class.
 * If native binder service `relf_service` exists and is accessible, we use this class
 * to communicate with it and call some low-level OS functions. Otherwise, fallback to
 * the Android framework ones.
 *
 * @author Ruipeng Zhang
 */
public final class RelfService {
    private static final String TAG = RelfService.class.getSimpleName();
    private static final String SERVICE_NAME = "relf_service";

    @SuppressLint("PrivateApi")
    private static IRelfService getService() {
        try {
            Method getService = Class.forName("android.os.ServiceManager").getMethod("getService", String.class);
            IBinder binder = (IBinder) getService.invoke(null, SERVICE_NAME);
            if (binder != null) {
                return IRelfService.Stub.asInterface(binder);
            }
        } catch (ReflectiveOperationException e) {
            Log.w(TAG, "Cannot access the native RelfService.", e);
        }
        return null;
    }

    @SuppressLint("PrivateApi")
    private static int getErrno(Exception e) {
        try {
            if (e.getClass().equals(Class.forName("android.os.ServiceSpecificException"))) {
                return getFieldValue(e, "errorCode");
            }
        } catch (ReflectiveOperationException ignored) {
        }
        return -1;
    }

    /**
     * @return whether the native binder exists and is working.
     */
    public static boolean isNativeServiceActive() {
        return getService() != null;
    }

    /**
     * See <a href="http://man7.org/linux/man-pages/man2/stat.2.html">stat(2)</a>.
     *
     * @param path file path to be inspected.
     * @return file status.
     * @throws IOException file does not exist or cannot read file.
     * @see android.system.Os#stat(String)
     */
    public static StructStat stat(String path) throws IOException {
        try {
            if (!isNativeServiceActive()) {
                return Os.stat(path);
            }
            ParcelStructStat stat = getService().stat(path);
            return new StructStat(
                    stat.st_dev,
                    stat.st_ino,
                    (int) stat.st_mode,
                    stat.st_nlink,
                    (int) stat.st_uid,
                    (int) stat.st_gid,
                    stat.st_rdev,
                    stat.st_size,
                    stat.st_atime,
                    stat.st_mtime,
                    stat.st_ctime,
                    stat.st_blksize,
                    stat.st_blocks
            );
        } catch (Exception e) {
            int errno = e instanceof ErrnoException ? ((ErrnoException) e).errno : getErrno(e);
            if (errno == OsConstants.ENOENT) {
                throw new FileNotFoundException(path);
            }
            throw new IOException(e);
        }
    }

    /**
     * Same as {@link #stat(String)} but does not follow the link.
     * See <a href="http://man7.org/linux/man-pages/man2/stat.2.html">stat(2)</a>.
     *
     * @param path file path to be inspected.
     * @return file status.
     * @throws IOException file does not exist or cannot read file.
     * @see android.system.Os#stat(String)
     */
    public static StructStat lstat(String path) throws IOException {
        try {
            if (!isNativeServiceActive()) {
                return Os.stat(path);
            }
            ParcelStructStat stat = getService().lstat(path);
            return new StructStat(
                    stat.st_dev,
                    stat.st_ino,
                    (int) stat.st_mode,
                    stat.st_nlink,
                    (int) stat.st_uid,
                    (int) stat.st_gid,
                    stat.st_rdev,
                    stat.st_size,
                    stat.st_atime,
                    stat.st_mtime,
                    stat.st_ctime,
                    stat.st_blksize,
                    stat.st_blocks
            );
        } catch (Exception e) {
            int errno = e instanceof ErrnoException ? ((ErrnoException) e).errno : getErrno(e);
            if (errno == OsConstants.ENOENT) {
                throw new FileNotFoundException(path);
            }
            throw new IOException(e);
        }
    }

    /**
     * See <a href="http://man7.org/linux/man-pages/man2/statvfs.2.html">stat(2)</a>.
     *
     * @param path file system mount point.
     * @return file system status.
     * @throws IOException file does not exist or cannot read file.
     * @see android.system.Os#statvfs(String)
     */
    public static StructStatVfs statvfs(String path) throws IOException {
        try {
            if (!isNativeServiceActive()) {
                return Os.statvfs(path);
            }
            ParcelStructStatvfs stat = getService().statvfs(path);
            return new StructStatVfs(
                    stat.f_bsize,
                    stat.f_frsize,
                    stat.f_blocks,
                    stat.f_bfree,
                    stat.f_bavail,
                    stat.f_files,
                    stat.f_ffree,
                    stat.f_favail,
                    stat.f_fsid,
                    stat.f_flag,
                    stat.f_namemax
            );
        } catch (Exception e) {
            int errno = e instanceof ErrnoException ? ((ErrnoException) e).errno : getErrno(e);
            if (errno == OsConstants.ENOENT) {
                throw new FileNotFoundException(path);
            }
            throw new IOException(e);
        }
    }

    /**
     * @param path path of the file to be checked.
     * @return true if the file exists.
     * @see File#exists()
     */
    public static boolean exists(String path) {
        if (!isNativeServiceActive()) {
            return new File(path).exists();
        }
        try {
            stat(path);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * @param path path of the file to be checked.
     * @return true if the file is a directory.
     * @see File#isDirectory()
     */
    public static boolean isdir(String path) {
        if (!isNativeServiceActive()) {
            return new File(path).isDirectory();
        }
        try {
            StructStat stat = stat(path);
            return OsConstants.S_ISDIR(stat.st_mode);
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * @param path path of the directory to be listed.
     * @return a list of files under the given directory, or null if the given path is not a
     * directory.
     * @see File#list()
     */
    @Nullable
    public static String[] listdir(String path) {
        if (!isNativeServiceActive()) {
            return new File(path).list();
        }
        try {
            return isdir(path) ? getService().listdir(path) : null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Get the length of a file.
     *
     * @param path file to be inspected.
     * @return the file length in bytes.
     * @see File#length()
     */
    public static long length(String path) {
        if (!isNativeServiceActive()) {
            return new File(path).length();
        }
        try {
            StructStat stat = stat(path);
            return stat.st_size;
        } catch (IOException e) {
            return 0;
        }
    }

    /**
     * Read the path to which the symbolic link points.
     *
     * @param path symbolic file path.
     * @return the path that the symbolic file points to.
     * @throws IOException invalid path or error finding the file.
     * @see Os#readlink(String)
     */
    public static String readlink(String path) throws IOException {
        try {
            if (!isNativeServiceActive()) {
                return Os.readlink(path);
            }
            return getService().readlink(path);
        } catch (Exception e) {
            int errno = e instanceof ErrnoException ? ((ErrnoException) e).errno : getErrno(e);
            if (errno == OsConstants.ENOENT) {
                throw new FileNotFoundException(path);
            }
            throw new IOException(e);
        }
    }

    /**
     * Get the canonical path of the file, which follows the link and normalizes /./ and /../
     *
     * @param path path to the file.
     * @return canonical path of the file.
     * @throws IOException invalid path or error finding the file.
     */
    public static String realpath(String path) throws IOException {
        if (!isNativeServiceActive()) {
            return new File(path).getCanonicalPath();
        }
        try {
            return getService().realpath(path);
        } catch (Exception e) {
            if (getErrno(e) == OsConstants.ENOENT) {
                throw new FileNotFoundException(path);
            }
            throw new IOException(e);
        }
    }

    /**
     * Read a file in read-only mode and receive the content through a pipe.
     *
     * @param path file path.
     * @return the file content stream.
     * @throws IOException file does not exist or cannot read file.
     */
    public static FileInputStream open(String path) throws IOException {
        return open(path, 0, -1);
    }

    /**
     * Read a file in read-only mode and receive the content through a pipe.
     *
     * @param path file path.
     * @param offset number of bytes from file beginning to skip when reading the file.
     * @param length maximum number of bytes to be read from the pipe. -1 if read to the end.
     * @return the file content stream.
     * @throws IOException file does not exist or cannot read file.
     */
    public static FileInputStream open(String path, long offset, long length) throws IOException {
        if (!isNativeServiceActive()) {
            FileInputStream in = new FileInputStream(path);
            in.skip(offset);
            return in;
        }
        try {
            ParcelFileDescriptor[] pipe = ParcelFileDescriptor.createPipe();
            ParcelFileDescriptor read = pipe[0].dup();
            ParcelFileDescriptor write = pipe[1].dup();
            pipe[0].close();
            pipe[1].close();
            getService().pipe(write, path, offset, length);
            write.close();
            return new FileInputStream(read.getFileDescriptor()) {
                // keep a reference to the parcel so the file descriptor wouldn't be destroyed.
                private ParcelFileDescriptor ref = read;

                @Override
                public void close() throws IOException {
                    super.close();
                    if (ref != null) {
                        ref.close();
                        ref = null;
                    }
                }
            };
        } catch (Exception e1) {
            if (getErrno(e1) == OsConstants.ENOENT) {
                throw new FileNotFoundException(path);
            }
            throw new IOException(e1);
        }
    }

    /**
     * List all Android Linux users (except for OEM users and APP users).
     *
     * @return a list of Linux users or null if native service is unavailable.
     * @throws IOException failed to invoke getpwent system call.
     */
    @Nullable
    public static StructPasswd[] getpwent() throws IOException {
        if (isNativeServiceActive()) {
            try {
                return getService().getpwent();
            } catch (RemoteException e) {
                throw new IOException(e);
            }
        }
        return null;
    }

    /**
     * Get passwd struct of a specific process.
     *
     * @param uid the target user ID.
     * @return the {@link StructPasswd} of a specific process.
     * @throws IOException failed to invoke getpwuid system call.
     */
    @Nullable
    public static StructPasswd getpwuid(int uid) throws IOException {
        if (isNativeServiceActive()) {
            try {
                return getService().getpwuid(uid);
            } catch (RemoteException e) {
                throw new IOException(e);
            }
        }
        return null;
    }

    /**
     * A utility class to read a line from a file.
     */
    public static class LineReader implements Closeable {
        private final String path;
        private BufferedReader reader;

        /**
         * @param path file path.
         */
        public LineReader(String path) {
            this.path = path;
        }

        /**
         * @return the next line in the file.
         * @throws IOException cannot open the file.
         */
        public String readLine() throws IOException {
            if (reader == null) {
                reader = new BufferedReader(new InputStreamReader(RelfService.open(path)));
            }
            return reader.readLine();
        }

        /**
         * Closes the opened file stream.
         *
         * @throws IOException failed to close the file stream.
         */
        @Override
        public void close() throws IOException {
            if (reader != null) {
                reader.close();
                reader = null;
            }
        }
    }
}
