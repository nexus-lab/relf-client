package org.nexus_lab.relf.testload.app;

import android.graphics.Point;
import android.graphics.Rect;

import androidx.test.uiautomator.By;
import androidx.test.uiautomator.Direction;
import androidx.test.uiautomator.UiObject2;

import org.nexus_lab.relf.testload.core.AppMeta;
import org.nexus_lab.relf.testload.core.RunnableApp;

import static org.nexus_lab.relf.testload.util.DeviceUtil.ensureFindObjectById;
import static org.nexus_lab.relf.testload.util.DeviceUtil.ensureFindObjectByText;
import static org.nexus_lab.relf.testload.util.DeviceUtil.sleep;
import static org.nexus_lab.relf.testload.util.DeviceUtil.threadSleep;
import static org.nexus_lab.relf.testload.util.DeviceUtil.waitForIdle;
import static org.nexus_lab.relf.testload.util.DeviceUtil.waitUntilFindObject;
import static org.nexus_lab.relf.testload.util.DeviceUtil.waitUntilFindObjectByDesc;
import static org.nexus_lab.relf.testload.util.DeviceUtil.waitUntilFindObjectById;
import static org.nexus_lab.relf.testload.util.DeviceUtil.waitUntilObjectGoneById;

@AppMeta(name = "Clock", packageName = "com.google.android.deskclock")
public class Clock extends RunnableApp {
    private enum Tab {
        Alarm,
        Clock,
        Timer,
        Stopwatch
    }

    private void switchToTab(Tab tab) {
        UiObject2 tabs = ensureFindObjectById(getResourceId("tabs"));
        UiObject2 button;
        switch (tab) {
            case Alarm:
                button = tabs.findObject(By.desc("Alarm"));
                break;
            case Clock:
                button = tabs.findObject(By.desc("Clock"));
                break;
            case Timer:
                button = tabs.findObject(By.desc("Timer"));
                break;
            case Stopwatch:
                button = tabs.findObject(By.desc("Stopwatch"));
                break;
            default:
                return;
        }
        if (!button.isSelected()) {
            button.click();
        }
    }

    private void addGlobalTime(String city) {
        switchToTab(Tab.Clock);
        UiObject2 cityName = waitUntilFindObject(By.res(getResourceId("city_name")).text(city), 1000);
        if (cityName != null) {
            return;
        }
        UiObject2 fab = ensureFindObjectById(getResourceId("fab"));
        fab.click();
        UiObject2 searchBar = ensureFindObjectById(getResourceId("search_src_text"));
        searchBar.setText(city);
        UiObject2 results = ensureFindObjectById(getResourceId("search_results_view"));
        results.getChildren().get(0).click();
        waitForIdle();
    }

    private void removeGlobalTime(String city) {
        switchToTab(Tab.Clock);
        UiObject2 cityName = waitUntilFindObject(By.res(getResourceId("city_name")).text(city), 1000);
        if (cityName == null) {
            return;
        }
        UiObject2 item = cityName.getParent().getParent().getParent();
        Rect originArea = item.getVisibleBounds();
        Point origin = new Point((originArea.left + originArea.right) / 2, (originArea.top + originArea.bottom) / 2);
        UiObject2 fab = ensureFindObjectById(getResourceId("fab"));
        Rect targetArea = fab.getVisibleBounds();
        Point target = new Point((targetArea.left + targetArea.right) / 2, (targetArea.top + targetArea.bottom) / 2);
        getDevice().swipe(new Point[]{origin, origin, target}, 50);
        waitForIdle();
    }

    // minute must be a multiple of 5
    private void addAlarm(int hour, int minute, boolean am) {
        switchToTab(Tab.Alarm);
        ensureFindObjectById(getResourceId("fab")).click();
        waitForIdle();
        ensureFindObjectById("android:id/hours").click();
        ensureFindObjectById("android:id/radial_picker").findObject(By.desc(String.valueOf(hour))).click();
        ensureFindObjectById("android:id/minutes").click();
        ensureFindObjectById("android:id/radial_picker").findObject(By.desc(String.format("%02d", minute))).click();
        ensureFindObjectById(am ? "android:id/am_label" : "android:id/pm_label").click();
        ensureFindObjectByText("OK").click();
        waitForIdle();
    }

    private void removeAlarm(int hour, int minute, boolean am) {
        switchToTab(Tab.Alarm);
        String time = hour + ":" + minute + " " + (am ? "AM" : "PM");
        UiObject2 list = ensureFindObjectById(getResourceId("alarm_recycler_view"));
        UiObject2 label = waitUntilFindObjectByDesc(time, 1000);
        while (list.scroll(Direction.UP, 1)) {
            // scroll to the top
        }
        while (label == null) {
            if (!list.scroll(Direction.DOWN, 1)) {
                break;
            }
            waitForIdle();
            label = waitUntilFindObjectByDesc(time, 1000);
        }
        if (label != null) {
            UiObject2 collapse = label.getParent().findObject(By.res(getResourceId("arrow")));
            if (collapse.getContentDescription().equals("Expand alarm")) {
                collapse.click();
            }
            sleep(1000);
            label = waitUntilFindObjectByDesc(time, 1000);
            label.getParent().findObject(By.res(getResourceId("delete"))).click();
        }
        waitForIdle();
    }

    private void runStopWatch(long duration) {
        switchToTab(Tab.Stopwatch);
        UiObject2 fab = ensureFindObjectById(getResourceId("fab"));
        Point[] buttons = new Point[]{
                new Point(fab.getVisibleBounds().centerX(), fab.getVisibleBounds().centerY()),
                new Point(fab.getVisibleBounds().centerX() / 2, fab.getVisibleBounds().centerY())
        };
        // start stop watch
        getDevice().click(buttons[0].x, buttons[0].y);
        threadSleep(duration);
        // stop stopwatch
        getDevice().click(buttons[0].x, buttons[0].y);
        threadSleep(2 * 1000);
        // reset stopwatch
        getDevice().click(buttons[1].x, buttons[1].y);
    }

    @Override
    public void run() {
        addGlobalTime("London, UK");
        addAlarm(1, 20, true);
        addGlobalTime("Beijing");
        addAlarm(9, 45, false);
        addGlobalTime("Chattanooga");
        removeAlarm(1, 20, true);
        removeAlarm(9, 45, false);
        waitUntilFindObjectById(getResourceId("snackbar_text"), 4000);
        waitUntilObjectGoneById(getResourceId("snackbar_text"), 4000);
        removeGlobalTime("London");
        removeGlobalTime("Chattanooga");
        removeGlobalTime("Beijing");
        runStopWatch(10 * 1000);
    }

    @Override
    public boolean waitUntilReady() {
        UiObject2 ui = waitUntilFindObjectById(getResourceId("app_bar_layout"), 5000);
        return ui != null;
    }
}
