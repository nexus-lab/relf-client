package org.nexus_lab.relf.testload.app;

import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiObject2;

import java.util.Date;

import org.nexus_lab.relf.testload.core.AppMeta;
import org.nexus_lab.relf.testload.core.RunnableApp;

import static org.nexus_lab.relf.testload.util.DeviceUtil.ensureFindObject;
import static org.nexus_lab.relf.testload.util.DeviceUtil.ensureFindObjectById;
import static org.nexus_lab.relf.testload.util.DeviceUtil.sleep;
import static org.nexus_lab.relf.testload.util.DeviceUtil.swipeDown;
import static org.nexus_lab.relf.testload.util.DeviceUtil.swipeUp;
import static org.nexus_lab.relf.testload.util.DeviceUtil.waitForIdle;
import static org.nexus_lab.relf.testload.util.DeviceUtil.waitUntilFindObject;
import static org.nexus_lab.relf.testload.util.DeviceUtil.waitUntilFindObjectByDesc;
import static org.nexus_lab.relf.testload.util.DeviceUtil.waitUntilFindObjectById;

@AppMeta(name = "Twitter", packageName = "com.twitter.android")
public class Twitter extends RunnableApp {
    private void scrollPage() {
        for (int i = 0; i < 5; i++) {
            swipeUp();
        }
        waitForIdle();
    }

    private void switchTab(String name) {
        UiObject2 tab = null;
        switch (name) {
            case "Search":
                tab = waitUntilFindObjectByDesc("Search and Explore Tab", 1000);
                break;
            case "Notifications":
                tab = waitUntilFindObject(By.descContains("Notifications Tab"), 1000);
                break;
            case "Messages":
                tab = waitUntilFindObject(By.descContains("Messages Tab"), 1000);
                break;
            case "Home":
                tab = waitUntilFindObject(By.descContains("Home Tab"), 1000);
                break;
        }
        if (tab != null) {
            tab.click();
            waitForIdle();
        }
    }

    private void checkTweet() {
        UiObject2 row = waitUntilFindObjectById(getResourceId("tweet_content_text"), 1000);
        if (row == null) {
            return;
        }
        row.click();
        if (waitUntilFindObjectByDesc("Navigate up", 2000) != null ||
                waitUntilFindObjectByDesc("Close tab", 2000) != null) {
            scrollPage();
            getDevice().pressBack();
        }
    }

    private void searchTweet(String keywords) {
        UiObject2 queryView = waitUntilFindObjectById(getResourceId("query_view"), 1000);
        if (queryView == null) {
            swipeDown();
            queryView = waitUntilFindObjectById(getResourceId("query_view"), 1000);
            if (queryView == null) {
                return;
            }
        }
        queryView.click();
        UiObject2 search = ensureFindObject(By.clazz("android.widget.EditText").res(getResourceId("query")));
        search.setText(keywords);
        getDevice().pressEnter();
        waitForIdle();
        checkTweet();
        getDevice().pressBack();
    }

    private void postTweet() {
        ensureFindObjectById(getResourceId("composer_write")).click();
        ensureFindObjectById(getResourceId("tweet_text")).setText("Test tweet at " + new Date());
        ensureFindObjectById(getResourceId("button_tweet")).click();
        sleep(4000);
    }

    @Override
    public void run() {
        switchTab("Home");
        for (int i = 0; i < 5; i++) {
            checkTweet();
            scrollPage();
            sleep(1000);
        }
        waitForIdle();
        postTweet();
        switchTab("Search");
        searchTweet("NexusLab");
        switchTab("Notifications");
        for (int i = 0; i < 3; i++) {
            scrollPage();
        }
        switchTab("Messages");
    }

    @Override
    public boolean waitUntilReady() {
        return waitUntilFindObjectById(getResourceId("main_layout"), 5000) != null;
    }
}
