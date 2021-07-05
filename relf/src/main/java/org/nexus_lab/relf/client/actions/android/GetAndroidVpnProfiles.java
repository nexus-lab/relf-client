package org.nexus_lab.relf.client.actions.android;

import static org.nexus_lab.relf.utils.ReflectionUtils.Parameter.from;
import static org.nexus_lab.relf.utils.ReflectionUtils.callInstanceMethod;
import static org.nexus_lab.relf.utils.ReflectionUtils.callStaticMethod;
import static org.nexus_lab.relf.utils.ReflectionUtils.getFieldValue;
import static org.nexus_lab.relf.utils.ReflectionUtils.getStaticFieldValue;
import static org.nexus_lab.relf.utils.ReflectionUtils.setFieldValue;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import org.nexus_lab.relf.client.actions.Action;
import org.nexus_lab.relf.client.actions.ActionCallback;
import org.nexus_lab.relf.lib.rdfvalues.RDFNull;
import org.nexus_lab.relf.lib.rdfvalues.android.RDFAndroidVpnProfile;
import org.nexus_lab.relf.proto.AndroidVpnProfile;

/**
 * @author Ruipeng Zhang
 * NOTICE: No test for this action available
 */
public class GetAndroidVpnProfiles implements Action<RDFNull, RDFAndroidVpnProfile> {
    private static final String TAG = GetAndroidVpnProfiles.class.getSimpleName();

    private void setField(Object from, String fromField, RDFAndroidVpnProfile to, String toField) {
        try {
            Object value = getFieldValue(from, fromField);
            setFieldValue(to, toField, value);
        } catch (ReflectiveOperationException e) {
            Log.w(TAG, e);
        }
    }

    @Override
    public void execute(Context context, RDFNull request, ActionCallback<RDFAndroidVpnProfile> callback) {
        try {
            Class<?> keyStoreClass = Class.forName("android.security.KeyStore");
            Class<?> credentialsClass = Class.forName("android.security.Credentials");
            Class<?> profileClass = Class.forName("com.android.internal.net.VpnProfile");
            Object keyStore = callStaticMethod(keyStoreClass, "getInstance");
            Object prefix = getStaticFieldValue(credentialsClass, "VPN");
            String[] keys;
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1) {
                keys = callInstanceMethod(keyStore, "saw", from(String.class, prefix));
            } else {
                keys = callInstanceMethod(keyStore, "list", from(String.class, prefix));
            }
            for (String key : keys) {
                byte[] encoded = callInstanceMethod(keyStore, "get",
                        from(String.class, prefix + key));
                Object profile = callStaticMethod(profileClass, "decode", from(String.class, key),
                        from(byte[].class, encoded));
                RDFAndroidVpnProfile info = new RDFAndroidVpnProfile();
                info.setType(AndroidVpnProfile.Type.forNumber(getFieldValue(profile, "type")));
                setField(profile, "name", info, "name");
                setField(profile, "server", info, "server");
                setField(profile, "username", info, "username");
                setField(profile, "password", info, "password");
                setField(profile, "dnsServers", info, "dnsServers");
                setField(profile, "searchDomains", info, "searchDomains");
                setField(profile, "routes", info, "routes");
                setField(profile, "mppe", info, "isMppe");
                setField(profile, "l2tpSecret", info, "l2tpSecret");
                setField(profile, "ipsecIdentifier", info, "ipsecIdentifier");
                setField(profile, "ipsecSecret", info, "ipsecSecret");
                setField(profile, "ipsecUserCert", info, "ipsecUserCert");
                setField(profile, "ipsecCaCert", info, "ipsecCaCert");
                setField(profile, "ipsecServerCert", info, "ipsecServerCert");
                callback.onResponse(info);
            }
            callback.onComplete();
        } catch (ReflectiveOperationException e) {
            callback.onError(e);
        }
    }
}
