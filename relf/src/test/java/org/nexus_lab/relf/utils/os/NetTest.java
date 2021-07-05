package org.nexus_lab.relf.utils.os;

import org.nexus_lab.relf.Constants;
import org.nexus_lab.relf.robolectric.ShadowRelfService;
import org.nexus_lab.relf.robolectric.ShadowLinux;
import org.nexus_lab.relf.robolectric.ShadowPosix;

import org.apache.commons.lang3.SystemUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetAddress;

import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeTrue;

/**
 * @author Ruipeng Zhang
 */
@RunWith(RobolectricTestRunner.class)
@Config(shadows = {ShadowLinux.class, ShadowPosix.class, ShadowRelfService.class}, sdk = 28)
public class NetTest {
    private static final String TCP4 = Constants.fspath("/proc/net/tcp");
    private static final String TCP6 = Constants.fspath("/proc/net/tcp6");
    private static final String UDP4 = Constants.fspath("/proc/net/udp");
    private static final String UDP6 = Constants.fspath("/proc/net/udp6");
    private static final String UNIX = Constants.fspath("/proc/net/unix");

    @Before
    public void setup() {
        assumeTrue(SystemUtils.IS_OS_LINUX || SystemUtils.IS_OS_MAC_OSX);
        Constants.overrideOsConstants();
    }

    private int getSocketNumber(String path) throws IOException {
        RandomAccessFile file = new RandomAccessFile(path, "r");
        int i = 0;
        String line;
        while ((line = file.readLine()) != null) {
            if (!line.trim().isEmpty()) {
                i++;
            }
        }
        return i - 1;
    }

    @Test
    public void tcp4() throws IOException {
        Net.InetSocket[] sockets = Net.tcp4();
        assertEquals(getSocketNumber(TCP4), sockets.length);
        assertEquals(InetAddress.getByAddress(new byte[]{127, 0, 0, 1}), sockets[0].getLocalAddress());
        assertEquals(431124, sockets[0].getInode());
        assertEquals(10, sockets[0].getState());
        assertEquals(5037, sockets[0].getLocalPort());
        assertEquals(InetAddress.getByAddress(new byte[]{0, 0, 0, 0}), sockets[0].getRemoteAddress());
        assertEquals(0, sockets[0].getRemotePort());
        assertEquals(InetAddress.getByAddress(new byte[]{-64, -88, 29, -52}), sockets[37].getLocalAddress());
        assertEquals(41856, sockets[37].getLocalPort());
        assertEquals(InetAddress.getByAddress(new byte[]{64, -23, -71, -68}), sockets[37].getRemoteAddress());
        assertEquals(5228, sockets[37].getRemotePort());
    }

    @Test
    public void tcp6() throws IOException {
        assertEquals(getSocketNumber(TCP6), Net.tcp6().length);
    }

    @Test
    public void udp4() throws IOException {
        assertEquals(getSocketNumber(UDP4), Net.udp4().length);
    }

    @Test
    public void udp6() throws IOException {
        assertEquals(getSocketNumber(UDP6), Net.udp6().length);
    }

    @Test
    public void unix() throws IOException {
        Net.UnixSocket[] sockets = Net.unix();
        assertEquals(getSocketNumber(UNIX), sockets.length);
        assertEquals(179781, sockets[sockets.length - 5].getInode());
        assertEquals("/run/systemd/journal/stdout", sockets[sockets.length - 5].getPath());
    }
}