package org.nexus_lab.relf.lib.rdfvalues.android;

import org.nexus_lab.relf.lib.rdfvalues.RDFDatetime;
import org.nexus_lab.relf.lib.rdfvalues.structs.RDFProtoStruct;
import org.nexus_lab.relf.proto.AndroidUserProfile;

import net.badata.protobuf.converter.annotation.ProtoClass;
import net.badata.protobuf.converter.annotation.ProtoField;
import net.badata.protobuf.converter.mapping.Proto2MapperImpl;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Ruipeng Zhang
 */
@ProtoClass(value = AndroidUserProfile.class, mapper = Proto2MapperImpl.class)
public class RDFAndroidUserProfile extends RDFProtoStruct<AndroidUserProfile> {
    @Getter
    @Setter
    @ProtoField
    private Integer id;
    @Getter
    @Setter
    @ProtoField
    private Integer serialNumber;
    @Getter
    @Setter
    @ProtoField
    private String name;
    @Getter
    @Setter
    @ProtoField(converter = RDFDatetime.Converter.class)
    private RDFDatetime creationTime;
    @Getter
    @Setter
    @ProtoField(converter = RDFDatetime.Converter.class)
    private RDFDatetime lastLoggedInTime;
    @Getter
    @Setter
    @ProtoField
    private String lastLoggedInFingerprint;
    @Getter
    @Setter
    @ProtoField
    private Boolean isPrimary;
    @Getter
    @Setter
    @ProtoField
    private Boolean isAdmin;
    @Getter
    @Setter
    @ProtoField
    private Boolean isGuest;
    @Getter
    @Setter
    @ProtoField
    private Boolean isGuestToRemove;
    @Getter
    @Setter
    @ProtoField
    private Boolean isSystem;
    @Getter
    @Setter
    @ProtoField
    private Boolean isSystemOnly;
    @Getter
    @Setter
    @ProtoField
    private Boolean isPartial;
    @Getter
    @Setter
    @ProtoField
    private Boolean isRestricted;
    @Getter
    @Setter
    @ProtoField
    private Boolean isManagedProfile;
    @Getter
    @Setter
    @ProtoField
    private Boolean isEnabled;
    @Getter
    @Setter
    @ProtoField
    private Boolean isQuietModeEnabled;
    @Getter
    @Setter
    @ProtoField
    private Boolean isEphemeral;
    @Getter
    @Setter
    @ProtoField
    private Boolean isInitialized;
    @Getter
    @Setter
    @ProtoField
    private Boolean isDemo;
    @Getter
    @Setter
    @ProtoField
    private Boolean isUserRunning;
    @Getter
    @Setter
    @ProtoField
    private Boolean isUserStopping;
    @Getter
    @Setter
    @ProtoField
    private Boolean isUserUnlocked;
    @Getter
    @Setter
    @ProtoField
    private Boolean supportsSwitchTo;
    @Getter
    @Setter
    @ProtoField
    private Boolean supportsSwitchToByUser;

    /**
     * Set user creation time from milliseconds from epoch.
     *
     * @param milliseconds user creation time in milliseconds from epoch.
     */
    public void setCreationTimeFromEpoch(Long milliseconds) {
        creationTime = new RDFDatetime(milliseconds * 1000);
    }

    /**
     * Set user last logged in time from milliseconds from epoch.
     *
     * @param milliseconds user last logged in time in milliseconds from epoch.
     */
    public void setLastLoggedInTimeFromEpoch(Long milliseconds) {
        lastLoggedInTime = new RDFDatetime(milliseconds * 1000);
    }
}
