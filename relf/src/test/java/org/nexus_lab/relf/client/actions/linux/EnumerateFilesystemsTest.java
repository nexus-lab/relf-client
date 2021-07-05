package org.nexus_lab.relf.client.actions.linux;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import org.nexus_lab.relf.client.actions.ActionCallback;
import org.nexus_lab.relf.client.exceptions.TestSuccessException;
import org.nexus_lab.relf.lib.rdfvalues.client.RDFFilesystem;
import org.nexus_lab.relf.robolectric.ShadowRelfService;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/**
 * @author Ruipeng Zhang
 */
@RunWith(RobolectricTestRunner.class)
@Config(shadows = ShadowRelfService.class)
public class EnumerateFilesystemsTest {
    private final ActionCallback<RDFFilesystem> callback = new ActionCallback<RDFFilesystem>() {
        private int hit = 0;
        private String[] targetDev = new String[]{"rootfs", "tmpfs", "proc", "sysfs", "tmpfs", "none"};
        private String[] targetMp = new String[]{"/", "/dev", "/proc", "/sys", "/mnt", "/dev/cpuctl"};
        private String[] targetFs = new String[]{"rootfs", "tmpfs", "proc", "sysfs", "tmpfs", "cgroup"};

        @Override
        public void onResponse(RDFFilesystem filesystem) {
            for (int j = 0; j < targetMp.length; j++) {
                if (targetMp[j].equals(filesystem.getMountPoint())) {
                    assertEquals(targetFs[j], filesystem.getType());
                    assertEquals(targetDev[j], filesystem.getDevice());
                    assertTrue(filesystem.getOptions().size() > 0);
                    hit++;
                }
            }
        }

        @Override
        public void onComplete() {
            assertEquals(targetMp.length, hit);
            throw new TestSuccessException();
        }
    };

    @Test(expected = TestSuccessException.class)
    public void execute() {
        Context context = ApplicationProvider.getApplicationContext();
        new EnumerateFilesystems().execute(context, null, callback);
    }

}