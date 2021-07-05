package org.nexus_lab.relf.testload.app;

import org.nexus_lab.relf.testload.core.AppMeta;
import org.nexus_lab.relf.testload.core.RunnableApp;

import static org.nexus_lab.relf.testload.util.DeviceUtil.swipeLeft;
import static org.nexus_lab.relf.testload.util.DeviceUtil.swipeUp;
import static org.nexus_lab.relf.testload.util.DeviceUtil.waitUntilFindObjectById;

@AppMeta(name = "1Weather", packageName = "com.handmark.expressweather")
public class OneWeather extends RunnableApp {
    private void skipAds() {
        while (waitUntilFindObjectById(getResourceId("toolbar"), 1000) == null) {
            getDevice().pressBack();
        }
    }

    @Override
    public void run() {
        for (int i = 0; i < 4; i++) {
            skipAds();
            swipeUp();
            skipAds();
            swipeLeft();
        }
    }

    @Override
    public boolean waitUntilReady() {
        return waitUntilFindObjectById(getResourceId("toolbar"), 10000) != null;
    }
}
