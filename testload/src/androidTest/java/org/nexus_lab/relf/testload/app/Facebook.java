package org.nexus_lab.relf.testload.app;

import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiObject2;
import androidx.test.uiautomator.Until;

import org.nexus_lab.relf.testload.core.AppMeta;
import org.nexus_lab.relf.testload.core.RunnableApp;
import org.nexus_lab.relf.testload.core.Skipped;

import java.util.List;

import static org.nexus_lab.relf.testload.util.DeviceUtil.ensureFindObjectByDesc;
import static org.nexus_lab.relf.testload.util.DeviceUtil.ensureFindObjectByText;
import static org.nexus_lab.relf.testload.util.DeviceUtil.sleep;
import static org.nexus_lab.relf.testload.util.DeviceUtil.swipeUp;
import static org.nexus_lab.relf.testload.util.DeviceUtil.waitUntilFindObjectByDesc;
import static org.nexus_lab.relf.testload.util.DeviceUtil.waitUntilFindObjectById;
import static org.nexus_lab.relf.testload.util.DeviceUtil.waitUntilFindObjectByText;

@Skipped
@Deprecated
@AppMeta(name = "Facebook", packageName = "com.facebook.katana")
public class Facebook extends RunnableApp {
    private List<UiObject2> getTabs() {
        UiObject2 container = getDevice()
                .findObject(By.clazz("android.widget.LinearLayout")
                        .hasChild(By.clazz("android.view.View"))
                        .res(getResourceId("(name removed)")));
        return container.getChildren();
    }

    private void switchToTab(String tab) {
        List<UiObject2> tabs = getTabs();
        switch (tab) {
            case "friends":
                tabs.get(1).click();
                return;
            case "groups":
                tabs.get(2).click();
                return;
            case "profile":
                tabs.get(3).click();
                return;
            case "notifications":
                tabs.get(4).click();
                return;
            case "functions":
                tabs.get(5).click();
                return;
            case "home":
            default:
                tabs.get(0).click();
        }
    }

    private void checkFriendRequests() {
        UiObject2 requests = waitUntilFindObjectByDesc("Requests", 2000);
        requests.click();
        sleep(2000);
        getDevice().pressBack();
        sleep(2000);
    }

    private void createNewPost() {
        ensureFindObjectByDesc("Make a post on Facebook").click();
        sleep(2000);
        UiObject2 input = getDevice().wait(Until.findObject(By.clazz("android.widget.EditText")), 4000);
        input.setText("Test facebook post");
        UiObject2 post = getDevice().findObject(By.text("POST"));
        post.click();
        sleep(5000);
    }

    private void deleteFailedPosts() {
        for (int i = 0; i < 10; i++) {
            UiObject2 menu = waitUntilFindObjectByDesc("More Menu", 1000);
            if (menu != null) {
                menu.click();
                if (ensureFindObjectByText("Discard") != null) {
                    ensureFindObjectByText("Edit").click();
                    ensureFindObjectByText("Your Posts");
                    UiObject2 menu1;
                    while ((menu1 = getDevice().findObject(By.desc("Menu"))) != null) {
                        menu1.click();
                        UiObject2 discard = ensureFindObjectByDesc("Discard Draft");
                        if (discard != null) {
                            discard.click();
                        } else {
                            discard = ensureFindObjectByDesc("Discard Post");
                            if (discard != null) {
                                discard.click();
                            }
                        }
                        waitUntilFindObjectByText("DELETE", 2000).click();
                        sleep(1000);
                    }
                    getDevice().pressBack();
                    sleep(1000);
                    break;
                }
            }
            swipeUp();
        }
    }


    @Override
    public void run() {
        switchToTab("home");
        createNewPost();
        deleteFailedPosts();
        switchToTab("friends");
        checkFriendRequests();
        switchToTab("groups");
        switchToTab("profile");
        switchToTab("notifications");
        switchToTab("functions");
    }

    @Override
    public boolean waitUntilReady() {
        UiObject2 ui = waitUntilFindObjectById(getResourceId("(name removed)"), 5000);
        return ui != null;
    }
}
