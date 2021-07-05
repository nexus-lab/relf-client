package org.nexus_lab.relf.testload.app;

import androidx.test.uiautomator.Direction;
import androidx.test.uiautomator.UiObject2;

import org.nexus_lab.relf.testload.core.AppMeta;
import org.nexus_lab.relf.testload.core.RunnableApp;

import static org.nexus_lab.relf.testload.util.DeviceUtil.ensureFindObjectByDesc;
import static org.nexus_lab.relf.testload.util.DeviceUtil.ensureFindObjectById;
import static org.nexus_lab.relf.testload.util.DeviceUtil.sleep;
import static org.nexus_lab.relf.testload.util.DeviceUtil.waitUntilFindObjectByDesc;
import static org.nexus_lab.relf.testload.util.DeviceUtil.waitUntilFindObjectById;
import static org.nexus_lab.relf.testload.util.DeviceUtil.waitUntilFindObjectByText;

@AppMeta(name = "Translate", packageName = "com.google.android.apps.translate")
public class Translate extends RunnableApp {
    private void chooseLang(String lang) {
        UiObject2 item;
        while ((item = waitUntilFindObjectByText(lang, 1000)) == null) {
            ensureFindObjectById("android:id/list").scroll(Direction.DOWN, 1f);
        }
        item.click();
    }

    private void translate(String text) {
        if (waitUntilFindObjectByDesc("Cancel input", 1000) != null) {
            ensureFindObjectByDesc("Cancel input").click();
        }
        UiObject2 entry = waitUntilFindObjectById(getResourceId("touch_to_type_text"), 1000);
        if (entry == null) {
            return;
        }
        entry.click();
        ensureFindObjectById(getResourceId("edit_input")).setText(text);
        getDevice().pressEnter();
        sleep(2000);
    }

    @Override
    public void run() {
        ensureFindObjectById(getResourceId("picker1")).click();
        chooseLang("DETECT LANGUAGE");
        ensureFindObjectById(getResourceId("picker2")).click();
        chooseLang("Japanese");
        translate("Hello world");
        translate("How are you?");
        ensureFindObjectById(getResourceId("picker2")).click();
        chooseLang("German");
        translate("What's up?");
        ensureFindObjectById(getResourceId("picker2")).click();
        chooseLang("Greek");
        translate("It's like Greek to me.");
    }

    @Override
    public boolean waitUntilReady() {
        return waitUntilFindObjectById(getResourceId("drawer_layout"), 5000) != null;
    }
}
