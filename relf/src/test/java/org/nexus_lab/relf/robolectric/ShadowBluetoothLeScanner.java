package org.nexus_lab.relf.robolectric;

import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.os.Build;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;

/**
 * Robolectric shadow {@link ShadowBluetoothLeScanner} class for testing.
 *
 * @author Ruipeng Zhang
 */
@Implements(value = BluetoothLeScanner.class, minSdk = Build.VERSION_CODES.LOLLIPOP)
public class ShadowBluetoothLeScanner {
    @Getter
    private List<ScanCallback> callbacks = new ArrayList<>();

    /**
     * Start Bluetooth LE scan with default parameters and no filters.
     *
     * @param callback Callback used to deliver scan results.
     */
    @Implementation
    public void startScan(final ScanCallback callback) {
        callbacks.add(callback);
    }


    /**
     * Stops an ongoing Bluetooth LE scan.
     *
     * @param callback Callback used to deliver scan results.
     */
    @Implementation
    public void stopScan(ScanCallback callback) {
        callbacks.remove(callback);
    }
}
