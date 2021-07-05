package org.nexus_lab.relf.client.actions.android;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.BLUETOOTH;
import static android.Manifest.permission.BLUETOOTH_ADMIN;
import static android.bluetooth.BluetoothAdapter.SCAN_MODE_CONNECTABLE;
import static android.bluetooth.BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE;
import static android.bluetooth.BluetoothAdapter.SCAN_MODE_NONE;
import static android.bluetooth.BluetoothDevice.BOND_BONDED;
import static android.bluetooth.BluetoothDevice.BOND_BONDING;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.ParcelUuid;

import org.nexus_lab.relf.client.actions.Action;
import org.nexus_lab.relf.client.actions.ActionCallback;
import org.nexus_lab.relf.client.actions.UsesPermission;
import org.nexus_lab.relf.client.exceptions.NoSuchDeviceException;
import org.nexus_lab.relf.lib.rdfvalues.RDFNull;
import org.nexus_lab.relf.lib.rdfvalues.android.RDFAndroidBluetoothDevice;
import org.nexus_lab.relf.lib.rdfvalues.android.RDFAndroidBluetoothInfo;
import org.nexus_lab.relf.lib.rdfvalues.client.MacAddress;
import org.nexus_lab.relf.proto.AndroidBluetoothDevice;
import org.nexus_lab.relf.proto.AndroidBluetoothInfo;
import org.nexus_lab.relf.utils.ByteUtils;

/**
 * @author Ruipeng Zhang
 */
@UsesPermission(
        allOf = {BLUETOOTH, BLUETOOTH_ADMIN},
        anyOf = {ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION}
)
public class GetAndroidBluetoothInfo implements Action<RDFNull, RDFAndroidBluetoothInfo> {
    private static final int SCAN_PERIOD = 10 * 1000;
    private static final int START_SCAN_CLASSIC = 0;
    private static final int STOP_SCAN_CLASSIC = 1;
    private static final int START_SCAN_LE = 2;
    private static final int STOP_SCAN_LE = 3;
    private static final int SEND_RESPONSE = 4;

    private final BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
    private final RDFAndroidBluetoothInfo result = new RDFAndroidBluetoothInfo();
    private final Handler handler = new Handler();
    private final BroadcastReceiver classicScanCallback = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                RDFAndroidBluetoothDevice info = createDevice(device);
                short rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MAX_VALUE);
                info.setRssi((int) rssi);
                result.addNearbyDevice(info);
            }
        }
    };
    private Context context;
    private BluetoothLeScanner scannerLE;
    private ActionCallback<RDFAndroidBluetoothInfo> callback;
    private final ScanCallback leScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult scanResult) {
            RDFAndroidBluetoothDevice info = createDevice(scanResult.getDevice());
            info.setRssi(scanResult.getRssi());
            result.addNearbyLEDevice(info);
        }

        @Override
        public void onScanFailed(int errorCode) {
            initiate(STOP_SCAN_LE);
        }
    };

    private boolean isLeSupported() {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }

    private RDFAndroidBluetoothDevice createDevice(BluetoothDevice device) {
        RDFAndroidBluetoothDevice info = new RDFAndroidBluetoothDevice();
        if (device.getAddress() != null) {
            info.setAddress(new MacAddress(ByteUtils.fromMacAddress(device.getAddress())));
        }
        if (device.getBluetoothClass() != null) {
            int deviceClass = device.getBluetoothClass().getMajorDeviceClass();
            info.setDeviceClass(AndroidBluetoothDevice.DeviceClass.forNumber(deviceClass));
        }
        switch (device.getBondState()) {
            case BOND_BONDED:
                info.setBondState(AndroidBluetoothDevice.BondState.BOND_BONDED);
                break;
            case BOND_BONDING:
                info.setBondState(AndroidBluetoothDevice.BondState.BOND_BONDING);
                break;
            default:
                info.setBondState(AndroidBluetoothDevice.BondState.BOND_NONE);
                break;
        }
        info.setName(device.getName());
        info.setType(AndroidBluetoothDevice.DeviceType.forNumber(device.getType()));
        if (device.getUuids() != null) {
            for (ParcelUuid uuid : device.getUuids()) {
                info.addUuid(uuid.toString());
            }
        }
        return info;
    }

    @SuppressLint("HardwareIds")
    private void fillResponse() {
        result.setState(AndroidBluetoothInfo.DeviceState.forNumber(adapter.getState()));
        if (adapter.getAddress() != null) {
            result.setAddress(new MacAddress(ByteUtils.fromMacAddress(adapter.getAddress())));
        }
        result.setName(adapter.getName());
        switch (adapter.getScanMode()) {
            case SCAN_MODE_CONNECTABLE:
                result.setScanMode(AndroidBluetoothInfo.ScanMode.SCAN_MODE_CONNECTABLE);
                break;
            case SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                result.setScanMode(
                        AndroidBluetoothInfo.ScanMode.SCAN_MODE_CONNECTABLE_DISCOVERABLE);
                break;
            case SCAN_MODE_NONE:
            default:
                result.setScanMode(AndroidBluetoothInfo.ScanMode.SCAN_MODE_NONE);
                break;
        }
        result.setIsMultipleAdvertisementSupported(adapter.isMultipleAdvertisementSupported());
        if (isLeSupported()) {
            result.setIsOffloadedFilteringSupported(adapter.isOffloadedFilteringSupported());
            result.setIsOffloadedScanBatchingSupported(
                    adapter.isOffloadedScanBatchingSupported());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                result.setIsLe2MPhySupported(adapter.isLe2MPhySupported());
                result.setIsLeCodedPhySupported(adapter.isLeCodedPhySupported());
                result.setIsLeExtendedAdvertisingSupported(
                        adapter.isLeExtendedAdvertisingSupported());
                result.setIsLePeriodicAdvertisingSupported(
                        adapter.isLePeriodicAdvertisingSupported());
                result.setLeMaximumAdvertisingDataLength(
                        adapter.getLeMaximumAdvertisingDataLength());
            }
        }
        for (BluetoothDevice device : adapter.getBondedDevices()) {
            result.addBondedDevice(createDevice(device));
        }
    }

    private void sendResponse() {
        if (adapter != null) {
            fillResponse();
            callback.onResponse(result);
            callback.onComplete();
        } else {
            callback.onError(new NoSuchDeviceException("Bluetooth device is not found."));
        }
    }

    private void initiate(int action) {
        if (action != SEND_RESPONSE) {
            if (adapter == null || !adapter.isEnabled()) {
                initiate(SEND_RESPONSE);
                return;
            }
            if (scannerLE == null && (action == START_SCAN_LE || action == STOP_SCAN_LE)) {
                initiate(SEND_RESPONSE);
                return;
            }
        }
        switch (action) {
            case START_SCAN_CLASSIC:
                IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                context.registerReceiver(classicScanCallback, filter);
                handler.postDelayed(() -> initiate(STOP_SCAN_CLASSIC), SCAN_PERIOD);
                break;
            case STOP_SCAN_CLASSIC:
                context.unregisterReceiver(classicScanCallback);
                initiate(START_SCAN_LE);
                break;
            case START_SCAN_LE:
                scannerLE.startScan(leScanCallback);
                handler.postDelayed(() -> initiate(STOP_SCAN_LE), SCAN_PERIOD);
                break;
            case STOP_SCAN_LE:
                scannerLE.stopScan(leScanCallback);
                initiate(SEND_RESPONSE);
                break;
            case SEND_RESPONSE:
                sendResponse();
                break;
            default:
                break;
        }
    }

    @Override
    public void execute(Context context, RDFNull request, ActionCallback<RDFAndroidBluetoothInfo> callback) {
        this.context = context;
        this.callback = callback;
        if (adapter != null && isLeSupported()) {
            scannerLE = adapter.getBluetoothLeScanner();
        }
        initiate(START_SCAN_CLASSIC);
    }
}
