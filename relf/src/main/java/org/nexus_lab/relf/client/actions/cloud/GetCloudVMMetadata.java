package org.nexus_lab.relf.client.actions.cloud;

import android.content.Context;

import org.nexus_lab.relf.client.actions.Action;
import org.nexus_lab.relf.client.actions.ActionCallback;
import org.nexus_lab.relf.lib.rdfvalues.RDFNull;
import org.nexus_lab.relf.lib.rdfvalues.cloud.RDFCloudMetadataRequests;

/**
 * @author Ruipeng Zhang
 */
public class GetCloudVMMetadata implements Action<RDFCloudMetadataRequests, RDFNull> {
    @Override
    public void execute(Context context, RDFCloudMetadataRequests request,
            ActionCallback<RDFNull> callback) {
        // Not a cloud service, directly return status
        callback.onComplete();
    }
}
