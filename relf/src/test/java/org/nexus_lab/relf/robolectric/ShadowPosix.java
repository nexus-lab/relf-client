package org.nexus_lab.relf.robolectric;


import static org.robolectric.util.ReflectionHelpers.ClassParameter.from;
import static org.robolectric.util.ReflectionHelpers.callInstanceMethod;

import android.os.Build;
import android.system.ErrnoException;
import android.system.OsConstants;
import android.system.StructStat;
import android.system.StructStatVfs;
import android.system.StructUtsname;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Robolectric shadow {@link libcore.io.Posix} class for testing.
 *
 * @author Ruipeng Zhang
 */
@Implements(
        className = "libcore.io.Posix",
        maxSdk = Build.VERSION_CODES.N_MR1, isInAndroidSdk = false)
public class ShadowPosix extends org.robolectric.shadows.ShadowPosix {
    /**
     * See <a href="http://man7.org/linux/man-pages/man2/stat.2.html">stat(2)</a>.
     *
     * @param path file path.
     * @return file status.
     * @throws ErrnoException failed to access file.
     */
    @Implementation
    public static StructStat stat(String path) throws ErrnoException {
        try {
            Process exec = Runtime.getRuntime().exec(new String[]{
                    "stat", "-c", "%d %i %f %h %u %g %t %T %s %X %Y %Z %o %b", path});
            InputStream in = exec.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String line = reader.readLine();
            try {
                exec.waitFor();
            } catch (InterruptedException ignored) {
            }
            String[] values = line.split("\\s+");
            return new StructStat(
                    Long.parseLong(values[0]), // st_dev
                    Long.parseLong(values[1]), // st_ino
                    Integer.parseInt(values[2], 16), // st_mode
                    Long.parseLong(values[3]), // st_nlink
                    Integer.parseInt(values[4]), // st_uid
                    Integer.parseInt(values[5]), // st_gid
                    Long.parseLong(values[6], 16) << 20 | Long.parseLong(values[7], 16), // st_rdev
                    Long.parseLong(values[8]), // st_size
                    Long.parseLong(values[9]), // st_atime
                    Long.parseLong(values[10]), // st_mtime
                    Long.parseLong(values[11]), // st_ctime,
                    Long.parseLong(values[12]), // st_blksize
                    Long.parseLong(values[13]) // st_blocks
            );
        } catch (IOException e) {
            throw new ErrnoException(e.getMessage(), OsConstants.ENOENT, e);
        }
    }

    /**
     * See <a href="http://man7.org/linux/man-pages/man2/uname.2.html">uname(2)</a>.
     *
     * @return uname.
     */
    @Implementation
    protected StructUtsname uname() {
        return new StructUtsname("Robolectric", "robolectric", "2.6.35-27-generic",
                "#48-Ubuntu SMP Tue Feb 22 20:25:29 UTC 2011", "x86_64");
    }

    /**
     * See <a href="http://man7.org/linux/man-pages/man2/getpid.2.html">getpid(2)</a>.
     *
     * @return process ID.
     */
    @Implementation
    protected int getpid() {
        try {
            return Integer.parseInt(new File("/proc/self").getCanonicalFile().getName());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * See <a href="http://man7.org/linux/man-pages/man2/statvfs.2.html">statvfs(2)</a>.
     *
     * @param path file path.
     * @return filesystem status.
     * @throws ErrnoException failed to access file.
     */
    @Implementation
    protected StructStatVfs statvfs(String path) throws ErrnoException {
        return new StructStatVfs(
                1, // f_bsize
                2, // f_frsize
                3, // f_blocks
                4, // f_bfree
                5, // f_bavail
                6, // f_files
                7, // f_ffree
                8, // f_favail
                9, // f_fsid
                10, // f_flag
                11 // f_namemax
        );
    }

    /**
     * See <a href="http://man7.org/linux/man-pages/man2/readlink.2.html">readlink(2)</a>.
     *
     * @param path file path.
     * @return filesystem status.
     * @throws ErrnoException failed to access file.
     */
    @Implementation
    protected String readlink(String path) throws ErrnoException {
        try {
            return Files.readSymbolicLink(Paths.get(path)).toString();
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Open a file and return its file descriptor.
     *
     * @param path  file path.
     * @param flags open flags.
     * @param mode  file open mode.
     * @return file descriptor.
     * @throws ErrnoException failed to open the given file.
     * @see ShadowLinux#open(String, int, int)
     * @see <a href="http://man7.org/linux/man-pages/man2/open.2.html">open(2)</a>.
     */
    @Implementation
    protected FileDescriptor open(String path, int flags, int mode) throws ErrnoException {
        ShadowLinux shadowLinux = new ShadowLinux();
        return callInstanceMethod(shadowLinux, "open", from(String.class, path),
                from(int.class, flags), from(int.class, mode));
    }

    /**
     * Create a named pipe.
     *
     * @param path the path to the named pipe.
     * @param mode DAC mode of the pipe.
     * @throws ErrnoException error creating the pipe.
     * @see <a href="http://man7.org/linux/man-pages/man3/mkfifo.3.html">mkfifo(3)</a>.
     */
    @Implementation
    protected void mkfifo(String path, int mode) throws ErrnoException {
        try {
            Process process = Runtime.getRuntime().exec("mkfifo " + path);
            process.waitFor();
        } catch (IOException e) {
            throw new ErrnoException("mkfifo", -1, e);
        } catch (InterruptedException ignored) {
        }
    }
}
