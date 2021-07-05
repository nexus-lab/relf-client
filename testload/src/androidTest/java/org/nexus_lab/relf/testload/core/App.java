package org.nexus_lab.relf.testload.core;

import android.app.Instrumentation;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.UiDevice;

import org.atteo.classindex.ClassIndex;
import org.atteo.classindex.IndexSubclasses;

@IndexSubclasses
public abstract class App {
    private final static UiDevice device;
    private final static Context context;
    private static App launcher;

    static {
        Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
        device = UiDevice.getInstance(instrumentation);
        context = instrumentation.getContext();
        for (Class<? extends App> subclass : ClassIndex.getSubclasses(App.class)) {
            AppMeta meta = subclass.getAnnotation(AppMeta.class);
            if (meta != null && meta.packageName().equals(device.getLauncherPackageName())) {
                try {
                    launcher = subclass.newInstance();
                } catch (IllegalAccessException | InstantiationException e) {
                    Log.e(App.class.getSimpleName(), "Cannot instantiate launcher "
                            + device.getLauncherPackageName());
                }
            }
        }
        if (launcher == null) {
            throw new RuntimeException("Cannot find app class for launcher "
                    + device.getLauncherPackageName());
        }
    }

    public static Context getContext() {
        return context;
    }

    public static UiDevice getDevice() {
        return device;
    }

    public static Launcher getLauncher() {
        return (Launcher) launcher;
    }

    public AppMeta getAppMeta() {
        AppMeta meta = getClass().getAnnotation(AppMeta.class);
        if (meta == null) {
            throw new RuntimeException("App " + getClass().getName() + " does not have meta information");
        }
        return meta;
    }

    public String getResourceId(String id) {
        return getAppMeta().packageName() + ":id/" + id;
    }

    public boolean launch() {
        if (!isPackageInstalled(getAppMeta().packageName())) {
            return false;
        }
        return getLauncher().launchApp(getAppMeta().name());
    }

    public boolean exit() {
        while (getAppMeta().packageName().equals(getDevice().getCurrentPackageName())) {
            getDevice().pressBack();
            getDevice().waitForIdle();
        }
        return true;
    }

    public abstract boolean waitUntilReady();

    private boolean isPackageInstalled(String packageName) {
        PackageManager pm = context.getPackageManager();
        try {
            pm.getPackageInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
}
