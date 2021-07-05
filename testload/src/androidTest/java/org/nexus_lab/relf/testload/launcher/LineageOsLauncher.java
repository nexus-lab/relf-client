package org.nexus_lab.relf.testload.launcher;

import android.graphics.Rect;
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
import static org.nexus_lab.relf.testload.util.DeviceUtil.sleep;
import static org.nexus_lab.relf.testload.util.DeviceUtil.swipeDown;
import static org.nexus_lab.relf.testload.util.DeviceUtil.waitUntilFindObject;
import static org.nexus_lab.relf.testload.util.DeviceUtil.waitUntilFindObjectById;

@AppMeta(name = "LineageOS Launcher", packageName = "org.lineageos.trebuchet")
public class LineageOsLauncher extends Launcher {
    private final static String TAG = LineageOsLauncher.class.getSimpleName();
    private final static int TIMEOUT = 5000;

    @Override
    public boolean openAppDrawer() {
        int retry = 0;
        getDevice().pressHome();
        UiObject2 appView = findObjectById("com.android.launcher3:id/apps_view");
        if (appView != null) {
            return true;
        }
        do {
            retry++;
            getDevice().swipe(getDevice().getDisplayWidth() / 2,
                    getDevice().getDisplayHeight(),
                    getDevice().getDisplayWidth() / 2, 0, 20);
            appView = waitUntilFindObjectById("com.android.launcher3:id/apps_view", TIMEOUT);
        } while (appView == null && retry < 3);
        return appView != null;
    }

    @Override
    public UiObject2 findAppInDrawer(String name) {
        if (!openAppDrawer()) {
            Log.w(TAG, "Cannot open drawer.");
            return null;
        }
        boolean canScroll = true;
        BySelector selector = By.res("com.android.launcher3:id/icon").desc(name);
        UiObject2 appList = findObjectById("com.android.launcher3:id/apps_list_view");
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
        UiObject2 recentView = findObjectById("com.android.systemui:id/recents_view");
        if (recentView == null) {
            try {
                if (!getDevice().pressRecentApps()) {
                    return false;
                }
            } catch (RemoteException ignored) {
                return false;
            }
        }
        UiObject2 list = waitUntilFindObject(By.clazz("android.widget.ScrollView"), TIMEOUT);
        if (list == null || list.getChildCount() == 0) {
            getDevice().pressBack();
            return true;
        }
        UiObject2 clearAll;
        while ((clearAll = findObject(By.res("com.android.systemui:id/button").text("CLEAR ALL"))) == null) {
            swipeDown();
        }
        Rect bounds = clearAll.getVisibleBounds();
        getDevice().click(bounds.left + bounds.width() / 2, bounds.top + bounds.height() / 2);
        sleep(1000);
        return true;
    }
}
