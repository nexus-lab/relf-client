package org.nexus_lab.relf.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import android.system.OsConstants;
import android.system.StructStat;
import android.system.StructStatVfs;

import org.nexus_lab.relf.client.exceptions.TestSuccessException;
import org.nexus_lab.relf.service.struct.StructPasswd;
import org.nexus_lab.relf.utils.os.Process;

import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

/**
 * @author Ruipeng Zhang
 */
public class RelfServiceTest {
    private static final int UID_ROOT = 0;
    private static final int UID_SYSTEM = 1000;

    @BeforeClass
    public static void setup() {
        assumeTrue(RelfService.isNativeServiceActive());
    }

    @Test
    public void stat() throws IOException {
        StructStat stat = RelfService.stat("/init");
        assertNotNull(stat);
        assertEquals(0, stat.st_uid);
        assertTrue(stat.st_gid == 0 || stat.st_gid == 2000);
        assertFalse(OsConstants.S_ISLNK(stat.st_mode));
        assertTrue(stat.st_size > 0);
    }

    @Test
    public void lstat() throws IOException {
        StructStat stat = RelfService.lstat("/proc/1/exe");
        assertNotNull(stat);
        assertEquals(0, stat.st_uid);
        assertEquals(0, stat.st_gid);
        assertTrue(OsConstants.S_ISLNK(stat.st_mode));
        assertEquals(0, stat.st_size);
    }

    @Test
    public void statvfs() throws IOException {
        StructStatVfs stat = RelfService.statvfs("/system");
        assertNotNull(stat);
        assertTrue(stat.f_bsize > 0);
        assertTrue(stat.f_bfree > 0);
        assertTrue(stat.f_bavail > 0);
    }

    @Test
    public void exists() {
        assertFalse(RelfService.exists("/proc/0"));
        assertTrue(RelfService.exists("/proc/1"));
    }

    @Test
    public void isdir() {
        assertTrue(RelfService.isdir("/proc/1"));
        assertFalse(RelfService.isdir("/proc/1/exe"));
    }

    @Test
    public void listdir() {
        String[] dirs = RelfService.listdir("/proc");
        assertNotNull(dirs);
        List<String> dirList = Arrays.asList(dirs);
        assertTrue(dirList.contains("net"));
        assertTrue(dirList.contains("tty"));
        assertTrue(dirList.contains("1"));
        assertTrue(dirList.contains(String.valueOf(Process.currentProcess().pid())));
    }

    @Test
    public void length() {
        assertTrue(RelfService.length("/init") > 0);
        assertEquals(0, RelfService.length("/proc/1/stat"));
    }

    @Test(expected = TestSuccessException.class)
    public void readlink() throws IOException {
        assertEquals("/init", RelfService.readlink("/proc/1/exe"));
        try {
            assertEquals("/init", RelfService.readlink("/init.rc"));
        } catch (IOException e) {
            throw new TestSuccessException();
        }
    }

    @Test
    public void realpath() throws IOException {
        assertEquals("/init", RelfService.realpath("/proc/1/exe"));
        assertEquals("/init.rc", RelfService.realpath("/init.rc"));
    }

    @Test
    public void open() throws IOException {
        FileInputStream fd = RelfService.open("/proc/1/exe");
        assertTrue(fd.read() > -1);
        assertTrue(fd.getFD().valid());
    }

    @Test
    public void getpwent() throws IOException {
        StructPasswd[] passwds = RelfService.getpwent();
        assertNotNull(passwds);
        assertTrue(passwds.length > 0);
        int hit = 0;
        for (StructPasswd passwd : passwds) {
            if (Arrays.asList(UID_ROOT, UID_SYSTEM).contains(passwd.pw_uid)) {
                hit++;
            }
            if (Arrays.asList("root", "system", "adb").contains(passwd.pw_name)) {
                hit++;
            }
        }
        assertEquals(5, hit);
    }

    @Test
    public void getpwuid() throws IOException {
        StructPasswd root = RelfService.getpwuid(UID_ROOT);
        assertNotNull(root);
        assertEquals("root", root.pw_name);
        assertEquals(UID_ROOT, root.pw_uid);
        assertEquals(UID_ROOT, root.pw_gid);
        StructPasswd system = RelfService.getpwuid(UID_SYSTEM);
        assertNotNull(system);
        assertEquals("system", system.pw_name);
        assertEquals(UID_SYSTEM, system.pw_uid);
        assertEquals(UID_SYSTEM, system.pw_gid);
    }

    @Test
    public void LineReader_readLine() throws IOException {
        String[] paths = new String[]{
                String.format("/proc/%d/cmdline", Process.currentProcess().pid()),
                "/proc/net/tcp6",
                "/proc/net/unix"
        };
        for (String path : paths) {
            File tmp = File.createTempFile("relf-androidTest-", "");
            tmp.delete();
            Files.copy(new File(path).toPath(), tmp.toPath());
            RelfService.LineReader relfReader = new RelfService.LineReader(tmp.getAbsolutePath());
            RandomAccessFile jdkReader = new RandomAccessFile(tmp.getAbsolutePath(), "r");
            while (true) {
                String expected = jdkReader.readLine();
                String actual = relfReader.readLine();
                assertEquals(expected, actual);
                if (expected == null) {
                    break;
                }
            }
            jdkReader.close();
            relfReader.close();
        }
    }
}