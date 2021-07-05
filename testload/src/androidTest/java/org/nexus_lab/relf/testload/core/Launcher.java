package org.nexus_lab.relf.testload.core;

import android.app.KeyguardManager;
import android.content.Context;
import android.os.Build;
import android.os.PowerManager;
import android.os.RemoteException;

import androidx.test.uiautomator.UiObject2;

public abstract class Launcher extends App {
    public abstract boolean openAppDrawer();

    public abstract UiObject2 findAppInDrawer(String name);

    public boolean unlock() {
        if (isDeviceLocked()) {
            try {
                getDevice().wakeUp();
            } catch (RemoteException e) {
                return false;
            }
        }
        return true;
    }

    public abstract boolean clearRecentTasks();

    /**
     * https://gist.github.com/Jeevuz/4ec01688083670b1f3f92af64e44c112
     */
    public boolean isDeviceLocked() {
        // First we check the locked state
        KeyguardManager km = (KeyguardManager) getContext().getSystemService(Context.KEYGUARD_SERVICE);
        boolean inKeyguardRestrictedInputMode = km.inKeyguardRestrictedInputMode();

        if (inKeyguardRestrictedInputMode) {
            return true;
        } else {
            // If password is not set in the settings, the inKeyguardRestrictedInputMode() returns false,
            // so we need to check if screen on for this case
            PowerManager pm = (PowerManager) getContext().getSystemService(Context.POWER_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
                return !pm.isInteractive();
            } else {
                //noinspection deprecation
                return !pm.isScreenOn();
            }
        }
    }

    public boolean launchApp(String name) {
        UiObject2 app = findAppInDrawer(name);
        if (app == null) {
            return false;
        }
        app.click();
        getDevice().waitForIdle();
        return true;
    }

    @Override
    public boolean launch() {
        return true;
    }

    @Override
    public boolean waitUntilReady() {
        return true;
    }

    @Override
    public boolean exit() {
        return false;
    }
}
