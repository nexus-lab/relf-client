package org.nexus_lab.relf.testload.app;

import android.os.RemoteException;

import androidx.test.uiautomator.Direction;
import androidx.test.uiautomator.UiObject2;

import org.nexus_lab.relf.testload.core.AppMeta;
import org.nexus_lab.relf.testload.core.RunnableApp;

import static org.nexus_lab.relf.testload.util.DeviceUtil.ensureFindObjectById;
import static org.nexus_lab.relf.testload.util.DeviceUtil.sleep;
import static org.nexus_lab.relf.testload.util.DeviceUtil.waitForIdle;
import static org.nexus_lab.relf.testload.util.DeviceUtil.waitUntilFindObjectById;

@AppMeta(name = "Pluto TV", packageName = "tv.pluto.android")
public class PlutoTV extends RunnableApp {
    @Override
    public void run() {
        try {
            getDevice().setOrientationNatural();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        UiObject2 list = ensureFindObjectById(getResourceId("guide_rv"));
        while (list.scroll(Direction.UP, 1f)) {
            waitForIdle();
        }
        for (int i = 0; i < 5; i++) {
            ensureFindObjectById(getResourceId("guide_timeline")).click();
            sleep(5000);
            list.scroll(Direction.DOWN, 1f);
        }
        try {
            getDevice().setOrientationNatural();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean waitUntilReady() {
        return waitUntilFindObjectById(getResourceId("main_content"), 10000) != null;
    }
}
