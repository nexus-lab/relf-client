package org.nexus_lab.relf.lib.rdfvalues.android;

import org.nexus_lab.relf.lib.rdfvalues.structs.RDFProtoStruct;
import org.nexus_lab.relf.proto.AndroidTelephonyInfo;

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
@ProtoClass(value = AndroidTelephonyInfo.class, mapper = Proto2MapperImpl.class)
public class RDFAndroidTelephonyInfo extends RDFProtoStruct<AndroidTelephonyInfo> {
    @Getter
    @Setter
    @ProtoField
    private List<RDFAndroidCellInfo> cells;
    @Getter
    @Setter
    @ProtoField
    private List<RDFAndroidSubscriptionInfo> subscriptions;
    @Getter
    @Setter
    @ProtoField
    private AndroidTelephonyInfo.CallState callState;
    @Getter
    @Setter
    @ProtoField
    private AndroidTelephonyInfo.DataState dataState;
    @Getter
    @Setter
    @ProtoField
    private AndroidTelephonyInfo.DataActivity dataActivity;
    @Getter
    @Setter
    @ProtoField
    private Boolean isSmsCapable;
    @Getter
    @Setter
    @ProtoField
    private Boolean isVoiceCapable;
    @Getter
    @Setter
    @ProtoField
    private Integer phoneCount;

    /**
     * @param cell Cell information to be added
     */
    public void addCell(RDFAndroidCellInfo cell) {
        if (cells == null) {
            cells = new ArrayList<>();
        }
        cells.add(cell);
    }

    /**
     * @param subscription Cell information to be added
     */
    public void addSubscription(RDFAndroidSubscriptionInfo subscription) {
        if (subscriptions == null) {
            subscriptions = new ArrayList<>();
        }
        subscriptions.add(subscription);
    }
}
