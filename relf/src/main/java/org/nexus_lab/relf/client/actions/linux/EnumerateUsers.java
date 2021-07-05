package org.nexus_lab.relf.client.actions.linux;

import android.content.Context;

import org.nexus_lab.relf.client.actions.Action;
import org.nexus_lab.relf.client.actions.ActionCallback;
import org.nexus_lab.relf.client.actions.android.GetAndroidUserProfiles;
import org.nexus_lab.relf.lib.rdfvalues.RDFNull;
import org.nexus_lab.relf.lib.rdfvalues.android.RDFAndroidUserProfile;
import org.nexus_lab.relf.lib.rdfvalues.client.RDFUser;

import java.util.Locale;

/**
 * Unlike {@link EnumerateLinuxUsers} on other platforms, this one will list all Android users
 * on a multi-user device.
 *
 * @author Ruipeng Zhang
 */
public class EnumerateUsers implements Action<RDFNull, RDFUser> {
    @Override
    public void execute(Context context, RDFNull request, ActionCallback<RDFUser> callback) {
        new GetAndroidUserProfiles().execute(context, null, new ActionCallback<RDFAndroidUserProfile>() {
            @Override
            public void onResponse(RDFAndroidUserProfile response) {
                RDFUser user = new RDFUser();
                user.setUsername(response.getName());
                user.setUid(response.getId());
                user.setHomedir(String.format(Locale.ENGLISH, "/data/user/%d/", response.getId()));
                callback.onResponse(user);
            }

            @Override
            public void onComplete() {
                callback.onComplete();
            }

            @Override
            public void onError(Throwable e) {
                callback.onError(e);
            }
        });
    }
}
