package org.nexus_lab.relf.testload.app;

import androidx.test.uiautomator.By;
import androidx.test.uiautomator.Direction;
import androidx.test.uiautomator.UiObject2;
import androidx.test.uiautomator.Until;

import org.nexus_lab.relf.testload.core.AppMeta;
import org.nexus_lab.relf.testload.core.RunnableApp;
import org.nexus_lab.relf.testload.util.DeviceUtil;

import java.util.regex.Pattern;

import static org.nexus_lab.relf.testload.util.DeviceUtil.sleep;
import static org.nexus_lab.relf.testload.util.DeviceUtil.waitUntilFindObjectById;

@AppMeta(name = "Calendar", packageName = "com.google.android.calendar")
public class Calendar extends RunnableApp {
    private UiObject2 openDrawer() {
        UiObject2 toolbar = getDevice().findObject(By.res(getResourceId("toolbar")));
        UiObject2 menu = toolbar.findObject(
                By.desc("Show Calendar List and Settings drawer"));
        menu.click();
        getDevice().wait(Until.findObject(By.res(getResourceId("logo_lockup_container"))), 1000);
        return getDevice().findObject(By.res(getResourceId("drawer_main_frame")));
    }

    private void clickDrawerMenu(String name) {
        UiObject2 drawer = openDrawer();
        UiObject2 menu = drawer.findObject(By.text(name));
        menu.click();
    }

    private void gotoToday() {
        UiObject2 gotoToday = getDevice().findObject(By.res(getResourceId("action_today")));
        gotoToday.click();
    }

    private void addEvent() {
        UiObject2 fab = getDevice().findObject(By.res(getResourceId("floating_action_button")));
        fab.click();
        sleep(1000);
        fab.click();
        sleep(1000);
        UiObject2 title = getDevice().wait(Until.findObject(
                By.res(getResourceId("title")).clazz("android.widget.EditText")), 1000);
        if (title == null) {
            title = getDevice().wait(Until.findObject(
                    By.res(getResourceId("title_edit_text")).clazz("android.widget.EditText")), 1000);
        }
        title.setText("Demo event");
        UiObject2 save = getDevice().wait(Until.findObject(
                By.res(getResourceId("save")).clazz("android.widget.Button")), 1000);
        save.click();
        DeviceUtil.sleep(1000);
    }

    private boolean removeEvent() {
        UiObject2 timeline = getDevice().findObject(
                By.res(getResourceId("alternate_timeline_fragment_container")));
        UiObject2 event = timeline.findObject(By.desc(Pattern.compile(".*Demo event.*")));
        if (event == null) {
            return false;
        }
        event.click();
        UiObject2 eventInfo = getDevice().wait(Until.findObject(
                By.res(getResourceId("event_info_overlay_view"))), 1000);
        UiObject2 menu = eventInfo.findObject(By.desc("More options"));
        menu.click();
        UiObject2 delete = getDevice().wait(Until.findObject(By.text("Delete")), 1000);
        delete.click();
        getDevice().wait(Until.findObject(By.text("Cancel")), 1000);
        UiObject2 confirm = getDevice().findObject(By.text("Delete"));
        confirm.click();
        getDevice().wait(Until.gone(By.res(getResourceId("event_info_overlay_view"))), 1000);
        return true;
    }

    @Override
    public void run() {
        clickDrawerMenu("Schedule");
        UiObject2 timeline = getDevice().findObject(
                By.res(getResourceId("alternate_timeline_fragment_container")));
        for (int i = 0; i < 2; i++) {
            timeline.swipe(Direction.UP, 0.5f);
        }
        gotoToday();
        clickDrawerMenu("Day");
        gotoToday();
        addEvent();
        while (removeEvent()) {
            //
        }
    }

    @Override
    public boolean waitUntilReady() {
        return waitUntilFindObjectById(getResourceId("header"), 5000) != null;
    }
}
