package org.nexus_lab.relf.client.actions.admin;

import static org.nexus_lab.relf.utils.ReflectionUtils.callStaticMethod;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.jaredrummler.android.device.DeviceName;
import org.nexus_lab.relf.client.actions.Action;
import org.nexus_lab.relf.client.actions.ActionCallback;
import org.nexus_lab.relf.lib.rdfvalues.RDFNull;
import org.nexus_lab.relf.lib.rdfvalues.client.RDFUname;
import org.nexus_lab.relf.utils.ReflectionUtils;
import org.nexus_lab.relf.utils.os.SystemProperties;

import org.apache.commons.lang3.exception.ExceptionUtils;

/**
 * @author Ruipeng Zhang
 */
public class GetPlatformInfo implements Action<RDFNull, RDFUname> {
    private static final String TAG = GetPlatformInfo.class.getSimpleName();

    /**
     * TODO: need to adapt to the Android O (DHCP client no longer sends domain name)
     *
     * @return Get device hostname
     */
    private String hostname() {
        try {
            return callStaticMethod(Build.class, "getString",
                    ReflectionUtils.Parameter.from(String.class, "net.hostname"));
        } catch (ReflectiveOperationException e) {
            Log.w(TAG, String.format("Cannot get localhost hostname.\n%s",
                    ExceptionUtils.getStackTrace(e)));
        }
        return null;
    }

    /**
     * @return {@link RDFUname} created from the current system information
     */
    private RDFUname getUnameFromCurrentSystem() {
        String hostname = hostname();
        String deviceName = DeviceName.getDeviceName();
        RDFUname uname = new RDFUname();
        uname.setSystem(SystemProperties.OS_NAME);
        uname.setVersion(SystemProperties.OS_VERSION);
        uname.setRelease(SystemProperties.OS_RELEASE);
        uname.setPep425Tag("");
        uname.setMachine(SystemProperties.OS_ARCH);
        uname.setKernel(SystemProperties.OS_KERNEL);
        uname.setArchitecture(SystemProperties.OS_ARCH);
        uname.setNode(deviceName);
        uname.setFqdn(hostname == null ? deviceName : hostname);
        return uname;
    }

    @Override
    public void execute(Context context, RDFNull request, ActionCallback<RDFUname> callback) {
        callback.onResponse(getUnameFromCurrentSystem());
        callback.onComplete();
    }
}
