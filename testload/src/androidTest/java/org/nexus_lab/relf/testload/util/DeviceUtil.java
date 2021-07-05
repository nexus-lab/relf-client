package org.nexus_lab.relf.testload.util;

import androidx.test.uiautomator.By;
import androidx.test.uiautomator.BySelector;
import androidx.test.uiautomator.UiObject2;
import androidx.test.uiautomator.Until;

import org.nexus_lab.relf.testload.core.App;

public class DeviceUtil {
    public static UiObject2 findObject(BySelector selector) {
        return App.getDevice().findObject(selector);
    }

    public static UiObject2 findObjectById(String id) {
        return findObject(By.res(id));
    }

    public static UiObject2 findObjectByText(String text) {
        return findObject(By.text(text));
    }

    public static UiObject2 findObjectByDesc(String desc) {
        return findObject(By.desc(desc));
    }

    public static UiObject2 ensureFindObject(BySelector selector) {
        int tries = 0;
        UiObject2 object = null;
        while (object == null) {
            object = App.getDevice().wait(Until.findObject(selector), 1000);
            if (++tries >= 30) {
                throw new RuntimeException("Cannot find object " + selector);
            }
        }
        return object;
    }

    public static UiObject2 ensureFindObjectById(String resourceId) {
        return ensureFindObject(By.res(resourceId));
    }

    public static UiObject2 ensureFindObjectByDesc(String desc) {
        return ensureFindObject(By.desc(desc));
    }

    public static UiObject2 ensureFindObjectByText(String text) {
        return ensureFindObject(By.text(text));
    }

    public static UiObject2 waitUntilFindObject(BySelector selector, long timeout) {
        return App.getDevice().wait(Until.findObject(selector), timeout);
    }

    public static UiObject2 waitUntilFindObjectById(String resourceId, long timeout) {
        return waitUntilFindObject(By.res(resourceId), timeout);
    }

    public static UiObject2 waitUntilFindObjectByDesc(String desc, long timeout) {
        return waitUntilFindObject(By.desc(desc), timeout);
    }

    public static UiObject2 waitUntilFindObjectByText(String text, long timeout) {
        return waitUntilFindObject(By.text(text), timeout);
    }

    public static void waitUntilObjectGoneById(String resourceId, long timeout) {
        App.getDevice().wait(Until.gone(By.res(resourceId)), timeout);
    }

    public static void waitForIdle() {
        App.getDevice().waitForIdle();
    }

    public static void sleep(long timeout) {
        App.getDevice().wait(Until.findObject(By.res("nonexistent")), timeout);
    }

    public static void threadSleep(long timeout) {
        try {
            Thread.sleep(timeout);
        } catch (InterruptedException ignored) {
        }
    }

    public static void swipeDown() {
        App.getDevice().swipe(
                App.getDevice().getDisplayWidth() / 2,
                400,
                App.getDevice().getDisplayWidth() / 2,
                App.getDevice().getDisplayHeight() - 200,
                30
        );
    }

    public static void swipeUp() {
        App.getDevice().swipe(
                App.getDevice().getDisplayWidth() / 2,
                App.getDevice().getDisplayHeight() - 200,
                App.getDevice().getDisplayWidth() / 2,
                400,
                30
        );
    }

    public static void swipeLeft() {
        App.getDevice().swipe(
                App.getDevice().getDisplayWidth() - 100,
                App.getDevice().getDisplayHeight() / 2,
                100,
                App.getDevice().getDisplayHeight() / 2,
                30
        );
    }

    public static void swipeRight() {
        App.getDevice().swipe(
                100,
                App.getDevice().getDisplayHeight() / 2,
                App.getDevice().getDisplayWidth() - 100,
                App.getDevice().getDisplayHeight() / 2,
                30
        );
    }
}
