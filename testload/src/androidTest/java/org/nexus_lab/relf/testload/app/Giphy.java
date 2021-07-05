package org.nexus_lab.relf.testload.app;

import androidx.test.uiautomator.By;
import androidx.test.uiautomator.Direction;
import androidx.test.uiautomator.UiObject2;

import org.nexus_lab.relf.testload.core.AppMeta;
import org.nexus_lab.relf.testload.core.RunnableApp;

import static org.nexus_lab.relf.testload.util.DeviceUtil.ensureFindObjectById;
import static org.nexus_lab.relf.testload.util.DeviceUtil.findObjectById;
import static org.nexus_lab.relf.testload.util.DeviceUtil.sleep;
import static org.nexus_lab.relf.testload.util.DeviceUtil.swipeDown;
import static org.nexus_lab.relf.testload.util.DeviceUtil.swipeUp;
import static org.nexus_lab.relf.testload.util.DeviceUtil.waitUntilFindObjectById;

@AppMeta(name = "GIPHY", packageName = "com.giphy.messenger")
public class Giphy extends RunnableApp {
    private void addToFavorites() {
        UiObject2 favorite = waitUntilFindObjectById(getResourceId("favoriteBtn"), 1000);
        if (favorite != null) {
            favorite.click();
            sleep(1000);
        }
    }

    private void checkPreviewStory() {
        ensureFindObjectById(getResourceId("storyPreviewImage")).click();
        ensureFindObjectById(getResourceId("startCardContainer")).swipe(Direction.UP, 1);
        for (int i = 0; i < 8; i++) {
            if (i == 1) {
                addToFavorites();
            }
            swipeUp();
        }
        for (int i = 0; i < 5; i++) {
            swipeDown();
        }
        if (findObjectById(getResourceId("closeBtn")) != null) {
            getDevice().pressBack();
        }
    }

    private void removeAllFavorites() {
        UiObject2 favsTab = ensureFindObjectById(getResourceId("favs_button"));
        favsTab.click();
        while (true) {
            UiObject2 list = ensureFindObjectById(getResourceId("recyclerView"));
            UiObject2 image = list.findObject(By.clazz("android.widget.ImageView"));
            if (image == null) {
                break;
            }
            image.click();
            ensureFindObjectById(getResourceId("favoriteBtn")).click();
            getDevice().pressBack();
            getDevice().waitForIdle();
        }
    }

    @Override
    public void run() {
        for (int i = 0; i < 3; i++) {
            checkPreviewStory();
            getDevice().waitForIdle();
            swipeUp();
            getDevice().waitForIdle();
        }
        removeAllFavorites();
    }

    @Override
    public boolean waitUntilReady() {
        return waitUntilFindObjectById(getResourceId("mainView"), 10000) != null;
    }
}
