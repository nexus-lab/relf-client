package org.nexus_lab.relf.client.actions.linux;

import android.content.Context;
import android.system.StructStat;

import org.nexus_lab.relf.client.actions.Action;
import org.nexus_lab.relf.client.actions.ActionCallback;
import org.nexus_lab.relf.lib.rdfvalues.RDFNull;
import org.nexus_lab.relf.lib.rdfvalues.protodict.RDFDataBlob;
import org.nexus_lab.relf.service.RelfService;

import java.io.IOException;

/**
 * @author Ruipeng Zhang
 */
public class GetInstallDate implements Action<RDFNull, RDFDataBlob> {
    @Override
    public void execute(Context context, RDFNull request, ActionCallback<RDFDataBlob> callback) {
        try {
            RDFDataBlob seconds = new RDFDataBlob();
            // TODO: no such file on Android L
            StructStat stat = RelfService.stat("/lost+found");
            seconds.setInteger(stat.st_ctime);
            callback.onResponse(seconds);
            callback.onComplete();
        } catch (IOException e) {
            callback.onError(e);
        }
    }
}
