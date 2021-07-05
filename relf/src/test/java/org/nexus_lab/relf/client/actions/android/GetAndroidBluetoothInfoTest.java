package org.nexus_lab.relf.client.actions.android;


import static android.bluetooth.BluetoothDevice.BOND_BONDED;
import static android.bluetooth.BluetoothDevice.BOND_BONDING;

import static org.nexus_lab.relf.utils.ReflectionUtils.Parameter.from;
import static org.nexus_lab.relf.utils.ReflectionUtils.callConstructor;
import static org.nexus_lab.relf.utils.ReflectionUtils.callInstanceMethod;
import static org.nexus_lab.relf.utils.ReflectionUtils.setFieldValue;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Looper;
import android.os.ParcelUuid;

import androidx.test.core.app.ApplicationProvider;

import com.github.javafaker.Faker;
import org.nexus_lab.relf.client.actions.ActionCallback;
import org.nexus_lab.relf.client.exceptions.TestSuccessException;
import org.nexus_lab.relf.lib.rdfvalues.android.RDFAndroidBluetoothDevice;
import org.nexus_lab.relf.lib.rdfvalues.android.RDFAndroidBluetoothInfo;
import org.nexus_lab.relf.proto.AndroidBluetoothDevice;
import org.nexus_lab.relf.proto.AndroidBluetoothInfo;
import org.nexus_lab.relf.robolectric.ShadowBluetoothAdapter;
import org.nexus_lab.relf.robolectric.ShadowBluetoothDevice;
import org.nexus_lab.relf.robolectric.ShadowBluetoothLeScanner;
import org.nexus_lab.relf.utils.RandomUtils;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.shadows.ShadowApplication;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;


/**
 * @author Ruipeng Zhang
 */
@RunWith(RobolectricTestRunner.class)
@Config(shadows = {
        ShadowBluetoothAdapter.class,
        ShadowBluetoothLeScanner.class,
        ShadowBluetoothDevice.class
})
public class GetAndroidBluetoothInfoTest {
    private final List<BluetoothDevice> bondedDevices = new ArrayList<>();
    private final List<BluetoothDevice> nearbyDevices = new ArrayList<>();
    private final List<BluetoothDevice> nearbyLeDevices = new ArrayList<>();
    private final List<Short> nearbyRssi = new ArrayList<>();
    private final List<Short> nearbyLeRssi = new ArrayList<>();
    private final ActionCallback<RDFAndroidBluetoothInfo> callback = bluetoothInfo -> {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        assertEquals(adapter.getState(), bluetoothInfo.getState().getNumber());
        assertEquals(adapter.getAddress(),
                bluetoothInfo.getAddress().getHumanReadableAddress());
        assertEquals(adapter.getName(), bluetoothInfo.getName());
        assertEquals(mapScanMode(adapter.getScanMode()), bluetoothInfo.getScanMode());
        assertEquals(adapter.isMultipleAdvertisementSupported(),
                bluetoothInfo.getIsMultipleAdvertisementSupported());
        assertEquals(adapter.isOffloadedFilteringSupported(),
                bluetoothInfo.getIsOffloadedFilteringSupported());
        assertEquals(adapter.isOffloadedScanBatchingSupported(),
                bluetoothInfo.getIsOffloadedScanBatchingSupported());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            assertEquals(adapter.isLe2MPhySupported(), bluetoothInfo.getIsLe2MPhySupported());
            assertEquals(adapter.isLeCodedPhySupported(),
                    bluetoothInfo.getIsLeCodedPhySupported());
            assertEquals(adapter.isLeExtendedAdvertisingSupported(),
                    bluetoothInfo.getIsLeExtendedAdvertisingSupported());
            assertEquals(adapter.isLePeriodicAdvertisingSupported(),
                    bluetoothInfo.getIsLePeriodicAdvertisingSupported());
            assertEquals(adapter.getLeMaximumAdvertisingDataLength(),
                    bluetoothInfo.getLeMaximumAdvertisingDataLength().longValue());
        }
        assertEquals(bondedDevices.size(), bluetoothInfo.getBondedDevices().size());
        for (int i = 0; i < bondedDevices.size(); i++) {
            assertDeviceEquals(bondedDevices.get(i), bluetoothInfo.getBondedDevices().get(i));
        }
        assertEquals(nearbyDevices.size(), bluetoothInfo.getNearbyDevices().size());
        for (int i = 0; i < nearbyDevices.size(); i++) {
            assertDeviceEquals(nearbyDevices.get(i), bluetoothInfo.getNearbyDevices().get(i));
            assertEquals(nearbyRssi.get(i).shortValue(), bluetoothInfo.getNearbyDevices().get(
                    i).getRssi().shortValue());
        }
        assertEquals(nearbyLeDevices.size(), bluetoothInfo.getNearbyLEDevices().size());
        for (int i = 0; i < nearbyLeDevices.size(); i++) {
            assertDeviceEquals(nearbyLeDevices.get(i),
                    bluetoothInfo.getNearbyLEDevices().get(i));
            assertEquals(nearbyLeRssi.get(i).shortValue(),
                    bluetoothInfo.getNearbyLEDevices().get(i).getRssi().shortValue());
        }
        throw new TestSuccessException();
    };
    private Context context;

    private void assertDeviceEquals(BluetoothDevice device, RDFAndroidBluetoothDevice info) {
        assertEquals(device.getAddress(), info.getAddress().getHumanReadableAddress());
        assertEquals(device.getBluetoothClass().getMajorDeviceClass(),
                info.getDeviceClass().getNumber());
        assertEquals(mapBondState(device.getBondState()), info.getBondState());
        assertEquals(device.getName(), info.getName());
        assertEquals(device.getType(), info.getType().getNumber());
        String[] uuids = new String[device.getUuids().length];
        for (int i = 0; i < uuids.length; i++) {
            uuids[i] = device.getUuids()[i].toString();
        }
        assertArrayEquals(uuids, info.getUuid().toArray(new String[0]));
    }

    private AndroidBluetoothInfo.ScanMode mapScanMode(int scanMode) {
        switch (scanMode) {
            case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                return AndroidBluetoothInfo.ScanMode.SCAN_MODE_CONNECTABLE;
            case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                return AndroidBluetoothInfo.ScanMode.SCAN_MODE_CONNECTABLE_DISCOVERABLE;
            default:
                return AndroidBluetoothInfo.ScanMode.SCAN_MODE_NONE;
        }
    }

    private AndroidBluetoothDevice.BondState mapBondState(int bondState) {
        switch (bondState) {
            case BOND_BONDED:
                return AndroidBluetoothDevice.BondState.BOND_BONDED;
            case BOND_BONDING:
                return AndroidBluetoothDevice.BondState.BOND_BONDING;
            default:
                return AndroidBluetoothDevice.BondState.BOND_NONE;
        }
    }

    private BluetoothDevice createDevice(boolean isLeDevice) throws Exception {
        Faker faker = new Faker();
        BluetoothDevice device = callConstructor(BluetoothDevice.class,
                from(String.class, faker.internet().macAddress().toUpperCase()));
        ShadowBluetoothDevice shadowDevice = (ShadowBluetoothDevice) Shadows.shadowOf(device);
        shadowDevice.setBondState(RandomUtils.oneOf(
                BluetoothDevice.BOND_BONDED,
                BluetoothDevice.BOND_BONDING,
                BluetoothDevice.BOND_NONE));
        shadowDevice.setName(faker.team().name());
        shadowDevice.setUuids(new ParcelUuid[]{
                new ParcelUuid(UUID.randomUUID()),
                new ParcelUuid(UUID.randomUUID()),
                new ParcelUuid(UUID.randomUUID())
        });
        shadowDevice.setType(
                isLeDevice ? BluetoothDevice.DEVICE_TYPE_LE : BluetoothDevice.DEVICE_TYPE_CLASSIC);
        shadowDevice.setBluetoothClass(
                callConstructor(BluetoothClass.class, from(int.class, RandomUtils.oneOf(
                        BluetoothClass.Device.Major.MISC,
                        BluetoothClass.Device.Major.COMPUTER,
                        BluetoothClass.Device.Major.PHONE,
                        BluetoothClass.Device.Major.NETWORKING,
                        BluetoothClass.Device.Major.AUDIO_VIDEO,
                        BluetoothClass.Device.Major.PERIPHERAL,
                        BluetoothClass.Device.Major.IMAGING,
                        BluetoothClass.Device.Major.WEARABLE,
                        BluetoothClass.Device.Major.TOY,
                        BluetoothClass.Device.Major.HEALTH,
                        BluetoothClass.Device.Major.UNCATEGORIZED))));
        return device;
    }

    private ScanResult createScanResult() throws Exception {
        Random random = new Random();
        BluetoothDevice device = createDevice(true);
        short rssi = (short) random.nextInt(100);
        nearbyLeDevices.add(device);
        nearbyLeRssi.add(rssi);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return new ScanResult(device, 0, 0, 0, 0, 0, rssi, 0, null,
                    System.currentTimeMillis() * 1000);
        } else {
            return new ScanResult(device, null, rssi, System.currentTimeMillis() * 1000);
        }
    }

    private void sendBroadcast() throws Exception {
        Random random = new Random();
        Intent intent = new Intent(BluetoothDevice.ACTION_FOUND);
        BluetoothDevice device = createDevice(false);
        short rssi = (short) random.nextInt(100);
        nearbyDevices.add(device);
        nearbyRssi.add(rssi);
        intent.putExtra(BluetoothDevice.EXTRA_DEVICE, device);
        intent.putExtra(BluetoothDevice.EXTRA_RSSI, rssi);
        context.sendBroadcast(intent);
    }

    @Before
    public void setup() throws Exception {
        Faker faker = new Faker();
        Random random = new Random();
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        ShadowBluetoothAdapter shadowAdapter = (ShadowBluetoothAdapter) Shadows.shadowOf(adapter);
        shadowAdapter.setEnabled(true);
        shadowAdapter.setAddress(faker.internet().macAddress().toUpperCase());
        shadowAdapter.setState(RandomUtils.oneOf(
                BluetoothAdapter.STATE_DISCONNECTED,
                BluetoothAdapter.STATE_CONNECTING,
                BluetoothAdapter.STATE_CONNECTED,
                BluetoothAdapter.STATE_DISCONNECTING
        ));
        callInstanceMethod(adapter, "setScanMode", from(int.class, RandomUtils.oneOf(
                BluetoothAdapter.SCAN_MODE_NONE,
                BluetoothAdapter.SCAN_MODE_CONNECTABLE,
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE)));
        shadowAdapter.setIsMultipleAdvertisementSupported(random.nextBoolean());

        context = ApplicationProvider.getApplicationContext();
        Shadows.shadowOf(context.getPackageManager()).setSystemFeature(
                PackageManager.FEATURE_BLUETOOTH_LE, true);

        Set<BluetoothDevice> bondedDevices = new HashSet<>();
        for (int i = 0; i < 5; i++) {
            BluetoothDevice device = createDevice(false);
            bondedDevices.add(device);
        }
        this.bondedDevices.addAll(bondedDevices);
        shadowAdapter.setBondedDevices(bondedDevices);
        ShadowApplication application = Shadows.shadowOf((Application) context);
        setFieldValue(application, "bluetoothAdapter", adapter);
    }

    @Test(expected = TestSuccessException.class)
    public void execute() throws Exception {
        boolean hasNext;
        boolean classicScanStarted = false;
        boolean leScanStarted = false;
        ShadowBluetoothAdapter adapter = (ShadowBluetoothAdapter) Shadows.shadowOf(
                BluetoothAdapter.getDefaultAdapter());
        BluetoothLeScanner scanner = callConstructor(BluetoothLeScanner.class);
        ShadowBluetoothLeScanner shadowScanner = Shadow.extract(scanner);
        adapter.setBluetoothLeScanner(scanner);
        new GetAndroidBluetoothInfo().execute(context, null, callback);
        do {
            if (!classicScanStarted) {
                for (int i = 0; i < 3; i++) {
                    sendBroadcast();
                }
                classicScanStarted = true;
            }
            List<ScanCallback> leCallbacks = shadowScanner.getCallbacks();
            if (!leScanStarted && leCallbacks.size() > 0) {
                for (ScanCallback callback : leCallbacks) {
                    for (int i = 0; i < 3; i++) {
                        callback.onScanResult(0, createScanResult());
                    }
                }
                leScanStarted = true;
            }
            Thread.sleep(100);
            hasNext = Shadows.shadowOf(
                    Looper.myLooper()).getScheduler().advanceToNextPostedRunnable();
        } while (hasNext);
    }
}