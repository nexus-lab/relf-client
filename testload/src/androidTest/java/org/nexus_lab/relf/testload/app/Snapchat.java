package org.nexus_lab.relf.testload.app;

import androidx.test.uiautomator.By;
import androidx.test.uiautomator.Direction;
import androidx.test.uiautomator.UiObject2;

import org.nexus_lab.relf.testload.core.AppMeta;
import org.nexus_lab.relf.testload.core.RunnableApp;

import java.util.Date;

import static org.nexus_lab.relf.testload.util.DeviceUtil.ensureFindObject;
import static org.nexus_lab.relf.testload.util.DeviceUtil.ensureFindObjectById;
import static org.nexus_lab.relf.testload.util.DeviceUtil.ensureFindObjectByText;
import static org.nexus_lab.relf.testload.util.DeviceUtil.findObjectById;
import static org.nexus_lab.relf.testload.util.DeviceUtil.findObjectByText;
import static org.nexus_lab.relf.testload.util.DeviceUtil.sleep;
import static org.nexus_lab.relf.testload.util.DeviceUtil.swipeDown;
import static org.nexus_lab.relf.testload.util.DeviceUtil.swipeLeft;
import static org.nexus_lab.relf.testload.util.DeviceUtil.waitUntilFindObjectById;

@AppMeta(name = "Snapchat", packageName = "com.snapchat.android")
public class Snapchat extends RunnableApp {
    private void switchTo(String location) {
        switch (location) {
            case "chat":
                ensureFindObjectById(getResourceId("feed_icon_container")).click();
                break;
            case "discover":
                ensureFindObjectById(getResourceId("discoverfeed_icon_container")).click();
                break;
            case "camera":
            default:
                if (waitUntilFindObjectById(getResourceId("camera_flip_button"), 5000) == null) {
                    ensureFindObjectById(getResourceId("camera_capture_button")).click();
                }
                break;
        }
        sleep(3000);
    }

    private void login() {
        if (findObjectById(getResourceId("login_signup_landing_container")) == null) {
            return;
        }
        ensureFindObjectById(getResourceId("login_and_signup_page_fragment_login_button")).click();
        ensureFindObjectById(getResourceId("username_or_email_field")).setText("torvaldsigurd");
        ensureFindObjectById(getResourceId("password_field")).setText("<YOUR SNAPCHAT PASSWORD>");
        ensureFindObject(By.res(getResourceId("button_text")).text("Log In")).click();
        waitUntilFindObjectById(getResourceId("full_screen_surface_view"), 5000);
    }

    private void logout() {
        ensureFindObjectById(getResourceId("avatar_story")).click();
        ensureFindObjectById(getResourceId("profile_header_menu_button")).click();
        while (findObjectByText("Log Out") == null) {
            findObjectById(getResourceId("settings_items_layout")).scroll(Direction.DOWN, 1f);
        }
        ensureFindObjectByText("Log Out").click();
        sleep(2000);
        if (findObjectByText("Save Login Information") != null) {
            ensureFindObject(By.res(getResourceId("button_text")).text("Yes")).getParent().click();
            sleep(1000);
        }
        ensureFindObject(By.res(getResourceId("button_text")).text("Log Out")).getParent().click();
        sleep(2000);
    }

    private void takeSnapAndAddToStory() {
        ensureFindObjectById(getResourceId("camera_capture_button")).click();
        sleep(3000);
        ensureFindObjectById(getResourceId("send_btn")).click();
        ensureFindObjectByText("My Story").click();
        ensureFindObjectById(getResourceId("send_to_bottom_panel_send_button")).click();
    }

    private void takeSnapAndSendToSelf() {
        ensureFindObjectById(getResourceId("camera_capture_button")).click();
        sleep(3000);
        ensureFindObjectById(getResourceId("send_btn")).click();
        ensureFindObjectByText("Torvald Sigurd (me)").click();
        ensureFindObjectById(getResourceId("send_to_bottom_panel_send_button")).click();
    }

    private void talkToSelf() {
        do {
            UiObject2 self = findObjectByText("Torvald Sigurd");
            if (self == null) {
                return;
            }
            self.click();
            if (findObjectById(getResourceId("full_screen_surface_view")) != null) {
                findObjectById(getResourceId("full_screen_surface_view")).click();
                sleep(1000);
                if (findObjectByText("Add Friends") != null) {
                    getDevice().pressBack();
                }
                self = findObjectByText("Torvald Sigurd");
                if (self == null) {
                    return;
                }
                self.click();
            }
        } while (findObjectById(getResourceId("chat_input_text_field")) == null);
        UiObject2 input = ensureFindObjectById(getResourceId("chat_input_text_field"));
        input.setText("hello from myself at " + new Date().toString());
        getDevice().pressEnter();
        sleep(3000);
        getDevice().pressBack();
        getDevice().pressBack();
    }

    private void viewDiscoverStory() {
        UiObject2 frame = waitUntilFindObjectById(getResourceId("frame"), 1000);
        if (frame != null) {
            frame.click();
            sleep(5000);
            swipeLeft();
            sleep(5000);
            swipeDown();
        }
        sleep(2000);
    }

    @Override
    public void run() {
        login();
        switchTo("camera");
        takeSnapAndAddToStory();
        takeSnapAndSendToSelf();
        switchTo("chat");
        talkToSelf();
        switchTo("discover");
        viewDiscoverStory();
        logout();
    }

    @Override
    public boolean waitUntilReady() {
        UiObject2 loginPage = waitUntilFindObjectById(getResourceId("login_signup_landing_container"), 5000);
        if (loginPage != null) {
            return true;
        }
        return waitUntilFindObjectById(getResourceId("full_screen_surface_view"), 5000) != null;
    }

    @Override
    public boolean exit() {
        getDevice().pressHome();
        return true;
    }
}
