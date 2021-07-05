package org.nexus_lab.relf.client.actions.linux;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import org.nexus_lab.relf.client.exceptions.TestSuccessException;
import org.nexus_lab.relf.robolectric.ShadowRelfService;
import org.nexus_lab.relf.robolectric.ShadowLinux;
import org.nexus_lab.relf.robolectric.ShadowPosix;

import org.apache.commons.lang3.SystemUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assume.assumeTrue;

/**
 * @author Ruipeng Zhang
 */
@RunWith(RobolectricTestRunner.class)
@Config(shadows = {ShadowPosix.class, ShadowLinux.class, ShadowRelfService.class})
public class GetInstallDateTest {

    @BeforeClass
    public static void setup() {
        // Test will fail on Windows until VFS code is refactored
        assumeTrue(SystemUtils.IS_OS_LINUX || SystemUtils.IS_OS_MAC_OSX);
    }

    @Test(expected = TestSuccessException.class)
    public void execute() {
        Context context = ApplicationProvider.getApplicationContext();
        new GetInstallDate().execute(context, null, response -> {
            assertNotNull(response.getInteger());
            throw new TestSuccessException();
        });
    }
}