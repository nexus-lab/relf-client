package org.nexus_lab.relf.lib.rdfvalues.android;

import org.nexus_lab.relf.lib.rdfvalues.structs.RDFProtoStruct;
import org.nexus_lab.relf.proto.AndroidContactInfo;

import net.badata.protobuf.converter.annotation.ProtoClass;
import net.badata.protobuf.converter.annotation.ProtoField;
import net.badata.protobuf.converter.mapping.Proto2MapperImpl;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Ruipeng Zhang
 */
@ProtoClass(value = AndroidContactInfo.class, mapper = Proto2MapperImpl.class)
public class RDFAndroidContactInfo extends RDFProtoStruct<AndroidContactInfo> {
    @Getter
    @Setter
    @ProtoField
    private String name;
    @Getter
    @Setter
    @ProtoField
    private String phoneticName;
    @Getter
    @Setter
    @ProtoField
    private String nickname;
    @Getter
    @Setter
    @ProtoField
    private String company;
    @Getter
    @Setter
    @ProtoField
    private String title;
    @Getter
    @Setter
    @ProtoField
    private List<AndroidContactInfo.AndroidPhoneNumber> phoneNumbers;
    @Getter
    @Setter
    @ProtoField
    private List<AndroidContactInfo.AndroidEmailAddress> emailAddresses;
    @Getter
    @Setter
    @ProtoField
    private List<AndroidContactInfo.AndroidPostalAddress> postalAddresses;
    @Getter
    @Setter
    @ProtoField
    private List<AndroidContactInfo.AndroidIM> ims;
    @Getter
    @Setter
    @ProtoField
    private String website;
    @Getter
    @Setter
    @ProtoField
    private String note;
    @Getter
    @Setter
    @ProtoField
    private String sip;
}
