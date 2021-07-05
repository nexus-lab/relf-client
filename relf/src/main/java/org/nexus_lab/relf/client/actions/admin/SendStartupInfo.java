package org.nexus_lab.relf.client.actions.admin;

import android.content.Context;
import android.os.SystemClock;

import org.nexus_lab.relf.client.actions.Action;
import org.nexus_lab.relf.client.actions.ActionCallback;
import org.nexus_lab.relf.lib.rdfvalues.RDFNull;
import org.nexus_lab.relf.lib.rdfvalues.SessionID;
import org.nexus_lab.relf.lib.rdfvalues.client.RDFStartupInfo;

/**
 * @author Ruipeng Zhang
 */
public class SendStartupInfo implements Action<RDFNull, RDFStartupInfo> {
    /**
     * Start up well-known session ID
     */
    public static final SessionID WELL_KNOWN_SESSION_ID = SessionID.fromFlow("Startup");

    @Override
    public void execute(Context context, RDFNull request, ActionCallback<RDFStartupInfo> callback) {
        RDFStartupInfo info = new RDFStartupInfo();
        info.setBootTime((System.currentTimeMillis() - SystemClock.elapsedRealtime()) * 1000);
        info.setClientInfo(GetClientInfo.getClientInfo(context));
        callback.onResponse(info);
        callback.onComplete();
    }
}
