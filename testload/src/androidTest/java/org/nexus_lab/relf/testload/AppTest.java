package org.nexus_lab.relf.testload;

import android.os.Build;
import android.util.Log;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.nexus_lab.relf.testload.core.App;
import org.nexus_lab.relf.testload.core.AppMeta;
import org.nexus_lab.relf.testload.core.RunnableApp;
import org.nexus_lab.relf.testload.core.Skipped;
import org.nexus_lab.relf.testload.watcher.ANRWatcher;

import org.atteo.classindex.ClassIndex;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;

@RunWith(AndroidJUnit4.class)
public class AppTest {
    private final static String TAG = AppTest.class.getSimpleName();
    private final static ANRWatcher anrWatcher = new ANRWatcher();

    private static ArrayList<Class<? extends RunnableApp>> apps;

    private boolean validateApp(Class<? extends RunnableApp> app) {
        Skipped skipped = app.getAnnotation(Skipped.class);
        if (skipped != null) {
            return false;
        }
        AppMeta meta = app.getAnnotation(AppMeta.class);
        if (meta == null) {
            return false;
        }
        if (meta.sdk() > -1 && meta.sdk() != Build.VERSION.SDK_INT) {
            return false;
        }
        if (meta.minSdk() > Build.VERSION.SDK_INT || meta.maxSdk() < Build.VERSION.SDK_INT) {
            return false;
        }
        return true;
    }

    private void runApp(RunnableApp app) {
        Log.i(TAG, "Launching " + app.getAppMeta().name());
        if (app.launch()) {
            if (!app.waitUntilReady()) {
                Log.e(TAG, "Timeout waiting for app " + app.getAppMeta().name() + " to be ready.");
            } else {
                try {
                    app.run();
                } catch (Exception e) {
                    Log.e(TAG, "Error running app " + app.getAppMeta().name(), e);
                }
            }
            app.exit();
        } else {
            Log.e(TAG, "Failed to launch app " + app.getAppMeta().name() + "(" +
                    app.getAppMeta().packageName() + ").");
        }
    }

    @BeforeClass
    public static void preSetup() {
        anrWatcher.start();
        apps = new ArrayList<>();
        for (Class<? extends RunnableApp> app : ClassIndex.getSubclasses(RunnableApp.class)) {
            apps.add(app);
        }
        apps.sort((a, b) -> a.getSimpleName().compareTo(b.getSimpleName()));
    }

    @Before
    public void setup() {
        App.getLauncher().unlock();
        App.getDevice().pressHome();
        App.getDevice().waitForIdle();
    }

    @Test
    public void launch() throws InstantiationException, IllegalAccessException {
        for (Class<? extends RunnableApp> app : apps) {
            if (!validateApp(app)) {
                Log.w(TAG, "Skipped " + app.getSimpleName() + ".");
                continue;
            }
            runApp(app.newInstance());
            App.getLauncher().clearRecentTasks();
        }
    }

    @After
    public void teardown() {
        App.getLauncher().clearRecentTasks();
    }

    @AfterClass
    public static void postTeardown() {
        anrWatcher.terminate();
    }
}
