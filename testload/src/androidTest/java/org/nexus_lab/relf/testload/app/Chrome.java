package org.nexus_lab.relf.testload.app;

import android.graphics.Rect;
import android.os.Build;
import android.util.Log;

import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiObject2;
import androidx.test.uiautomator.Until;

import org.nexus_lab.relf.testload.core.AppMeta;
import org.nexus_lab.relf.testload.core.RunnableApp;
import org.nexus_lab.relf.testload.util.DeviceUtil;

import static org.nexus_lab.relf.testload.util.DeviceUtil.findObject;
import static org.nexus_lab.relf.testload.util.DeviceUtil.findObjectById;
import static org.nexus_lab.relf.testload.util.DeviceUtil.findObjectByText;
import static org.nexus_lab.relf.testload.util.DeviceUtil.sleep;
import static org.nexus_lab.relf.testload.util.DeviceUtil.swipeDown;

@AppMeta(name = "Chrome", packageName = "com.android.chrome")
public class Chrome extends RunnableApp {
    private void openNewTab() {
        UiObject2 emptyNewTab = findObjectById(getResourceId("empty_new_tab_button"));
        if (emptyNewTab != null) {
            emptyNewTab.click();
            sleep(2000);
            return;
        }
        UiObject2 menu = findObject(By.res(getResourceId("menu_button")).desc("More options"));
        if (menu != null) {
            menu.click();
            UiObject2 addNewTab = findObject(By.res(getResourceId("menu_item_text")).text("New tab"));
            if (addNewTab != null) {
                addNewTab.click();
                sleep(2000);
            }
            return;
        }
        UiObject2 tabSwitcher = DeviceUtil.ensureFindObjectById(getResourceId("tab_switcher_button"));
        tabSwitcher.click();
        UiObject2 addNewTab = DeviceUtil.ensureFindObjectById(getResourceId("new_tab_button"));
        addNewTab.click();
        getDevice().waitForIdle();
    }

    private void searchOrGoTo(String urlOrKeywords) {
        UiObject2 searchBox = DeviceUtil.ensureFindObjectById(getResourceId("search_box_text"));
        searchBox.setText(urlOrKeywords);
        getDevice().pressEnter();
        // wait for 3 secs
        DeviceUtil.sleep(3000);
    }

    private void closeAllTabs() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            swipeDown();
            // for nexus 9 tablet only
            UiObject2 container = findObjectById(getResourceId("toolbar_container"));
            UiObject2 toolbar = findObjectById(getResourceId("toolbar"));
            if (container == null || toolbar == null) {
                return;
            }
            Rect containerBounds = container.getVisibleBounds();
            Rect toolbarBounds = toolbar.getVisibleBounds();
            int y = containerBounds.top / 2 + toolbarBounds.top / 2;
            // switch to first tab
            getDevice().click(100, y);
            for (int x = 300; x < toolbarBounds.width() - 200; x += 3) {
                // close all tabs
                Log.d(Chrome.class.getSimpleName(), "clicking (" + x + ", " + y + ")");
                getDevice().swipe(x, y, x, y, 30);
                if (findObjectByText("Close all tabs") != null) {
                    findObjectByText("Close all tabs").click();
                    return;
                }
            }
            return;
        }
        UiObject2 tabSwitcher = DeviceUtil.ensureFindObjectById(getResourceId("tab_switcher_button"));
        tabSwitcher.click();
        UiObject2 tabCounter = DeviceUtil.ensureFindObjectById(getResourceId("tab_switcher_mode_tab_switcher_button"));
        while (tabCounter.isEnabled()) {
            getDevice().swipe(
                    0,
                    getDevice().getDisplayHeight() / 2,
                    getDevice().getDisplayWidth(),
                    getDevice().getDisplayHeight() / 2,
                    10);
            tabCounter = DeviceUtil.ensureFindObjectById(getResourceId("tab_switcher_mode_tab_switcher_button"));
        }
    }

    private void scrollPage() {
        for (int i = 0; i < 10; i++) {
            getDevice().swipe(0, getDevice().getDisplayHeight(), 0, getDevice().getDisplayHeight() / 2, 100);
        }
        for (int i = 0; i < 5; i++) {
            getDevice().swipe(0, getDevice().getDisplayHeight() / 2, 0, getDevice().getDisplayHeight(), 100);
        }
        getDevice().waitForIdle();
    }

    @Override
    public void run() {
        UiObject2 searchBox = DeviceUtil.ensureFindObjectById(getResourceId("search_box_text"));
        if (searchBox == null) {
            openNewTab();
        }
        searchOrGoTo("www.google.com");
        scrollPage();
        openNewTab();
        searchOrGoTo("Hello world");
        scrollPage();
        openNewTab();
        searchOrGoTo("utc.edu");
        scrollPage();
        openNewTab();
        searchOrGoTo("twitter.com");
        scrollPage();
        closeAllTabs();
    }

    @Override
    public boolean waitUntilReady() {
        UiObject2 ui = getDevice().wait(Until.findObject(By.res(getResourceId("coordinator"))), 5000);
        return ui != null;
    }
}
