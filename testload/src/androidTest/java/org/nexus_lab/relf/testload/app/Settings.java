package org.nexus_lab.relf.testload.app;

import android.graphics.Rect;
import android.os.Build;

import androidx.test.uiautomator.Direction;
import androidx.test.uiautomator.UiObject2;

import org.nexus_lab.relf.testload.core.AppMeta;
import org.nexus_lab.relf.testload.core.RunnableApp;

import static org.nexus_lab.relf.testload.util.DeviceUtil.sleep;
import static org.nexus_lab.relf.testload.util.DeviceUtil.swipeUp;
import static org.nexus_lab.relf.testload.util.DeviceUtil.waitUntilFindObjectById;
import static org.nexus_lab.relf.testload.util.DeviceUtil.waitUntilFindObjectByText;

@AppMeta(name = "Settings", packageName = "com.android.settings")
public class Settings extends RunnableApp {
    private void goToSettings(String name) {
        boolean hitTop = false;
        boolean hitBottom = false;
        UiObject2 item = waitUntilFindObjectByText(name, 1000);
        UiObject2 container = waitUntilFindObjectById(getResourceId("dashboard_container"), 1000);
        if (container == null) {
            container = waitUntilFindObjectById(getResourceId("list"), 1000);
        }
        if (container == null) {
            return;
        }
        while (item == null && !hitTop) {
            if (!hitBottom) {
                if (!container.scroll(Direction.DOWN, 1f)) {
                    hitBottom = true;
                }
            } else if (!container.scroll(Direction.UP, 1f)) {
                hitTop = true;
            }
            item = waitUntilFindObjectByText(name, 1000);
        }
        if (item != null) {
            item.click();
            sleep(1000);
        }
    }

    private void adjustBrightness() {
        UiObject2 slider = waitUntilFindObjectById("com.android.systemui:id/slider", 1000);
        Rect bounds = slider.getVisibleBounds();
        getDevice().click((int) (bounds.left + bounds.width() * 0.1), (int) (bounds.top + bounds.height() * 0.5));
        sleep(3000);
        getDevice().click((int) (bounds.left + bounds.width() * 0.9), (int) (bounds.top + bounds.height() * 0.5));
        sleep(3000);
        getDevice().click((int) (bounds.left + bounds.width() * 0.7), (int) (bounds.top + bounds.height() * 0.5));
        getDevice().pressBack();
    }

    @Override
    public void run() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            goToSettings("Network & internet");
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            goToSettings("Network & Internet");
        }
        goToSettings("Wi‑Fi");
        getDevice().pressBack();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getDevice().pressBack();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            goToSettings("Connected devices");
        } else {
            goToSettings("Bluetooth");
        }
        getDevice().pressBack();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            goToSettings("Apps & notifications");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                goToSettings("Screen time");
                getDevice().pressBack();
            }
            goToSettings("Advanced");
            goToSettings("App permissions");
            getDevice().pressBack();
            getDevice().pressBack();
        } else {
            goToSettings("Apps");
            swipeUp();
            getDevice().pressBack();
            goToSettings("Notifications");
            swipeUp();
            getDevice().pressBack();
        }
        goToSettings("Battery");
        getDevice().pressBack();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            goToSettings("System");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                getDevice().pressBack();
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            goToSettings("About phone");
        } else {
            goToSettings("About tablet");
        }
        goToSettings("Build number");
        getDevice().pressBack();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getDevice().pressBack();
        }
        goToSettings("Display");
        goToSettings("Brightness level");
        adjustBrightness();
    }

    @Override
    public boolean waitUntilReady() {
        return waitUntilFindObjectById(getResourceId("content_frame"), 5000) != null;
    }
}
