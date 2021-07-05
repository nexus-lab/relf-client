package org.nexus_lab.relf.robolectric;

import static org.robolectric.util.ReflectionHelpers.ClassParameter.from;

import android.system.StructStat;
import android.system.StructStatVfs;

import org.nexus_lab.relf.Constants;
import org.nexus_lab.relf.service.RelfService;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.shadow.api.Shadow;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Robolectric shadow {@link RelfService} class for testing.
 * @see RelfService
 *
 * @author Ruipeng Zhang
 */
@SuppressWarnings("javadocmethod")
@Implements(RelfService.class)
public class ShadowRelfService {
    /**
     * @see RelfService#exists(String)
     */
    @Implementation
    public static boolean exists(String path) {
        return Shadow.directlyOn(RelfService.class, "exists", from(String.class, Constants.fspath(path)));
    }

    /**
     * @see RelfService#isdir(String)
     */
    @Implementation
    public static boolean isdir(String path) {
        return Shadow.directlyOn(RelfService.class, "isdir", from(String.class, Constants.fspath(path)));
    }

    /**
     * @see RelfService#length(String)
     */
    @Implementation
    public static long length(String path) {
        return Shadow.directlyOn(RelfService.class, "length", from(String.class, Constants.fspath(path)));
    }

    /**
     * @see RelfService#listdir(String)
     */
    @Implementation
    public static String[] listdir(String path) {
        return Shadow.directlyOn(RelfService.class, "listdir", from(String.class, Constants.fspath(path)));
    }

    /**
     * @see RelfService#readlink(String)
     */
    @Implementation
    public static String readlink(String path) throws IOException {
        RandomAccessFile file = new RandomAccessFile(Constants.fspath(path), "r");
        return file.readLine();
    }

    /**
     * @see RelfService#realpath(String)
     */
    @Implementation
    public static String realpath(String path) throws IOException {
        return Shadow.directlyOn(RelfService.class, "realpath", from(String.class, Constants.fspath(path)));
    }

    /**
     * @see RelfService#stat(String)
     */
    @Implementation
    public static StructStat stat(String path) {
        return Shadow.directlyOn(RelfService.class, "stat", from(String.class, Constants.fspath(path)));
    }

    /**
     * @see RelfService#lstat(String)
     */
    @Implementation
    public static StructStat lstat(String path) throws IOException {
        return Shadow.directlyOn(RelfService.class, "stat", from(String.class, readlink(path)));
    }

    /**
     * @see RelfService#statvfs(String)
     */
    @Implementation
    public static StructStatVfs statvfs(String path) throws IOException {
        return Shadow.directlyOn(RelfService.class, "statvfs", from(String.class, Constants.fspath(path)));
    }

    /**
     * @see RelfService#open(String)
     */
    @Implementation
    public static FileInputStream open(String path) throws IOException {
        return Shadow.directlyOn(RelfService.class, "open", from(String.class, Constants.fspath(path)));
    }
}
