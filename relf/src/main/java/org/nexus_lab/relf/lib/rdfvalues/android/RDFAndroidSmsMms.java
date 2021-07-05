package org.nexus_lab.relf.lib.rdfvalues.android;

import org.nexus_lab.relf.lib.rdfvalues.RDFDatetime;
import org.nexus_lab.relf.lib.rdfvalues.structs.RDFProtoStruct;
import org.nexus_lab.relf.proto.AndroidSmsMms;

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
@ProtoClass(value = AndroidSmsMms.class, mapper = Proto2MapperImpl.class)
public class RDFAndroidSmsMms extends RDFProtoStruct<AndroidSmsMms> {
    @Getter
    @Setter
    @ProtoField
    private AndroidSmsMms.Type type;
    @Getter
    @Setter
    @ProtoField
    private AndroidSmsMms.MessageBox messageBox;
    @Getter
    @Setter
    @ProtoField
    private Long threadId;
    @Getter
    @Setter
    @ProtoField
    private Integer status;
    @Getter
    @Setter
    @ProtoField
    private List<String> from;
    @Getter
    @Setter
    @ProtoField
    private List<String> to;
    @Getter
    @Setter
    @ProtoField
    private List<String> cc;
    @Getter
    @Setter
    @ProtoField
    private List<String> bcc;
    @Getter
    @Setter
    @ProtoField(converter = RDFDatetime.Converter.class)
    private RDFDatetime dateReceived;
    @Getter
    @Setter
    @ProtoField(converter = RDFDatetime.Converter.class)
    private RDFDatetime dateSent;
    @Getter
    @Setter
    @ProtoField
    private String subject;
    @Getter
    @Setter
    @ProtoField
    private List<RDFAndroidSmsMmsBody> body;
    @Getter
    @Setter
    @ProtoField
    private Boolean isRead;
    @Getter
    @Setter
    @ProtoField
    private Boolean isSeen;
    @Getter
    @Setter
    @ProtoField
    private Boolean isLocked;
    /**
     * A temporary variable for storage address type
     */
    @Getter
    @Setter
    private Integer addressType;

    /**
     * Set the date of the receiving the message from second from epoch.
     *
     * @param second second from epoch.
     */
    public void setDateReceivedFromEpoch(long second) {
        dateReceived = new RDFDatetime(second * 1000 * 1000);
    }

    /**
     * Set the date of the sending the message from second from epoch.
     *
     * @param second second from epoch.
     */
    public void setDateSentFromEpoch(long second) {
        dateSent = new RDFDatetime(second * 1000 * 1000);
    }

    /**
     * Add a sender address of the message.
     *
     * @param address sender's address.
     */
    public void addFromAddress(String address) {
        if (from == null) {
            from = new ArrayList<>();
        }
        from.add(address);
    }

    /**
     * Add a recipient address of the message.
     *
     * @param address recipient's address.
     */
    public void addToAddress(String address) {
        if (to == null) {
            to = new ArrayList<>();
        }
        to.add(address);
    }

    /**
     * Add a carbon copy address of the message.
     *
     * @param address carbon copy recipient's address.
     */
    public void addCcAddress(String address) {
        if (cc == null) {
            cc = new ArrayList<>();
        }
        cc.add(address);
    }

    /**
     * Add a blind carbon copy address of the message.
     *
     * @param address blind carbon copy recipient's address.
     */
    public void addBccAddress(String address) {
        if (bcc == null) {
            bcc = new ArrayList<>();
        }
        bcc.add(address);
    }

    /**
     * Add a multimedia part that represent the body content of the message.
     *
     * @param part a multimedia part object.
     */
    public void addBody(RDFAndroidSmsMmsBody part) {
        if (body == null) {
            body = new ArrayList<>();
        }
        body.add(part);
    }

    /**
     * Add a string that represent the body content of the message.
     *
     * @param part a string message body.
     */
    public void addBody(String part) {
        RDFAndroidSmsMmsBody mmsBody = new RDFAndroidSmsMmsBody();
        mmsBody.setContentType("text/plain");
        mmsBody.setData(part.getBytes());
        addBody(mmsBody);
    }
}
