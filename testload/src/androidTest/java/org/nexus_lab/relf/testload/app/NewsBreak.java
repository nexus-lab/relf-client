package org.nexus_lab.relf.testload.app;

import androidx.test.uiautomator.UiObject2;

import org.nexus_lab.relf.testload.core.AppMeta;
import org.nexus_lab.relf.testload.core.RunnableApp;

import static org.nexus_lab.relf.testload.util.DeviceUtil.sleep;
import static org.nexus_lab.relf.testload.util.DeviceUtil.swipeLeft;
import static org.nexus_lab.relf.testload.util.DeviceUtil.swipeUp;
import static org.nexus_lab.relf.testload.util.DeviceUtil.waitForIdle;
import static org.nexus_lab.relf.testload.util.DeviceUtil.waitUntilFindObjectById;

@AppMeta(name = "News Break", packageName = "com.particlenews.newsbreak")
public class NewsBreak extends RunnableApp {
    private void readAPieceOfNews() {
        sleep(5000);
        UiObject2 newsHandle = waitUntilFindObjectById(getResourceId("news_normal"), 1000);
        if (newsHandle == null) {
            newsHandle = waitUntilFindObjectById(getResourceId("channel_news_normal_item"), 1000);
        }
        if (newsHandle == null) {
            return;
        }
        newsHandle.click();
        waitUntilFindObjectById(getResourceId("title"), 2000);
        for (int i = 0; i < 5; i++) {
            swipeUp();
        }
        // skip ads
        while (waitUntilFindObjectById(getResourceId("navi_tabs"), 1000) == null) {
            getDevice().pressBack();
        }
    }

    @Override
    public void run() {
        for (int i = 0; i < 5; i++) {
            swipeLeft();
            waitForIdle();
            readAPieceOfNews();
        }
    }

    @Override
    public boolean waitUntilReady() {
        return waitUntilFindObjectById(getResourceId("frag_content"), 5000) != null;
    }
}
