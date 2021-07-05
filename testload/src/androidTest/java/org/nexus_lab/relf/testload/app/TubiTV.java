package org.nexus_lab.relf.testload.app;

import android.os.RemoteException;

import androidx.test.uiautomator.UiObject2;

import org.nexus_lab.relf.testload.core.AppMeta;
import org.nexus_lab.relf.testload.core.RunnableApp;

import static org.nexus_lab.relf.testload.util.DeviceUtil.ensureFindObjectById;
import static org.nexus_lab.relf.testload.util.DeviceUtil.ensureFindObjectByText;
import static org.nexus_lab.relf.testload.util.DeviceUtil.sleep;
import static org.nexus_lab.relf.testload.util.DeviceUtil.swipeUp;
import static org.nexus_lab.relf.testload.util.DeviceUtil.waitUntilFindObjectById;

@AppMeta(name = "Tubi", packageName = "com.tubitv")
public class TubiTV extends RunnableApp {
    private void switchTabs(String name) {
        UiObject2 tabs = waitUntilFindObjectById("android:id/tabs", 1000);
        if (tabs == null) {
            return;
        }
        switch (name) {
            case "Browse":
                tabs.getChildren().get(1).click();
                break;
            case "Search":
                tabs.getChildren().get(2).click();
                break;
            case "Account":
                tabs.getChildren().get(3).click();
                break;
            case "Home":
            default:
                tabs.getChildren().get(0).click();
                break;
        }
        sleep(2000);
    }

    private void viewMoive() {
        UiObject2 item = waitUntilFindObjectById(getResourceId("view_home_content_iv"), 1000);
        if (item == null) {
            item = waitUntilFindObjectById(getResourceId("video_poster_image_view"), 1000);
            if (item == null) {
                return;
            }
        }
        item.click();
        sleep(1000);
        UiObject2 play = ensureFindObjectById(getResourceId("imageView_play"));
        play.click();
        sleep(10 * 1000);
        getDevice().click(getDevice().getDisplayWidth() / 2, getDevice().getDisplayHeight() / 2);
        ensureFindObjectById(getResourceId("view_tubi_controller_play_toggle_ib")).click();
        getDevice().pressBack();
        getDevice().pressBack();
        try {
            getDevice().setOrientationNatural();
        } catch (RemoteException e) {
            e.printStackTrace(System.err);
        }
    }

    @Override
    public void run() {
        switchTabs("Home");
        swipeUp();
        viewMoive();
        switchTabs("Browse");
        ensureFindObjectByText("Featured").click();
        sleep(1000);
        swipeUp();
        viewMoive();
    }

    @Override
    public boolean waitUntilReady() {
        return waitUntilFindObjectById(getResourceId("root_layout"), 10000) != null;
    }
}
