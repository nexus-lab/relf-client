package org.nexus_lab.relf.client.actions.android;

import static org.junit.Assert.assertEquals;

import android.content.ContentResolver;
import android.content.Context;
import android.provider.Settings;

import androidx.test.core.app.ApplicationProvider;

import org.nexus_lab.relf.client.actions.ActionCallback;
import org.nexus_lab.relf.client.exceptions.TestSuccessException;
import org.nexus_lab.relf.lib.rdfvalues.android.RDFAndroidSystemSettings;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.TimeZone;

/**
 * @author Ruipeng Zhang
 */
@RunWith(RobolectricTestRunner.class)
public class GetAndroidSystemSettingsTest {
    private ContentResolver resolver;
    private final ActionCallback<RDFAndroidSystemSettings> callback = response -> {
        try {
            assertEquals(Settings.System.getInt(resolver, Settings.System.TIME_12_24),
                    response.getSystem().get(Settings.System.TIME_12_24));
            assertEquals(Settings.System.getString(resolver, Settings.System.DATE_FORMAT),
                    response.getSystem().get(Settings.System.DATE_FORMAT));
            assertEquals(Settings.Global.getInt(resolver, Settings.Global.ADB_ENABLED),
                    response.getGlobal().get(Settings.Global.ADB_ENABLED));
            assertEquals(Settings.Global.getString(resolver, Settings.Global.DEVICE_NAME),
                    response.getGlobal().get(Settings.Global.DEVICE_NAME));
            assertEquals(Settings.Secure.getInt(resolver, Settings.Secure.PARENTAL_CONTROL_ENABLED),
                    response.getSecure().get(Settings.Secure.PARENTAL_CONTROL_ENABLED));
            assertEquals(Settings.Secure.getString(resolver, Settings.Secure.ANDROID_ID),
                    response.getSecure().get(Settings.Secure.ANDROID_ID));
            assertEquals(TimeZone.getDefault().getID(), response.getSystem().get("timezone"));
            List<String> locales = (ArrayList) response.getSystem().get("system_locales");
            assertEquals(1, locales.size());
        } catch (Settings.SettingNotFoundException e) {
            throw new RuntimeException(e);
        }
        throw new TestSuccessException();
    };

    @Test(expected = TestSuccessException.class)
    public void execute() {
        Random random = new Random();
        Context context = ApplicationProvider.getApplicationContext();
        resolver = context.getContentResolver();
        Settings.System.putInt(resolver, Settings.System.TIME_12_24,
                random.nextBoolean() ? 12 : 24);
        Settings.System.putString(resolver, Settings.System.DATE_FORMAT,
                random.nextBoolean() ? "yyyy/MM/dd" : "MM/dd/YYYY");
        Settings.Global.putInt(resolver, Settings.Global.ADB_ENABLED, random.nextInt(2));
        Settings.Global.putString(resolver, Settings.Global.DEVICE_NAME,
                RandomStringUtils.randomAlphanumeric(6));
        Settings.Secure.putInt(resolver, Settings.Secure.PARENTAL_CONTROL_ENABLED,
                random.nextInt(2));
        Settings.Secure.putString(resolver, Settings.Secure.ANDROID_ID,
                RandomStringUtils.randomAlphanumeric(6));

        new GetAndroidSystemSettings().execute(context, null, callback);
    }
}