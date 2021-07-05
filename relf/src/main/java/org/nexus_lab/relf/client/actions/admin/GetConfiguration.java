package org.nexus_lab.relf.client.actions.admin;

import android.content.Context;

import org.nexus_lab.relf.client.actions.Action;
import org.nexus_lab.relf.client.actions.ActionCallback;
import org.nexus_lab.relf.lib.config.ConfigLib;
import org.nexus_lab.relf.lib.rdfvalues.RDFNull;
import org.nexus_lab.relf.lib.rdfvalues.protodict.RDFDict;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Ruipeng Zhang
 */
public class GetConfiguration implements Action<RDFNull, RDFDict> {
    private static final List<String> BLOCKED_PARAMETERS = new ArrayList<>();

    static {
        BLOCKED_PARAMETERS.add("Client.private_key");
    }

    @Override
    public void execute(Context context, RDFNull request, ActionCallback<RDFDict> callback) {
        RDFDict result = new RDFDict();
        for (String name : ConfigLib.list()) {
            Object value;
            if (BLOCKED_PARAMETERS.contains("Client.private_key")) {
                value = "[Redacted]";
            } else {
                value = ConfigLib.get(name);
            }
            if (value != null) {
                result.put(name, value);
            }
        }
        callback.onResponse(result);
        callback.onComplete();
    }
}
