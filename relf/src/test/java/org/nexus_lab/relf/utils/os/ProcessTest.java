package org.nexus_lab.relf.utils.os;

import android.util.Pair;

import org.nexus_lab.relf.robolectric.ShadowRelfService;
import org.nexus_lab.relf.robolectric.ShadowLinux;
import org.nexus_lab.relf.robolectric.ShadowPosix;

import org.apache.commons.lang3.SystemUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

/**
 * @author Ruipeng Zhang
 */
@RunWith(RobolectricTestRunner.class)
@Config(shadows = {ShadowPosix.class, ShadowLinux.class, ShadowRelfService.class}, sdk = 28)
public class ProcessTest {
    private Process process;

    @Before
    public void setup() {
        assumeTrue(SystemUtils.IS_OS_LINUX || SystemUtils.IS_OS_MAC_OSX);
        process = new Process(1);
    }

    @Test
    public void currentProcess() throws IOException {
        int pid = Integer.parseInt(new File("/proc/self").getCanonicalFile().getName());
        assertEquals(pid, Process.currentProcess().pid());
    }

    @Test
    public void allProcesses() {
        Process[] processes = Process.allProcesses();
        assertEquals(2, processes.length);
        Set<Integer> pids = new HashSet<>();
        pids.add(processes[0].pid());
        pids.add(processes[1].pid());
        assertTrue(pids.contains(1));
        assertTrue(pids.contains(9999));
    }

    @Test
    public void name() {
        assertEquals("rogue process", process.name());
    }

    @Test
    public void cmdline() {
        assertTrue(process.cmdline().length > 0);
    }

    @Test
    public void username() {
        // skipped
    }

    @Test
    public void terminal() {
        // skipped, junit do not open terminal
    }

    @Test
    public void exe() {
        assertEquals("/bin/bash", process.exe());
    }

    @Test
    public void cwd() {
        assertEquals("/", process.cwd());
    }

    @Test
    public void fd() {
        Pair<Integer, String>[] descriptors = process.fd();
        assertEquals(3, descriptors.length);
        for (int i = 0; i < descriptors.length; i++) {
            switch (descriptors[i].first) {
                case 0:
                    assertEquals("/usr/bin/yes", descriptors[i].second);
                    break;
                case 1:
                    assertEquals("socket:[296537]", descriptors[i].second);
                    break;
                case 2:
                    assertEquals("socket:[27281]", descriptors[i].second);
                    break;
                default:
                    throw new RuntimeException("Tests out of range");
            }
        }
    }

    @Test
    public void stat() {
        Process.Stat stat = process.stat();
        assertEquals("rogue process", stat.name());
        assertEquals(0, stat.ppid());
        assertEquals(0, stat.session());
        assertEquals(0, stat.tty_nr());
        assertEquals(4, stat.utime());
        assertEquals(102, stat.stime());
        assertEquals(15, stat.cutime());
        assertEquals(16, stat.cstime());
        assertEquals(0, stat.nice());
        assertEquals(1, stat.num_threads());
        assertEquals(4.0, stat.starttime(), 0);
    }

    @Test
    public void socket() {
        Pair<Integer, Long>[] sockets = process.socket();
        assertEquals(2, sockets.length);
        for (Pair<Integer, Long> socket : sockets) {
            switch (socket.first) {
                case 1:
                    assertEquals(296537, socket.second.longValue());
                    break;
                case 2:
                    assertEquals(27281, socket.second.longValue());
                    break;
                default:
                    throw new RuntimeException("Socket fd out of range");
            }
        }
    }

    @Test
    public void statm() {
        Process.Statm stat = process.statm();
        assertEquals(4710, stat.size(), 10);
        assertEquals(891, stat.resident(), 10);
        assertEquals(615, stat.shared(), 10);
        assertEquals(561, stat.text(), 10);
        assertEquals(0, stat.lib(), 10);
        assertEquals(1084, stat.data(), 10);
        assertEquals(0, stat.dt(), 10);
    }

    @Test
    public void status() {
        Process.Status status = process.status();
        assertArrayEquals(new int[]{0, 1, 2}, status.uids());
        assertArrayEquals(new int[]{0, 2, 3}, status.gids());
    }
}