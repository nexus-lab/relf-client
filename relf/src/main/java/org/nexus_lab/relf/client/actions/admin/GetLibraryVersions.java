package org.nexus_lab.relf.client.actions.admin;

import android.content.Context;

import org.nexus_lab.relf.client.actions.Action;
import org.nexus_lab.relf.client.actions.ActionCallback;
import org.nexus_lab.relf.lib.rdfvalues.RDFNull;
import org.nexus_lab.relf.lib.rdfvalues.protodict.RDFDict;
import org.nexus_lab.relf.proto.MessageList;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

/**
 * @author Ruipeng Zhang
 */
public class GetLibraryVersions implements Action<RDFNull, RDFDict> {
    private String getSSLVersion() {
        // just a dummy text
        return BouncyCastleProvider.class.getPackage().getImplementationVersion();
    }

    private String getProtoVersion() {
        return MessageList.class.getPackage().getImplementationVersion();
    }

    @Override
    public void execute(Context context, RDFNull request, ActionCallback<RDFDict> callback) {
        RDFDict result = new RDFDict();
        result.put("SSL", getSSLVersion());
        callback.onResponse(result);
        callback.onComplete();
    }
}
