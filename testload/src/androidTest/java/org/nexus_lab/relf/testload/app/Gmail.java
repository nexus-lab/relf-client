package org.nexus_lab.relf.testload.app;

import androidx.test.uiautomator.By;
import androidx.test.uiautomator.Direction;
import androidx.test.uiautomator.StaleObjectException;
import androidx.test.uiautomator.UiObject2;
import androidx.test.uiautomator.Until;

import org.nexus_lab.relf.testload.core.AppMeta;
import org.nexus_lab.relf.testload.core.RunnableApp;

import java.util.List;

import static org.nexus_lab.relf.testload.util.DeviceUtil.ensureFindObjectByDesc;
import static org.nexus_lab.relf.testload.util.DeviceUtil.ensureFindObjectById;
import static org.nexus_lab.relf.testload.util.DeviceUtil.sleep;
import static org.nexus_lab.relf.testload.util.DeviceUtil.swipeDown;
import static org.nexus_lab.relf.testload.util.DeviceUtil.swipeUp;
import static org.nexus_lab.relf.testload.util.DeviceUtil.waitUntilFindObjectById;
import static org.nexus_lab.relf.testload.util.DeviceUtil.waitUntilObjectGoneById;

@AppMeta(name = "Gmail", packageName = "com.google.android.gm")
public class Gmail extends RunnableApp {
    private void sendEmail() {
        UiObject2 compose = ensureFindObjectById(getResourceId("compose_button"));
        compose.click();
        UiObject2 from = ensureFindObjectById(getResourceId("from_account_name"));
        ensureFindObjectById(getResourceId("to")).setText(from.getText());
        ensureFindObjectById(getResourceId("subject")).setText("Test email from android ui automation");
        UiObject2 bodyWrapper = ensureFindObjectById(getResourceId("body_wrapper"));
        UiObject2 input = bodyWrapper.findObject(By.text("Compose email"));
        if (input != null) {
            input.setText("This is just a test");
        }
        ensureFindObjectById(getResourceId("send")).click();
        getDevice().waitForIdle();
        waitUntilFindObjectById(getResourceId("snackbar_main_content"), 2000);
        waitUntilObjectGoneById(getResourceId("snackbar_main_content"), 5000);
    }

    private void checkEmail() {
        while (true) {
            List<UiObject2> handlers = getDevice().wait(Until.findObjects(By.res(getResourceId("contact_image"))), 5000);
            try {
                UiObject2 handler = handlers.get(handlers.size() / 2);
                handler.getParent().click();
                sleep(4000);
                swipeUp();
                break;
            } catch (StaleObjectException ignored) {
            }
        }
    }

    private void checkEmailAndGoBack() {
        checkEmail();
        getDevice().waitForIdle();
        getDevice().pressBack();
        getDevice().waitForIdle();
    }

    private void deleteEmail() {
        UiObject2 delete = ensureFindObjectById(getResourceId("delete"));
        delete.click();
    }

    private void goToPage(String page) {
        swipeDown();
        getDevice().waitForIdle();
        UiObject2 openDrawer = ensureFindObjectByDesc("Open navigation drawer");
        openDrawer.click();
        UiObject2 drawerList = ensureFindObjectById("android:id/list");
        UiObject2 menu;
        int counter = 0;
        while ((menu = drawerList.findObject(By.text(page))) == null) {
            drawerList.scroll(Direction.DOWN, 0.5f);
            if (counter++ > 2) {
                break;
            }
        }
        if (menu != null) {
            menu.click();
            getDevice().waitForIdle();
        }
    }

    @Override
    public void run() {
        for (int i = 0; i < 3; i++) {
            swipeUp();
            sleep(1000);
            checkEmailAndGoBack();
        }
        sleep(2000);
        sendEmail();
        goToPage("Sent");
        checkEmail();
        deleteEmail();
    }

    @Override
    public boolean waitUntilReady() {
        if (waitUntilFindObjectById(getResourceId("drawer_container"), 3000) != null) {
            return true;
        }
        return waitUntilFindObjectById(getResourceId("content_container"), 1000) != null;
    }
}
