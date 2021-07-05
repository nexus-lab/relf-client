package org.nexus_lab.relf.utils.os;

import static org.junit.Assert.assertTrue;

import org.nexus_lab.relf.robolectric.ShadowLinux;
import org.nexus_lab.relf.robolectric.ShadowPosix;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/**
 * @author Ruipeng Zhang
 */
@RunWith(RobolectricTestRunner.class)
@Config(shadows = {ShadowPosix.class, ShadowLinux.class}, sdk = 28)
public class SystemPropertiesTest {
    @Test
    public void os() {
        assertTrue(StringUtils.isNotEmpty(SystemProperties.OS_ARCH));
        assertTrue(StringUtils.isNotEmpty(SystemProperties.OS_KERNEL));
        assertTrue(StringUtils.isNotEmpty(SystemProperties.OS_NAME));
        assertTrue(StringUtils.isNotEmpty(SystemProperties.OS_RELEASE));
        assertTrue(StringUtils.isNotEmpty(SystemProperties.OS_VERSION));
    }
}
