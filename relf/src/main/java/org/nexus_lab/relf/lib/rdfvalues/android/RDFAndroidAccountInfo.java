package org.nexus_lab.relf.lib.rdfvalues.android;

import org.nexus_lab.relf.lib.rdfvalues.structs.RDFProtoStruct;
import org.nexus_lab.relf.proto.AndroidAccount;
import org.nexus_lab.relf.proto.AndroidAccountAuthenticator;
import org.nexus_lab.relf.proto.AndroidAccountInfo;

import net.badata.protobuf.converter.annotation.ProtoClass;
import net.badata.protobuf.converter.annotation.ProtoField;
import net.badata.protobuf.converter.mapping.Proto2MapperImpl;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * RDF wrapper of {@link AndroidAccountInfo}
 *
 * @author Ruipeng Zhang
 */
@ProtoClass(value = AndroidAccountInfo.class, mapper = Proto2MapperImpl.class)
public class RDFAndroidAccountInfo extends RDFProtoStruct<AndroidAccountInfo> {
    @Getter
    @Setter
    @ProtoField
    private List<AndroidAccount> accounts;
    @Getter
    @Setter
    @ProtoField
    private List<AndroidAccountAuthenticator> authenticators;

    /**
     * Add an account to the account list.
     *
     * @param account account to be added.
     */
    public void addAccount(AndroidAccount account) {
        if (accounts == null) {
            accounts = new ArrayList<>();
        }
        accounts.add(account);
    }

    /**
     * Add an authenticator to the authenticator list.
     *
     * @param authenticator authenticator to be added.
     */
    public void addAuthenticator(AndroidAccountAuthenticator authenticator) {
        if (authenticators == null) {
            authenticators = new ArrayList<>();
        }
        authenticators.add(authenticator);
    }
}
