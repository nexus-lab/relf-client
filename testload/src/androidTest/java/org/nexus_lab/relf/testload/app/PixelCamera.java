package org.nexus_lab.relf.testload.app;

import androidx.test.uiautomator.By;
import androidx.test.uiautomator.Direction;
import androidx.test.uiautomator.UiObject2;

import org.nexus_lab.relf.testload.core.AppMeta;
import org.nexus_lab.relf.testload.core.RunnableApp;

import static org.nexus_lab.relf.testload.util.DeviceUtil.ensureFindObjectById;
import static org.nexus_lab.relf.testload.util.DeviceUtil.sleep;
import static org.nexus_lab.relf.testload.util.DeviceUtil.threadSleep;
import static org.nexus_lab.relf.testload.util.DeviceUtil.waitForIdle;
import static org.nexus_lab.relf.testload.util.DeviceUtil.waitUntilFindObjectById;

@AppMeta(name = "Camera", packageName = "com.google.android.GoogleCameraEng")
public class PixelCamera extends RunnableApp {
    private enum Mode {
        Panorama,
        Portrait,
        Camera,
        Video
    }

    private enum CameraLocation {
        Front,
        Back
    }

    private void switchToMode(Mode mode) {
        UiObject2 switcher = ensureFindObjectById(getResourceId("mode_switcher"));
        while (switcher.findObject(By.text(mode.name())) == null) {
            switch (mode) {
                case Panorama:
                case Portrait:
                    switcher.scroll(Direction.LEFT, 1f);
                    break;
                case Camera:
                case Video:
                    switcher.scroll(Direction.RIGHT, 1f);
                    break;
            }
            waitForIdle();
        }
        switcher.findObject(By.text(mode.name())).click();
        sleep(4000);
    }

    private void switchCamera(CameraLocation location) {
        UiObject2 switcher = ensureFindObjectById(getResourceId("camera_switch_button"));
        switch (location) {
            case Front:
                if (!switcher.getContentDescription().equals("Switch to back camera")) {
                    switcher.click();
                }
                break;
            case Back:
                if (!switcher.getContentDescription().equals("Switch to front camera")) {
                    switcher.click();
                }
                break;
        }
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

    private void cancelShot() {
        UiObject2 cancel = ensureFindObjectById(getResourceId("cancel_button"));
        cancel.click();
        sleep(4000);
    }

    @Override
    public void run() {
        switchToMode(Mode.Camera);
        takePicture();
        switchCamera(CameraLocation.Front);
        takePicture();
        switchToMode(Mode.Video);
        takeVideo(10 * 1000 + 100);
        switchToMode(Mode.Portrait);
        takePicture();
        switchCamera(CameraLocation.Back);
        takePicture();
    }

    @Override
    public boolean waitUntilReady() {
        return waitUntilFindObjectById(getResourceId("preview_overlay"), 5000) != null;
    }
}
