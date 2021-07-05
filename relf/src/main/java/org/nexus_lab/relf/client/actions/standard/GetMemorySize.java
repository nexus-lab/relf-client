package org.nexus_lab.relf.client.actions.standard;

import android.content.Context;

import org.nexus_lab.relf.client.actions.Action;
import org.nexus_lab.relf.client.actions.ActionCallback;
import org.nexus_lab.relf.lib.rdfvalues.ByteSize;
import org.nexus_lab.relf.lib.rdfvalues.RDFNull;
import org.nexus_lab.relf.utils.os.Memory;

/**
 * @author Ruipeng Zhang
 */
public class GetMemorySize implements Action<RDFNull, ByteSize> {
    @Override
    public void execute(Context context, RDFNull request, ActionCallback<ByteSize> callback) {
        callback.onResponse(new ByteSize(new Memory().memTotal()));
        callback.onComplete();
    }
}
