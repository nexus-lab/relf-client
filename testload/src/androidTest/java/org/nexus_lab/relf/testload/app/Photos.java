package org.nexus_lab.relf.testload.app;

import androidx.test.uiautomator.By;
import androidx.test.uiautomator.StaleObjectException;
import androidx.test.uiautomator.UiObject2;

import org.nexus_lab.relf.testload.core.AppMeta;
import org.nexus_lab.relf.testload.core.RunnableApp;

import static org.nexus_lab.relf.testload.util.DeviceUtil.ensureFindObjectByDesc;
import static org.nexus_lab.relf.testload.util.DeviceUtil.ensureFindObjectById;
import static org.nexus_lab.relf.testload.util.DeviceUtil.ensureFindObjectByText;
import static org.nexus_lab.relf.testload.util.DeviceUtil.findObjectById;
import static org.nexus_lab.relf.testload.util.DeviceUtil.sleep;
import static org.nexus_lab.relf.testload.util.DeviceUtil.swipeLeft;
import static org.nexus_lab.relf.testload.util.DeviceUtil.waitForIdle;
import static org.nexus_lab.relf.testload.util.DeviceUtil.waitUntilFindObject;
import static org.nexus_lab.relf.testload.util.DeviceUtil.waitUntilFindObjectById;

@AppMeta(name = "Photos", packageName = "com.google.android.apps.photos")
public class Photos extends RunnableApp {
    private void slideshow() {
        UiObject2 entry = waitUntilFindObject(By.descContains("taken on"), 1000);
        if (entry == null) {
            return;
        }
        entry.click();
        for (int i = 0; i < 5; i++) {
            swipeLeft();
            UiObject2 playVideo = waitUntilFindObjectById(getResourceId("photos_videoplayer_play_button"), 1000);
            if (playVideo != null) {
                playVideo.click();
                sleep(10000);
            } else {
                sleep(1000);
            }
        }
        getDevice().pressBack();
    }

    private void deleteAllPhotos() {
        while (true) {
            try {
                UiObject2 entry = waitUntilFindObject(By.descContains("taken on"), 1000);
                if (entry == null) {
                    return;
                }
                entry.click(2000);
            } catch (StaleObjectException e) {
                continue;
            }
            waitForIdle();
            UiObject2 checkbox = waitUntilFindObject(By.clazz("android.widget.CheckBox"), 1000);
            checkbox.click();
            UiObject2 trash = ensureFindObjectById(getResourceId("move_to_trash"));
            trash.click();
            sleep(1000);
            trash = ensureFindObjectById(getResourceId("move_to_trash"));
            trash.click();
            waitForIdle();
        }
    }

    private void emptyTrash() {
        ensureFindObjectByDesc("Show Navigation Drawer").click();
        ensureFindObjectByText("Trash").click();
        waitForIdle();
        UiObject2 menu = findObjectById(getResourceId("photos_overflow_icon"));
        if (menu == null) {
            getDevice().pressBack();
            return;
        }
        menu.click();
        ensureFindObjectByText("Empty trash").click();
        waitForIdle();
        ensureFindObjectByText("Delete").click();
        getDevice().pressBack();
    }

    @Override
    public void run() {
        slideshow();
        deleteAllPhotos();
        emptyTrash();
    }

    @Override
    public boolean waitUntilReady() {
        return waitUntilFindObjectById(getResourceId("main_container"), 5000) != null;
    }
}
