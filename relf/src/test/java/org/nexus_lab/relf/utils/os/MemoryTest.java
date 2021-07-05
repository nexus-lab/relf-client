package org.nexus_lab.relf.utils.os;

import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeTrue;

import org.nexus_lab.relf.Constants;
import org.nexus_lab.relf.robolectric.ShadowRelfService;
import org.nexus_lab.relf.robolectric.ShadowLinux;
import org.nexus_lab.relf.robolectric.ShadowPosix;

import org.apache.commons.lang3.SystemUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.RandomAccessFile;

/**
 * @author Ruipeng Zhang
 */
@RunWith(RobolectricTestRunner.class)
@Config(shadows = {ShadowLinux.class, ShadowPosix.class, ShadowRelfService.class}, sdk = 28)
public class MemoryTest {
    private static final String PATH = Constants.fspath("/proc/meminfo");

    @BeforeClass
    public static void setup() {
        assumeTrue(SystemUtils.IS_OS_LINUX || SystemUtils.IS_OS_MAC_OSX);
    }

    @Test
    public void memTotal() throws Exception {
        RandomAccessFile file = new RandomAccessFile(PATH, "r");
        String line;
        while ((line = file.readLine()) != null) {
            if (line.contains("MemTotal")) {
                String[] parts = line.split("\\s+");
                assertEquals(Long.parseLong(parts[1]) * 1024, new Memory().memTotal());
                return;
            }
        }
        throw new Exception("No memTotal found in /proc/meminfo");
    }
}