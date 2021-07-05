package org.nexus_lab.relf.client.actions.android;

import static android.Manifest.permission.READ_PHONE_STATE;

import static org.nexus_lab.relf.utils.ReflectionUtils.getStaticFieldValue;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.system.Os;
import android.system.StructUtsname;

import org.nexus_lab.relf.client.actions.Action;
import org.nexus_lab.relf.client.actions.ActionCallback;
import org.nexus_lab.relf.client.actions.UsesPermission;
import org.nexus_lab.relf.lib.rdfvalues.RDFNull;
import org.nexus_lab.relf.lib.rdfvalues.android.RDFAndroidDeviceInfo;
import org.nexus_lab.relf.lib.rdfvalues.android.RDFAndroidHardwareInfo;
import org.nexus_lab.relf.lib.rdfvalues.android.RDFAndroidOsBuildInfo;
import org.nexus_lab.relf.lib.rdfvalues.android.RDFAndroidOsInfo;
import org.nexus_lab.relf.proto.AndroidOsBuildInfo.BuildType;
import org.nexus_lab.relf.utils.os.SystemProperties;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

/**
 * @author Ruipeng Zhang
 */
@UsesPermission(READ_PHONE_STATE)
public class GetAndroidDeviceInfo implements Action<RDFNull, RDFAndroidDeviceInfo> {
    @SuppressLint("MissingPermission")
    private RDFAndroidHardwareInfo getHardwareInfo(Context context) {
        boolean isContainer = false;
        boolean isDebuggable = false;
        boolean isEmulator = false;
        try {
            isContainer = getStaticFieldValue(Build.class, "IS_CONTAINER");
        } catch (ReflectiveOperationException ignored) {
        }
        try {
            isDebuggable = getStaticFieldValue(Build.class, "IS_DEBUGGABLE");
        } catch (ReflectiveOperationException ignored) {
        }
        try {
            isEmulator = getStaticFieldValue(Build.class, "IS_EMULATOR");
        } catch (ReflectiveOperationException ignored) {
        }
        RDFAndroidHardwareInfo info = new RDFAndroidHardwareInfo();
        info.setBrand(Build.BRAND);
        info.setBaseband(SystemProperties.get(context, "gsm.version.baseband", ""));
        info.setBoard(Build.BOARD);
        info.setBootloader(Build.BOOTLOADER);
        info.setDevice(Build.DEVICE);
        info.setHardware(Build.HARDWARE);
        info.setIsContainer(isContainer);
        info.setIsDebuggable(isDebuggable);
        info.setIsEmulator(isEmulator);
        info.setManufacturer(Build.MANUFACTURER);
        info.setModel(Build.MODEL);
        info.setProduct(Build.PRODUCT);
        info.setRadio(Build.getRadioVersion());
        info.setSerial(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? Build.getSerial() : Build.SERIAL);
        info.setSupportedAbis(Arrays.asList(Build.SUPPORTED_ABIS));
        return info;
    }

    private RDFAndroidOsInfo getOsInfo() {
        RDFAndroidOsBuildInfo buildInfo = new RDFAndroidOsBuildInfo();
        buildInfo.setId(Build.ID);
        buildInfo.setHost(Build.HOST);
        buildInfo.setUser(Build.USER);
        buildInfo.setNumber(Build.DISPLAY);
        buildInfo.setTime(Build.TIME);
        buildInfo.setFingerprint(Build.FINGERPRINT);
        buildInfo.setTags(Arrays.asList(Build.TAGS.split(",")));
        buildInfo.setType("eng".equals(Build.TYPE) ? BuildType.ENG
                : ("userdebug".equals(Build.TYPE) ? BuildType.USERDEBUG : BuildType.USER));
        StructUtsname uname = Os.uname();
        String kernelVersion = StringUtils.join(
                new String[]{uname.sysname, uname.nodename, uname.release,
                        uname.version, uname.machine}, " ");
        RDFAndroidOsInfo info = new RDFAndroidOsInfo();
        info.setVersion(Build.VERSION.SDK_INT);
        info.setVersionName(Build.VERSION.RELEASE);
        info.setVersionCodename(Build.VERSION.CODENAME);
        info.setVersionIncremental(Build.VERSION.INCREMENTAL);
        info.setSecurityPatch(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? Build.VERSION.SECURITY_PATCH : "");
        info.setKernelVersion(kernelVersion);
        info.setBuildInfo(buildInfo);
        return info;
    }

    @Override
    public void execute(Context context, RDFNull request, ActionCallback<RDFAndroidDeviceInfo> callback) {
        RDFAndroidDeviceInfo deviceInfo = new RDFAndroidDeviceInfo();
        deviceInfo.setHardware(getHardwareInfo(context));
        deviceInfo.setSystem(getOsInfo());
        callback.onResponse(deviceInfo);
        callback.onComplete();
    }
}
