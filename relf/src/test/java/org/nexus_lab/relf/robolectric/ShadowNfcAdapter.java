package org.nexus_lab.relf.robolectric;

import android.nfc.NfcAdapter;
import android.os.Build;

import org.robolectric.annotation.Implements;

import lombok.Setter;

/**
 * Robolectric shadow {@link android.nfc.NfcAdapter} class for testing.
 *
 * @author Ruipeng Zhang
 */
@Implements(value = NfcAdapter.class, minSdk = Build.VERSION_CODES.LOLLIPOP)
public class ShadowNfcAdapter extends org.robolectric.shadows.ShadowNfcAdapter {
    @Setter
    private boolean isNdefPushEnabled;

    /**
     * Return true if the NDEF Push (Android Beam) feature is enabled.
     *
     * @return true if NDEF Push feature is enabled.
     */
    public boolean isNdefPushEnabled() {
        return isNdefPushEnabled;
    }
}
