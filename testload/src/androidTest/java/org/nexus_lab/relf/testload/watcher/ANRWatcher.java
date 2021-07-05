package org.nexus_lab.relf.testload.watcher;

import androidx.test.uiautomator.By;
import androidx.test.uiautomator.StaleObjectException;
import androidx.test.uiautomator.UiObject2;

import static org.nexus_lab.relf.testload.util.DeviceUtil.findObject;
import static org.nexus_lab.relf.testload.util.DeviceUtil.threadSleep;

public class ANRWatcher extends Thread {
    private boolean stopped = false;
    private String lastTitle = null;

    private void closeIsntResponding() {
        try {
            UiObject2 title = findObject(By.textContains("isn't responding"));
            UiObject2 close = findObject(By.res("android:id/aerr_close"));
            UiObject2 wait = findObject(By.res("android:id/aerr_wait"));
            if (title != null) {
                if (title.getText().equals(lastTitle)) {
                    if (close != null) {
                        close.click();
                    }
                } else if (wait != null) {
                    lastTitle = title.getText();
                    wait.click();
                }
            }
        } catch (StaleObjectException ignored) {
        }
    }

    private void closeHasStopped() {
        try {
            UiObject2 title = findObject(By.textContains("has stopped"));
            UiObject2 close = findObject(By.res("android:id/aerr_mute"));
            if (title != null && close != null) {
                close.click();
            }
        } catch (StaleObjectException ignored) {
        }
    }

    @Override
    public void run() {
        while (!stopped) {
            closeIsntResponding();
            closeHasStopped();
            threadSleep(2000);
        }
    }

    public void terminate() {
        stopped = true;
    }
}
