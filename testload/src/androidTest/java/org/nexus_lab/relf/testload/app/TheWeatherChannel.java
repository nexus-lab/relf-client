package org.nexus_lab.relf.testload.app;

import androidx.test.uiautomator.UiObject2;

import org.nexus_lab.relf.testload.core.AppMeta;
import org.nexus_lab.relf.testload.core.RunnableApp;

import static org.nexus_lab.relf.testload.util.DeviceUtil.sleep;
import static org.nexus_lab.relf.testload.util.DeviceUtil.swipeUp;
import static org.nexus_lab.relf.testload.util.DeviceUtil.waitUntilFindObjectById;

@AppMeta(name = "The Weather Channel", packageName = "com.weather.Weather")
public class TheWeatherChannel extends RunnableApp {
    private void switchTab(String name) {
        UiObject2 navigationView = waitUntilFindObjectById(getResourceId("bottom_navigation_view"), 1000);
        if (navigationView == null || navigationView.getChildCount() == 0) {
            return;
        }
        UiObject2 tab;
        navigationView = waitUntilFindObjectById(getResourceId("bottom_navigation_view"), 1000);
        UiObject2 tabContainer = navigationView.getChildren().get(0);
        switch (name) {
            case "Hourly":
                tab = tabContainer.getChildren().get(0);
                break;
            case "Daily":
                tab = tabContainer.getChildren().get(1);
                break;
            case "Radar":
                tab = tabContainer.getChildren().get(3);
                break;
            case "Video":
                tab = tabContainer.getChildren().get(4);
                break;
            case "Home":
            default:
                tab = tabContainer.getChildren().get(2);
                break;
        }
        tab.click();
        sleep(2000);
    }

    @Override
    public void run() {
        switchTab("Home");
        swipeUp();
        switchTab("Hourly");
        swipeUp();
        switchTab("Daily");
        swipeUp();
        switchTab("Radar");
        switchTab("Video");
        swipeUp();
    }

    @Override
    public boolean waitUntilReady() {
        return waitUntilFindObjectById(getResourceId("root_layout"), 10000) != null;
    }
}
