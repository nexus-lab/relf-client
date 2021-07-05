package org.nexus_lab.relf.testload.app;

import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiObject2;

import org.nexus_lab.relf.testload.core.AppMeta;
import org.nexus_lab.relf.testload.core.RunnableApp;

import static org.nexus_lab.relf.testload.util.DeviceUtil.ensureFindObject;
import static org.nexus_lab.relf.testload.util.DeviceUtil.sleep;
import static org.nexus_lab.relf.testload.util.DeviceUtil.swipeUp;
import static org.nexus_lab.relf.testload.util.DeviceUtil.waitUntilFindObject;
import static org.nexus_lab.relf.testload.util.DeviceUtil.waitUntilFindObjectById;
import static org.nexus_lab.relf.testload.util.DeviceUtil.waitUntilObjectGoneById;

@AppMeta(name = "Pinterest", packageName = "com.pinterest")
public class Pinterest extends RunnableApp {
    private void viewPins() {
        for (int i = 0; i < 5;) {
            UiObject2 pin = waitUntilFindObject(By.clazz("android.view.View").descContains("Pin"), 1000);
            if (pin != null && !pin.getContentDescription().contains("Promoted")) {
                i++;
                pin.click();
                UiObject2 view = waitUntilFindObject(By.clazz("androidx.viewpager.widget.ViewPager"), 1000);
                if (view == null) {
                    getDevice().pressBack();
                    continue;
                }
                swipeUp();
                UiObject2 save = waitUntilFindObjectById(getResourceId("save_pinit_bt"), 2000);
                if (save == null) {
                    getDevice().pressBack();
                } else {
                    save.click();
                    UiObject2 board = ensureFindObject(By.res(getResourceId("board_name")).text("testing"));
                    board.click();
                    waitUntilObjectGoneById(getResourceId("save_toast_container"), 2000);
                    waitUntilObjectGoneById(getResourceId("save_toast_container"), 4000);
                    getDevice().pressBack();
                }
            }
            sleep(2000);
            swipeUp();
        }

    }

    @Override
    public void run() {
        viewPins();
    }

    @Override
    public boolean waitUntilReady() {
        return waitUntilFindObjectById(getResourceId("coordinator"), 5000) != null;
    }
}
