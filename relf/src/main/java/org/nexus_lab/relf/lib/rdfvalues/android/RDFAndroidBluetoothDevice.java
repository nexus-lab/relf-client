package org.nexus_lab.relf.lib.rdfvalues.android;

import org.nexus_lab.relf.lib.rdfvalues.client.MacAddress;
import org.nexus_lab.relf.lib.rdfvalues.structs.RDFProtoStruct;
import org.nexus_lab.relf.proto.AndroidBluetoothDevice;

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
@ProtoClass(value = AndroidBluetoothDevice.class, mapper = Proto2MapperImpl.class)
public class RDFAndroidBluetoothDevice extends RDFProtoStruct<AndroidBluetoothDevice> {
    @Getter
    @Setter
    @ProtoField(converter = MacAddress.Converter.class)
    private MacAddress address;
    @Getter
    @Setter
    @ProtoField
    private AndroidBluetoothDevice.DeviceClass deviceClass;
    @Getter
    @Setter
    @ProtoField
    private AndroidBluetoothDevice.BondState bondState;
    @Getter
    @Setter
    @ProtoField
    private String name;
    @Getter
    @Setter
    @ProtoField
    private AndroidBluetoothDevice.DeviceType type;
    @Getter
    @Setter
    @ProtoField
    private List<String> uuid;
    @Getter
    @Setter
    @ProtoField
    private Integer rssi;

    /**
     * Add a supported feature (UUID) to the feature list.
     *
     * @param uuid the UUID of a supported feature
     */
    public void addUuid(String uuid) {
        if (this.uuid == null) {
            this.uuid = new ArrayList<>();
        }
        this.uuid.add(uuid);
    }
}
