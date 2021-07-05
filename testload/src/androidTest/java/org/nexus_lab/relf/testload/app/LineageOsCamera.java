package org.nexus_lab.relf.testload.app;

import androidx.test.uiautomator.UiObject2;

import org.nexus_lab.relf.testload.core.AppMeta;
import org.nexus_lab.relf.testload.core.RunnableApp;

import static org.nexus_lab.relf.testload.util.DeviceUtil.ensureFindObjectById;
import static org.nexus_lab.relf.testload.util.DeviceUtil.findObjectByDesc;
import static org.nexus_lab.relf.testload.util.DeviceUtil.findObjectById;
import static org.nexus_lab.relf.testload.util.DeviceUtil.sleep;
import static org.nexus_lab.relf.testload.util.DeviceUtil.threadSleep;
import static org.nexus_lab.relf.testload.util.DeviceUtil.waitUntilFindObjectById;

@AppMeta(name = "Camera", packageName = "org.lineageos.snap")
public class LineageOsCamera extends RunnableApp {
    private enum Mode {
        QrCode,
        Panorama,
        Video,
        Camera
    }

    private void switchToMode(Mode mode) {
        UiObject2 switcher = findObjectById(getResourceId("camera_switcher"));
        if (switcher != null) {
            switcher.click();
            waitUntilReady();
        }
        UiObject2 button = null;
        switch (mode) {
            case Panorama:
                button = findObjectByDesc("Switch to Photo Sphere");
                break;
            case QrCode:
                button = findObjectByDesc("Switch to panorama");
                break;
            case Camera:
                button = findObjectByDesc("Switch to photo");
                break;
            case Video:
                button = findObjectByDesc("Switch to video");
                break;
        }
        if (button == null) {
            throw new RuntimeException("Cannot switch to mode " + mode.name());
        }
        button.click();
        sleep(4000);
    }

    private void takePicture() {
        UiObject2 shutter = ensureFindObjectById(getResourceId("shutter_button"));
        shutter.click();
        sleep(4000);
    }

    private void takeVideo(long time) {
        UiObject2 shutter = ensureFindObjectById(getResourceId("shutter_button"));
        shutter.click();
        threadSleep(time);
        shutter = ensureFindObjectById(getResourceId("shutter_button"));
        shutter.click();
        sleep(4000);
    }

    @Override
    public String getResourceId(String id) {
        return "org.codeaurora.snapcam:id/" + id;
    }

    @Override
    public void run() {
        switchToMode(Mode.Camera);
        takePicture();
        ensureFindObjectById(getResourceId("front_back_switcher")).click();
        sleep(4000);
        takePicture();
        switchToMode(Mode.Video);
        ensureFindObjectById(getResourceId("front_back_switcher")).click();
        sleep(4000);
        takeVideo(10 * 1000 + 100);
        switchToMode(Mode.Camera);
        takePicture();
    }

    @Override
    public boolean waitUntilReady() {
        return waitUntilFindObjectById(getResourceId("filmstrip_view"), 5000) != null;
    }
}
