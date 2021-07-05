package org.nexus_lab.relf.lib.rdfvalues.android;

import org.nexus_lab.relf.lib.rdfvalues.structs.RDFProtoStruct;
import org.nexus_lab.relf.proto.AndroidSubscriptionInfo;

import net.badata.protobuf.converter.annotation.ProtoClass;
import net.badata.protobuf.converter.annotation.ProtoField;
import net.badata.protobuf.converter.mapping.Proto2MapperImpl;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Ruipeng Zhang
 */
@ProtoClass(value = AndroidSubscriptionInfo.class, mapper = Proto2MapperImpl.class)
public class RDFAndroidSubscriptionInfo extends RDFProtoStruct<AndroidSubscriptionInfo> {
    @Setter
    @Getter
    @ProtoField
    private Integer simSlot;
    @Setter
    @Getter
    @ProtoField
    private String simSerialNumber;
    @Setter
    @Getter
    @ProtoField
    private String phoneNumber;
    @Setter
    @Getter
    @ProtoField
    private String voicemailNumber;
    @Setter
    @Getter
    @ProtoField
    private Integer mcc;
    @Setter
    @Getter
    @ProtoField
    private Integer mnc;
    @Setter
    @Getter
    @ProtoField
    private String subscriptionName;
    @Setter
    @Getter
    @ProtoField
    private String carrierName;
    @Setter
    @Getter
    @ProtoField
    private String deviceId;
    @Setter
    @Getter
    @ProtoField
    private String subscriberId;
    @Getter
    @ProtoField
    private AndroidSubscriptionInfo.RadioType radioType;
    @Getter
    @ProtoField
    private AndroidSubscriptionInfo.DataNetworkType dataNetworkType;
    @Getter
    @ProtoField
    private AndroidSubscriptionInfo.SimState simState;
    @Getter
    @ProtoField
    private AndroidSubscriptionInfo.ServiceState serviceState;
    @Setter
    @Getter
    @ProtoField
    private String serviceInfo;
    @Setter
    @Getter
    @ProtoField
    private Integer level;
    @Setter
    @Getter
    @ProtoField
    private String signalStrength;
    @Setter
    @Getter
    @ProtoField
    private Boolean isRoaming;
    @Setter
    @Getter
    @ProtoField
    private Boolean isDataEnabled;
    @Setter
    @Getter
    @ProtoField
    private Boolean isConcurrentVoiceAndDataSupported;

    /**
     * @param type Integer value of the {@link AndroidSubscriptionInfo.RadioType}
     */
    public void setRadioType(Integer type) {
        radioType = AndroidSubscriptionInfo.RadioType.forNumber(type);
    }

    /**
     * @param type {@link AndroidSubscriptionInfo.RadioType} value
     */
    public void setRadioType(AndroidSubscriptionInfo.RadioType type) {
        radioType = type;
    }

    /**
     * @param type Integer value of the {@link AndroidSubscriptionInfo.DataNetworkType}
     */
    public void setDataNetworkType(Integer type) {
        dataNetworkType = AndroidSubscriptionInfo.DataNetworkType.forNumber(type);
    }

    /**
     * @param type {@link AndroidSubscriptionInfo.DataNetworkType} value
     */
    public void setDataNetworkType(AndroidSubscriptionInfo.DataNetworkType type) {
        dataNetworkType = type;
    }

    /**
     * @param state Integer value of the {@link AndroidSubscriptionInfo.SimState}
     */
    public void setSimState(Integer state) {
        simState = AndroidSubscriptionInfo.SimState.forNumber(state);
    }

    /**
     * @param state {@link AndroidSubscriptionInfo.SimState} value
     */
    public void setSimState(AndroidSubscriptionInfo.SimState state) {
        simState = state;
    }

    /**
     * @param state Integer value of the {@link AndroidSubscriptionInfo.ServiceState}
     */
    public void setServiceState(Integer state) {
        serviceState = AndroidSubscriptionInfo.ServiceState.forNumber(state);
    }

    /**
     * @param state {@link AndroidSubscriptionInfo.ServiceState} value
     */
    public void setServiceState(AndroidSubscriptionInfo.ServiceState state) {
        serviceState = state;
    }
}
