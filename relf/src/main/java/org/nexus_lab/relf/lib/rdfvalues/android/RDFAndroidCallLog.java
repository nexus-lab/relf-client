package org.nexus_lab.relf.lib.rdfvalues.android;

import org.nexus_lab.relf.lib.rdfvalues.Duration;
import org.nexus_lab.relf.lib.rdfvalues.RDFDatetime;
import org.nexus_lab.relf.lib.rdfvalues.structs.RDFProtoStruct;
import org.nexus_lab.relf.proto.AndroidCallLog;

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
@ProtoClass(value = AndroidCallLog.class, mapper = Proto2MapperImpl.class)
public class RDFAndroidCallLog extends RDFProtoStruct<AndroidCallLog> {
    @Getter
    @Setter
    @ProtoField
    private AndroidCallLog.CallType type;
    @Getter
    @Setter
    @ProtoField
    private String phoneNumber;
    @Getter
    @Setter
    @ProtoField
    private String contactName;
    @Getter
    @Setter
    @ProtoField(converter = RDFDatetime.Converter.class)
    private RDFDatetime date;
    @Getter
    @Setter
    @ProtoField(converter = Duration.Converter.class)
    private Duration duration;
    @Getter
    @Setter
    @ProtoField
    private Boolean isAcknowledged;
    @Getter
    @Setter
    @ProtoField
    private String countryIso;
    @Getter
    @Setter
    @ProtoField
    private String geocodedLocation;
    @Getter
    @Setter
    @ProtoField
    private List<AndroidCallLog.CallFeatures> features;
    @Getter
    @Setter
    @ProtoField
    private AndroidCallLog.NumberPresentation presentation;

    /**
     * Set the date of the call from milliseconds from epoch.
     *
     * @param milliseconds milliseconds from epoch.
     */
    public void setDateFromEpoch(long milliseconds) {
        date = new RDFDatetime(milliseconds * 1000);
    }

    /**
     * Set call duration from seconds.
     *
     * @param seconds call duration in seconds.
     */
    public void setDurationFromSeconds(long seconds) {
        duration = new Duration(seconds);
    }

    /**
     * Set call features from bits.
     *
     * @param bits the bit representation of features.
     */
    public void setFeaturesFromBits(int bits) {
        if (features == null) {
            features = new ArrayList<>();
        }
        for (AndroidCallLog.CallFeatures feature : AndroidCallLog.CallFeatures.values()) {
            if ((bits & feature.getNumber()) > 0) {
                features.add(feature);
            }
        }
    }
}
