package org.nexus_lab.relf.client.actions.android;

import static android.Manifest.permission.GET_ACCOUNTS;
import static android.Manifest.permission.GET_ACCOUNTS_PRIVILEGED;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorDescription;
import android.content.Context;

import org.nexus_lab.relf.client.actions.Action;
import org.nexus_lab.relf.client.actions.ActionCallback;
import org.nexus_lab.relf.client.actions.UsesPermission;
import org.nexus_lab.relf.lib.rdfvalues.RDFNull;
import org.nexus_lab.relf.lib.rdfvalues.android.RDFAndroidAccountInfo;
import org.nexus_lab.relf.proto.AndroidAccount;
import org.nexus_lab.relf.proto.AndroidAccountAuthenticator;

/**
 * @author Ruipeng Zhang
 */
@UsesPermission(anyOf = {GET_ACCOUNTS, GET_ACCOUNTS_PRIVILEGED})
public class GetAndroidAccounts implements Action<RDFNull, RDFAndroidAccountInfo> {
    @Override
    public void execute(Context context, RDFNull request, ActionCallback<RDFAndroidAccountInfo> callback) {
        RDFAndroidAccountInfo info = new RDFAndroidAccountInfo();
        AccountManager manager = AccountManager.get(context);
        AuthenticatorDescription[] authenticators = manager.getAuthenticatorTypes();
        for (AuthenticatorDescription authenticator : authenticators) {
            AndroidAccountAuthenticator.Builder builder = AndroidAccountAuthenticator.newBuilder()
                    .setType(authenticator.type)
                    .setPackageName(authenticator.packageName);
            info.addAuthenticator(builder.build());
        }
        Account[] accounts = manager.getAccounts();
        for (Account account : accounts) {
            String previousName = manager.getPreviousName(account);
            AndroidAccount.Builder builder = AndroidAccount.newBuilder()
                    .setName(account.name)
                    .setType(account.type);
            if (previousName != null) {
                builder.setPreviousName(previousName);
            }
            info.addAccount(builder.build());
        }
        callback.onResponse(info);
        callback.onComplete();
    }
}
