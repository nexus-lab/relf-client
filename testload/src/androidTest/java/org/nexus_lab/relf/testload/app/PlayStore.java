package org.nexus_lab.relf.testload.app;

import androidx.test.uiautomator.By;
import androidx.test.uiautomator.Direction;
import androidx.test.uiautomator.UiObject2;

import org.nexus_lab.relf.testload.core.AppMeta;
import org.nexus_lab.relf.testload.core.RunnableApp;

import static org.nexus_lab.relf.testload.util.DeviceUtil.ensureFindObject;
import static org.nexus_lab.relf.testload.util.DeviceUtil.ensureFindObjectById;
import static org.nexus_lab.relf.testload.util.DeviceUtil.swipeDown;
import static org.nexus_lab.relf.testload.util.DeviceUtil.swipeUp;
import static org.nexus_lab.relf.testload.util.DeviceUtil.waitForIdle;
import static org.nexus_lab.relf.testload.util.DeviceUtil.waitUntilFindObjectById;

@AppMeta(name = "Play Store", packageName = "com.android.vending")
public class PlayStore extends RunnableApp {
    private void switchTab(String name) {
        int counter = 0;
        UiObject2 tabContainer = ensureFindObjectById(getResourceId("play_header_list_tab_container"));
        UiObject2 tab = tabContainer.findObject(By.text(name));
        if (tab == null) {
            for (int i = 0; i < 3; i++) {
                tabContainer.findObjects(By.res(getResourceId("title")))
                        .get(1)
                        .swipe(Direction.RIGHT, 1f);
            }
            while ((tab = tabContainer.findObject(By.text(name))) == null) {
                if (counter++ > 3) {
                    break;
                }
                tabContainer.swipe(Direction.LEFT, 0.5f);
            }
        }
        if (tab != null) {
            tab.click();
        }
    }

    private void browseTab() {
        for (int i = 0; i < 3; i++) {
            swipeUp();
        }
        waitForIdle();
    }

    private void search(String keywords) {
        swipeDown();
        UiObject2 searchBar = ensureFindObjectById(getResourceId("search_bar"));
        searchBar.click();
        UiObject2 input = ensureFindObject(By.clazz("android.widget.EditText"));
        input.setText(keywords);
        getDevice().pressEnter();
        UiObject2 result = waitUntilFindObjectById(getResourceId("bucket_items"), 5000);
        if (result != null) {
            result.findObject(By.clazz("android.widget.ImageView")).click();
        }
        waitForIdle();
    }

    @Override
    public void run() {
        switchTab("Top charts");
        browseTab();
        switchTab("Early access");
        browseTab();
        search("Android Auto");
        browseTab();
        getDevice().pressBack();
        search("Google");
        browseTab();
    }

    @Override
    public boolean waitUntilReady() {
        return waitUntilFindObjectById(getResourceId("drawer_layout"), 5000) != null;
    }
}
