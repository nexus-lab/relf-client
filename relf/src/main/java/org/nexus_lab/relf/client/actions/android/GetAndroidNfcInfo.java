package org.nexus_lab.relf.client.actions.android;

import static android.Manifest.permission.NFC;

import android.content.Context;
import android.nfc.NfcAdapter;

import org.nexus_lab.relf.client.actions.Action;
import org.nexus_lab.relf.client.actions.ActionCallback;
import org.nexus_lab.relf.client.actions.UsesPermission;
import org.nexus_lab.relf.client.exceptions.NoSuchDeviceException;
import org.nexus_lab.relf.lib.rdfvalues.RDFNull;
import org.nexus_lab.relf.lib.rdfvalues.android.RDFAndroidNfcInfo;

/**
 * @author Ruipeng Zhang
 */
@UsesPermission(NFC)
public class GetAndroidNfcInfo implements Action<RDFNull, RDFAndroidNfcInfo> {
    @Override
    public void execute(Context context, RDFNull request, ActionCallback<RDFAndroidNfcInfo> callback) {
        NfcAdapter adapter = NfcAdapter.getDefaultAdapter(context);
        if (adapter == null) {
            callback.onError(new NoSuchDeviceException("NFC device is not found."));
        } else {
            RDFAndroidNfcInfo info = new RDFAndroidNfcInfo();
            info.setIsEnabled(adapter.isEnabled());
            info.setIsNdefPushEnabled(adapter.isNdefPushEnabled());
            callback.onResponse(info);
            callback.onComplete();
        }
    }
}
