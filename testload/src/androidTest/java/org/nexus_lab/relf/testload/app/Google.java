package org.nexus_lab.relf.testload.app;

import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiObject2;
import androidx.test.uiautomator.Until;

import org.nexus_lab.relf.testload.core.AppMeta;
import org.nexus_lab.relf.testload.core.RunnableApp;
import org.nexus_lab.relf.testload.util.DeviceUtil;

@AppMeta(name = "Google", packageName = "com.google.android.googlequicksearchbox")
public class Google extends RunnableApp {
    private void search(String keywords) {
        UiObject2 tab = getDevice().wait(Until.findObject(
                By.res(getResourceId("bottom_navigation_destination")).desc("Search")), 2000);
        tab.click();
        UiObject2 searchPlate = DeviceUtil.ensureFindObjectById(getResourceId("search_plate"));
        searchPlate.click();
        UiObject2 searchBox = DeviceUtil.ensureFindObjectById(getResourceId("search_box"));
        searchBox.setText(keywords);
        getDevice().pressEnter();
        getDevice().waitForIdle();
        DeviceUtil.waitUntilFindObjectById(getResourceId("corpus_bar_selector"), 5000);
    }

    private void checkUpdates(String keywords) {
        UiObject2 tab = getDevice().wait(Until.findObject(
                By.res(getResourceId("bottom_navigation_destination")).desc("Updates")), 2000);
        tab.click();
        DeviceUtil.waitUntilFindObjectById(getResourceId("contextual_greeting_message"), 10 * 1000);
        DeviceUtil.swipeUp();
        UiObject2 keyboardIndicator = DeviceUtil.ensureFindObjectById(getResourceId("keyboard_indicator"));
        keyboardIndicator.click();
        UiObject2 input = DeviceUtil.ensureFindObjectById(getResourceId("input_text"));
        input.setText(keywords);
        getDevice().pressEnter();
        DeviceUtil.waitUntilFindObjectById(getResourceId("chatui_google_bubble_logo"), 5000);
    }

    @Override
    public void run() {
        search("Hello world");
        getDevice().pressBack();
        checkUpdates("who am i");
        getDevice().pressBack();
    }

    @Override
    public boolean waitUntilReady() {
        return DeviceUtil.ensureFindObjectById(getResourceId("bottom_navigation_destinations")) != null;
    }
}
