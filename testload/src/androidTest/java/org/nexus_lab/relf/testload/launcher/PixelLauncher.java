package org.nexus_lab.relf.testload.launcher;

import android.os.RemoteException;
import android.util.Log;

import androidx.test.uiautomator.By;
import androidx.test.uiautomator.BySelector;
import androidx.test.uiautomator.Direction;
import androidx.test.uiautomator.UiObject2;

import org.nexus_lab.relf.testload.core.AppMeta;
import org.nexus_lab.relf.testload.core.Launcher;

import static org.nexus_lab.relf.testload.util.DeviceUtil.findObject;
import static org.nexus_lab.relf.testload.util.DeviceUtil.findObjectById;
import static org.nexus_lab.relf.testload.util.DeviceUtil.findObjectByText;
import static org.nexus_lab.relf.testload.util.DeviceUtil.waitUntilFindObjectById;

@AppMeta(name = "Pixel Launcher", packageName = "com.google.android.apps.nexuslauncher")
public final class PixelLauncher extends Launcher {
    private final static String TAG = PixelLauncher.class.getSimpleName();
    private final static int TIMEOUT = 5000;

    @Override
    public boolean openAppDrawer() {
        int retry = 0;
        boolean result = getDevice().pressHome();
        if (!result) {
            return false;
        }
        UiObject2 found = waitUntilFindObjectById(getResourceId("layout"), TIMEOUT);
        if (found == null) {
            return false;
        }
        found = null;
        while (found == null && retry < 3) {
            getDevice().swipe(getDevice().getDisplayWidth() / 2,
                    getDevice().getDisplayHeight(),
                    getDevice().getDisplayWidth() / 2, 0, 20);
            found = waitUntilFindObjectById(getResourceId("apps_list_view"), TIMEOUT);
            retry++;
        }
        return found != null;
    }

    @Override
    public UiObject2 findAppInDrawer(String name) {
        if (!openAppDrawer()) {
            Log.w(TAG, "Cannot open drawer.");
            return null;
        }
        boolean canScroll = true;
        BySelector selector = By.res(getResourceId("icon")).desc(name);
        UiObject2 appList = findObjectById(getResourceId("apps_list_view"));
        UiObject2 found = findObject(selector);
        while (appList != null && found == null && canScroll) {
            canScroll = appList.scroll(Direction.DOWN, 1);
            getDevice().waitForIdle();
            found = findObject(selector);
        }
        return found;
    }

    @Override
    public boolean clearRecentTasks() {
        UiObject2 overviewPanel = findObjectById(getResourceId("overview_panel"));
        if (overviewPanel == null) {
            try {
                if (!getDevice().pressRecentApps()) {
                    return false;
                }
            } catch (RemoteException ignored) {
                return false;
            }
        }
        overviewPanel = waitUntilFindObjectById(getResourceId("overview_panel"), TIMEOUT);
        if (overviewPanel != null && overviewPanel.getChildCount() > 0) {
            UiObject2 found = getDevice().findObject(By.text("Clear all"));
            while (found == null) {
                getDevice().swipe(0,
                        getDevice().getDisplayHeight() / 2,
                        getDevice().getDisplayWidth(),
                        getDevice().getDisplayHeight() / 2, 20);
                getDevice().waitForIdle();
                found = findObjectByText("Clear all");
            }
            found.click();
            getDevice().waitForIdle();
        }
        return true;
    }
}
