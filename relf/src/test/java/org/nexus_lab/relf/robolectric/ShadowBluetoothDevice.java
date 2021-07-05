package org.nexus_lab.relf.robolectric;

import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.os.Build;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

import lombok.Setter;

/**
 * Robolectric shadow {@link BluetoothDevice} class for testing.
 *
 * @author Ruipeng Zhang
 */
@Implements(value = BluetoothDevice.class, minSdk = Build.VERSION_CODES.LOLLIPOP)
public class ShadowBluetoothDevice extends org.robolectric.shadows.ShadowBluetoothDevice {
    @Setter
    private int type;

    @Setter
    private BluetoothClass bluetoothClass;

    /**
     * Get the Bluetooth class of the remote device.
     *
     * @return Bluetooth class object, or null on error
     */
    @Implementation
    public BluetoothClass getBluetoothClass() {
        return bluetoothClass;
    }

    /**
     * @return the Bluetooth device type of the remote device.
     */
    @Implementation
    public int getType() {
        return type;
    }
}
