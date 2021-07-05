package org.nexus_lab.relf.client.actions.android;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import android.content.Context;
import android.os.Build;
import android.system.Os;
import android.system.StructUtsname;

import androidx.test.core.app.ApplicationProvider;

import org.nexus_lab.relf.client.actions.ActionCallback;
import org.nexus_lab.relf.client.exceptions.TestSuccessException;
import org.nexus_lab.relf.lib.rdfvalues.android.RDFAndroidDeviceInfo;
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
@Config(shadows = {ShadowPosix.class, ShadowLinux.class})
public class GetAndroidDeviceInfoTest {
    private final ActionCallback<RDFAndroidDeviceInfo> callback = deviceInfo -> {
        assertEquals(Build.BRAND, deviceInfo.getHardware().getBrand());
        assertEquals(Build.DEVICE, deviceInfo.getHardware().getDevice());
        assertEquals(Build.MODEL, deviceInfo.getHardware().getModel());
        assertEquals(Build.HARDWARE, deviceInfo.getHardware().getHardware());
        assertEquals(Build.VERSION.SDK_INT, deviceInfo.getSystem().getVersion().intValue());
        assertEquals(Build.VERSION.CODENAME, deviceInfo.getSystem().getVersionCodename());
        StructUtsname uname = Os.uname();
        String kernelVersion = StringUtils.join(
                new String[]{
                        uname.sysname,
                        uname.nodename,
                        uname.release,
                        uname.version,
                        uname.machine
                }, " ");
        assertNotNull(uname);
        assertEquals(kernelVersion, deviceInfo.getSystem().getKernelVersion());
        throw new TestSuccessException();
    };

    @Test(expected = TestSuccessException.class)
    public void execute() {
        Context context = ApplicationProvider.getApplicationContext();
        new GetAndroidDeviceInfo().execute(context, null, callback);
    }
}