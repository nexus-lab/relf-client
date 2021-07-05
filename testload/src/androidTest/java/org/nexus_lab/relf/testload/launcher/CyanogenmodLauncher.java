package org.nexus_lab.relf.testload.launcher;

import android.graphics.Rect;
import android.util.Log;

import androidx.test.uiautomator.By;
import androidx.test.uiautomator.BySelector;
import androidx.test.uiautomator.UiObject2;

import org.nexus_lab.relf.testload.core.AppMeta;

import static org.nexus_lab.relf.testload.util.DeviceUtil.ensureFindObjectById;
import static org.nexus_lab.relf.testload.util.DeviceUtil.findObject;
import static org.nexus_lab.relf.testload.util.DeviceUtil.swipeUp;
import static org.nexus_lab.relf.testload.util.DeviceUtil.waitForIdle;
import static org.nexus_lab.relf.testload.util.DeviceUtil.waitUntilFindObjectByDesc;

@AppMeta(name = "Cyanogenmod Launcher", packageName = "com.cyanogenmod.trebuchet")
public class CyanogenmodLauncher extends LineageOsLauncher {
    private final static String TAG = LineageOsLauncher.class.getSimpleName();
    private final static int TIMEOUT = 5000;

    @Override
    public boolean unlock() {
        if (super.unlock()) {
            swipeUp();
            return true;
        }
        return false;
    }

    private void goToInitial(String initial) {
        UiObject2 dial = ensureFindObjectById("com.android.launcher3:id/scrubberText");
        String text = dial.getText();
        int i = Math.max(text.indexOf(initial), 0);
        Rect bounds = dial.getVisibleBounds();
        double unit = bounds.width() / (double) text.length();
        int x = (int) ((i + 0.5) * unit + bounds.left);
        int y = (int) (bounds.top + bounds.height() / 2.0);
        getDevice().click(x, y);
        waitForIdle();
    }

    @Override
    public boolean openAppDrawer() {
        getDevice().pressHome();
        UiObject2 apps = waitUntilFindObjectByDesc("Apps", TIMEOUT);
        if (apps == null) {
            return false;
        }
        apps.click();
        return true;
    }

    @Override
    public UiObject2 findAppInDrawer(String name) {
        if (!openAppDrawer()) {
            Log.w(TAG, "Cannot open drawer.");
            return null;
        }
        goToInitial("?");
        goToInitial(name.substring(0, 1));
        BySelector selector = By.res("com.android.launcher3:id/icon").desc(name);
        return findObject(selector);
    }
}
