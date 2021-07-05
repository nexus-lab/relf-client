package org.nexus_lab.relf.lib.rdfvalues.android;

import org.nexus_lab.relf.lib.rdfvalues.client.MacAddress;
import org.nexus_lab.relf.lib.rdfvalues.structs.RDFProtoStruct;
import org.nexus_lab.relf.proto.AndroidBluetoothInfo;

import net.badata.protobuf.converter.annotation.ProtoClass;
import net.badata.protobuf.converter.annotation.ProtoField;
import net.badata.protobuf.converter.mapping.Proto2MapperImpl;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Ruipeng Zhang
 */
@ProtoClass(value = AndroidBluetoothInfo.class, mapper = Proto2MapperImpl.class)
public class RDFAndroidBluetoothInfo extends RDFProtoStruct<AndroidBluetoothInfo> {
    @Getter
    @Setter
    @ProtoField
    private AndroidBluetoothInfo.DeviceState state;
    @Getter
    @Setter
    @ProtoField(converter = MacAddress.Converter.class)
    private MacAddress address;
    @Getter
    @Setter
    @ProtoField
    private String name;
    @Getter
    @Setter
    @ProtoField
    private AndroidBluetoothInfo.ScanMode scanMode;
    @Getter
    @Setter
    @ProtoField
    private List<RDFAndroidBluetoothDevice> bondedDevices;
    @Getter
    @Setter
    @ProtoField
    private List<RDFAndroidBluetoothDevice> nearbyDevices;
    @Getter
    @Setter
    @ProtoField
    private List<RDFAndroidBluetoothDevice> nearbyLEDevices;
    @Getter
    @Setter
    @ProtoField
    private Boolean isMultipleAdvertisementSupported;
    @Getter
    @Setter
    @ProtoField
    private Boolean isOffloadedFilteringSupported;
    @Getter
    @Setter
    @ProtoField
    private Boolean isOffloadedScanBatchingSupported;
    @Getter
    @Setter
    @ProtoField
    private Boolean isLe2MPhySupported;
    @Getter
    @Setter
    @ProtoField
    private Boolean isLeCodedPhySupported;
    @Getter
    @Setter
    @ProtoField
    private Boolean isLeExtendedAdvertisingSupported;
    @Getter
    @Setter
    @ProtoField
    private Boolean isLePeriodicAdvertisingSupported;
    @Getter
    @Setter
    @ProtoField
    private Integer leMaximumAdvertisingDataLength;

    /**
     * Add a Bluetooth device to the bonded/paired device list.
     *
     * @param device a Bluetooth device.
     */
    public void addBondedDevice(RDFAndroidBluetoothDevice device) {
        if (bondedDevices == null) {
            bondedDevices = new ArrayList<>();
        }
        bondedDevices.add(device);
    }

    /**
     * Add a Bluetooth device to the scanned device list.
     *
     * @param device a Bluetooth device.
     */
    public void addNearbyDevice(RDFAndroidBluetoothDevice device) {
        if (nearbyDevices == null) {
            nearbyDevices = new ArrayList<>();
        }
        nearbyDevices.add(device);
    }

    /**
     * Add a Bluetooth device to the scanned BLE device list.
     *
     * @param device a BLE device.
     */
    public void addNearbyLEDevice(RDFAndroidBluetoothDevice device) {
        if (nearbyLEDevices == null) {
            nearbyLEDevices = new ArrayList<>();
        }
        nearbyLEDevices.add(device);
    }
}
