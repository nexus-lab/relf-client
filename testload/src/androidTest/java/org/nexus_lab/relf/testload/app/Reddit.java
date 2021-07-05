package org.nexus_lab.relf.testload.app;

import androidx.test.uiautomator.Direction;
import androidx.test.uiautomator.UiObject2;

import org.nexus_lab.relf.testload.core.AppMeta;
import org.nexus_lab.relf.testload.core.RunnableApp;

import static org.nexus_lab.relf.testload.util.DeviceUtil.ensureFindObjectById;
import static org.nexus_lab.relf.testload.util.DeviceUtil.sleep;
import static org.nexus_lab.relf.testload.util.DeviceUtil.waitForIdle;
import static org.nexus_lab.relf.testload.util.DeviceUtil.waitUntilFindObjectById;
import static org.nexus_lab.relf.testload.util.DeviceUtil.waitUntilFindObjectByText;

@AppMeta(name = "Reddit", packageName = "com.reddit.frontpage")
public class Reddit extends RunnableApp {
    private void switchTab(String name) {
        UiObject2 bottomNav = ensureFindObjectById(getResourceId("bottom_nav"));
        switch (name) {
            case "followed":
                bottomNav.getChildren().get(1).click();
                break;
            case "chat":
                bottomNav.getChildren().get(3).click();
                break;
            case "inbox":
                bottomNav.getChildren().get(4).click();
                break;
            case "home":
            default:
                bottomNav.getChildren().get(0).click();
                break;
        }
        sleep(1000);
    }

    private void viewPost() {
        UiObject2 title = waitUntilFindObjectById(getResourceId("link_title"), 1000);
        if (title == null) {
            return;
        }
        title = waitUntilFindObjectById(getResourceId("link_title"), 1000);
        title.click();
        waitForIdle();
        sleep(1000);
        UiObject2 detail = waitUntilFindObjectById(getResourceId("detail_list"), 1000);
        if (detail != null) {
            for (int i = 0; i < 5; i++) {
                detail.scroll(Direction.DOWN, 1f);
            }
        }
        do {
            getDevice().pressBack();
            sleep(2000);
        } while (!getDevice().getCurrentPackageName().equals(getAppMeta().packageName()));
    }

    @Override
    public void run() {
        switchTab("home");
        sleep(3000);
        UiObject2 posts = ensureFindObjectById(getResourceId("link_list"));
        for (int i = 0; i < 5; i++) {
            posts.scroll(Direction.DOWN, 1f);
        }
        switchTab("followed");
        UiObject2 rFunny = waitUntilFindObjectByText("r/funny", 1000);
        if (rFunny != null) {
            rFunny.click();
            waitForIdle();
            for (int i = 0; i < 5; i++) {
                viewPost();
                ensureFindObjectById(getResourceId("link_list")).scroll(Direction.DOWN, 1f);
                ensureFindObjectById(getResourceId("link_list")).scroll(Direction.DOWN, 1f);
                UiObject2 upvote = waitUntilFindObjectById(getResourceId("vote_view_upvote"), 1000);
                if (upvote != null) {
                    upvote.click();
                }
            }
            getDevice().pressBack();
        }
        switchTab("chat");
        switchTab("inbox");
        UiObject2 notifications = ensureFindObjectById(getResourceId("link_list"));
        if (notifications.getChildCount() > 0) {
            notifications.getChildren().get(0).click();
            waitForIdle();
            getDevice().pressBack();
        }
    }

    @Override
    public boolean waitUntilReady() {
        return waitUntilFindObjectById(getResourceId("drawer_layout"), 5000) != null;
    }
}
