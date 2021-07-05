package org.nexus_lab.relf.robolectric;

import android.os.Build;
import android.system.ErrnoException;
import android.system.StructStat;
import android.system.StructStatVfs;
import android.system.StructUtsname;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/**
 * Robolectric shadow {@link libcore.io.Linux} class for testing.
 *
 * @author Ruipeng Zhang
 */
@Implements(className = "libcore.io.Linux", minSdk = Build.VERSION_CODES.O, isInAndroidSdk = false)
public class ShadowLinux extends org.robolectric.shadows.ShadowLinux {
    /**
     * See <a href="http://man7.org/linux/man-pages/man2/uname.2.html">uname(2)</a>.
     *
     * @return uname
     */
    @Implementation
    protected StructUtsname uname() {
        return new ShadowPosix().uname();
    }

    /**
     * See <a href="http://man7.org/linux/man-pages/man2/getpid.2.html">getpid(2)</a>.
     *
     * @return process ID
     */
    @Implementation
    protected int getpid() {
        return new ShadowPosix().getpid();
    }

    /**
     * See <a href="http://man7.org/linux/man-pages/man2/stat.2.html">stat(2)</a>.
     *
     * @param path file path
     * @return file status
     * @throws ErrnoException failed to access file
     */
    @Override
    public StructStat stat(String path) throws ErrnoException {
        return ShadowPosix.stat(path);
    }

    /**
     * See <a href="http://man7.org/linux/man-pages/man2/statvfs.2.html">statvfs(2)</a>.
     *
     * @param path file path
     * @return filesystem status
     * @throws ErrnoException failed to access file
     */
    @Implementation
    protected StructStatVfs statvfs(String path) throws ErrnoException {
        return new ShadowPosix().statvfs(path);
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
        return new ShadowPosix().readlink(path);
    }

    /**
     * See <a href="http://man7.org/linux/man-pages/man3/mkfifo.3.html">mkfifo(3)</a>.
     *
     * @param path the path to the named pipe.
     * @param mode DAC mode of the pipe.
     * @throws ErrnoException error creating the pipe.
     */
    @Implementation
    protected void mkfifo(String path, int mode) throws ErrnoException {
        new ShadowPosix().mkfifo(path, mode);
    }
}
