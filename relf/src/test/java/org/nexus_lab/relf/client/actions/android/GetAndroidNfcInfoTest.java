package org.nexus_lab.relf.client.actions.android;

import android.content.Context;
import android.nfc.NfcAdapter;

import androidx.test.core.app.ApplicationProvider;

import org.nexus_lab.relf.client.exceptions.TestSuccessException;
import org.nexus_lab.relf.robolectric.ShadowNfcAdapter;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;

import java.util.Random;

/**
 * @author Ruipeng Zhang
 */
@RunWith(RobolectricTestRunner.class)
@Config(shadows = {ShadowNfcAdapter.class})
public class GetAndroidNfcInfoTest {
    @Test(expected = TestSuccessException.class)
    public void execute() {
        Random random = new Random();
        Context context = ApplicationProvider.getApplicationContext();
        NfcAdapter adapter = NfcAdapter.getDefaultAdapter(context);
        ShadowNfcAdapter shadowAdapter = (ShadowNfcAdapter) Shadows.shadowOf(adapter);
        shadowAdapter.setEnabled(random.nextBoolean());
        shadowAdapter.setNdefPushEnabled(random.nextBoolean());
        new GetAndroidNfcInfo().execute(context, null, response -> {
            Assert.assertEquals(adapter.isEnabled(), response.getIsEnabled());
            Assert.assertEquals(adapter.isNdefPushEnabled(),
                    response.getIsNdefPushEnabled());
            throw new TestSuccessException();
        });
    }
}