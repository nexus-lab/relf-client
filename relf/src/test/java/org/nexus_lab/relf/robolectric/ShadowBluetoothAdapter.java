package org.nexus_lab.relf.robolectric;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.os.Build;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

import lombok.Setter;

/**
 * Robolectric shadow {@link BluetoothAdapter} class for testing.
 *
 * @author Ruipeng Zhang
 */
@Implements(value = BluetoothAdapter.class, minSdk = Build.VERSION_CODES.LOLLIPOP)
public class ShadowBluetoothAdapter extends org.robolectric.shadows.ShadowBluetoothAdapter {
    @Setter
    private boolean isLePeriodicAdvertisingSupported;
    @Setter
    private boolean isOffloadedFilteringSupported;
    @Setter
    private boolean isOffloadedScanBatchingSupported;
    @Setter
    private boolean isLe2MPhySupported;
    @Setter
    private boolean isLeCodedPhySupported;
    @Setter
    private boolean isLeExtendedAdvertisingSupported;
    @Setter
    private int leMaximumAdvertisingDataLength;
    @Setter
    private BluetoothLeScanner bluetoothLeScanner;

    /**
     * @return a {@link BluetoothLeScanner} object for Bluetooth LE scan operations.
     */
    @Implementation
    public BluetoothLeScanner getBluetoothLeScanner() {
        return bluetoothLeScanner;
    }

    /**
     * @return true if chipset supports on-chip filtering.
     */
    @Implementation
    public boolean isOffloadedFilteringSupported() {
        return isOffloadedFilteringSupported;
    }

    /**
     * @return true if chipset supports on-chip scan batching.
     */
    @Implementation
    public boolean isOffloadedScanBatchingSupported() {
        return isOffloadedScanBatchingSupported;
    }

    /**
     * @return true if chipset supports LE 2M PHY feature.
     */
    @Implementation(minSdk = Build.VERSION_CODES.O)
    public boolean isLe2MPhySupported() {
        return isLe2MPhySupported;
    }

    /**
     * @return true if chipset supports LE Coded PHY feature.
     */
    @Implementation(minSdk = Build.VERSION_CODES.O)
    public boolean isLeCodedPhySupported() {
        return isLeCodedPhySupported;
    }

    /**
     * @return true if chipset supports LE Extended Advertising feature.
     */
    @Implementation(minSdk = Build.VERSION_CODES.O)
    public boolean isLeExtendedAdvertisingSupported() {
        return isLeExtendedAdvertisingSupported;
    }

    /**
     * @return true if chipset supports LE Periodic Advertising feature.
     */
    @Implementation(minSdk = Build.VERSION_CODES.O)
    public boolean isLePeriodicAdvertisingSupported() {
        return isLePeriodicAdvertisingSupported;
    }

    /**
     * @return the maximum LE advertising data length in bytes,
     * if LE Extended Advertising feature is supported, 0 otherwise.
     */
    @Implementation(minSdk = Build.VERSION_CODES.O)
    public int getLeMaximumAdvertisingDataLength() {
        return leMaximumAdvertisingDataLength;
    }
}
