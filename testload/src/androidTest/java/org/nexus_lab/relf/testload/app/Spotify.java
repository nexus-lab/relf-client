package org.nexus_lab.relf.testload.app;

import android.util.Log;

import androidx.test.uiautomator.By;
import androidx.test.uiautomator.Direction;
import androidx.test.uiautomator.UiObject2;

import org.nexus_lab.relf.testload.core.AppMeta;
import org.nexus_lab.relf.testload.core.RunnableApp;

import static org.nexus_lab.relf.testload.util.DeviceUtil.ensureFindObject;
import static org.nexus_lab.relf.testload.util.DeviceUtil.ensureFindObjectById;
import static org.nexus_lab.relf.testload.util.DeviceUtil.findObjectById;
import static org.nexus_lab.relf.testload.util.DeviceUtil.findObjectByText;
import static org.nexus_lab.relf.testload.util.DeviceUtil.sleep;
import static org.nexus_lab.relf.testload.util.DeviceUtil.threadSleep;
import static org.nexus_lab.relf.testload.util.DeviceUtil.waitUntilFindObjectById;
import static org.nexus_lab.relf.testload.util.DeviceUtil.waitUntilFindObjectByText;

@AppMeta(name = "Spotify", packageName = "com.spotify.music")
public class Spotify extends RunnableApp {
    private boolean exited = false;
    private final Thread removeAd = new Thread() {
        private final String[] keywords = new String[]{
                "ADVERTISEMENT",
                "Advertisement",
                "NO, THANKS",
                "Dismiss",
                "DISMISS",
        };

        private void closeAd() {
            for (String keyword : keywords) {
                if (findObjectByText(keyword) != null) {
                    getDevice().pressBack();
                    Log.i(Spotify.class.getSimpleName(), "Closed AD by keyword " + keyword);
                    return;
                }
            }
        }

        @Override
        public void run() {
            while (!exited) {
                closeAd();
                threadSleep(1000);
            }
        }
    };

    private void browseHome() {
        UiObject2 body = ensureFindObjectById(getResourceId("home_body"));
        for (int i = 0; i < 5; i++) {
            body.scroll(Direction.DOWN, 1f);
        }
    }

    private void switchTab(String name) {
        if (waitUntilFindObjectById(getResourceId("navigation_bar"), 1000) == null) {
            return;
        }
        switch (name) {
            case "Search":
                ensureFindObjectById(getResourceId("search_tab")).click();
                break;
            case "Your Library":
                ensureFindObjectById(getResourceId("your_library_tab")).click();
                break;
            case "Premium":
                ensureFindObjectById(getResourceId("premium_tab")).click();
                break;
            case "Home":
            default:
                ensureFindObjectById(getResourceId("home_tab")).click();
                break;
        }
    }

    private void shufflePlay() {
        UiObject2 shufflePlay = waitUntilFindObjectByText("SHUFFLE PLAY", 3000);
        if (shufflePlay != null) {
            shufflePlay.click();
        }
        sleep(1000);
        getDevice().pressBack();
    }

    private void search(String keywords) {
        ensureFindObjectById(getResourceId("find_search_field")).click();
        ensureFindObject(By.clazz("android.widget.EditText").res(getResourceId("query"))).setText(keywords);
        getDevice().pressEnter();
    }

    private void playOrPause(boolean play) {
        UiObject2 playPause = ensureFindObjectById(getResourceId("play_pause_button"));
        if (!play && !playPause.getContentDescription().equals("Play")) {
            playPause.click();
        } else if (play && playPause.getContentDescription().equals("Play")) {
            playPause.click();
        }
        closePlayerOverlay();
    }

    private void closePlayerOverlay() {
        UiObject2 overlay = waitUntilFindObjectById(getResourceId("player_overlay"), 1000);
        if (overlay != null) {
            getDevice().pressBack();
        }
    }

    private void switchSecondaryTab(String name) {
        UiObject2 tab = ensureFindObject(By.res(getResourceId("bottom_tab_view_label")).text(name));
        if (tab != null) {
            tab.click();
        }
    }

    @Override
    public void run() {
        removeAd.start();
        closePlayerOverlay();
        switchTab("Home");
        browseHome();
        switchTab("Search");
        search("Underground Lindsey Stirling");
        ensureFindObjectById(getResourceId("row_view")).click();
        shufflePlay();
        sleep(10000);
        while (findObjectById(getResourceId("play_pause_button")) == null) {
            getDevice().pressBack();
            sleep(1000);
        }
        playOrPause(false);
        switchTab("Your Library");
        switchSecondaryTab("Albums");
        ensureFindObjectById(getResourceId("row_view")).click();
        shufflePlay();
        sleep(10000);
        playOrPause(false);
    }

    @Override
    public boolean waitUntilReady() {
        return waitUntilFindObjectById(getResourceId("content"), 5000) != null;
    }

    @Override
    public boolean exit() {
        exited = true;
        return super.exit();
    }
}
