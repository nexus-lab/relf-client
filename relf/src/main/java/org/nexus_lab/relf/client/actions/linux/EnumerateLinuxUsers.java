package org.nexus_lab.relf.client.actions.linux;

import android.content.Context;

import org.nexus_lab.relf.client.actions.Action;
import org.nexus_lab.relf.client.actions.ActionCallback;
import org.nexus_lab.relf.lib.rdfvalues.RDFNull;
import org.nexus_lab.relf.lib.rdfvalues.client.RDFUser;
import org.nexus_lab.relf.service.RelfService;
import org.nexus_lab.relf.service.struct.StructPasswd;

import java.io.IOException;

/**
 * @author Ruipeng Zhang
 */
public class EnumerateLinuxUsers implements Action<RDFNull, RDFUser> {
    @Override
    public void execute(Context context, RDFNull request, ActionCallback<RDFUser> callback) {
        try {
            StructPasswd[] users = RelfService.getpwent();
            if (users != null) {
                for (StructPasswd struct : users) {
                    RDFUser user = new RDFUser();
                    user.setUsername(struct.pw_name);
                    user.setFullName(struct.pw_gecos);
                    user.setUid(struct.pw_uid);
                    user.setGid(struct.pw_gid);
                    user.setShell(struct.pw_shell);
                    user.setHomedir(struct.pw_dir);
                    callback.onResponse(user);
                }
            }
            callback.onComplete();
        } catch (IOException e) {
            callback.onError(e);
        }
    }
}
