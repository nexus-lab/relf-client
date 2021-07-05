package org.nexus_lab.relf.client.actions.android;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorDescription;
import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import org.nexus_lab.relf.client.exceptions.TestSuccessException;
import org.nexus_lab.relf.proto.AndroidAccount;
import org.nexus_lab.relf.proto.AndroidAccountAuthenticator;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Shadows;
import org.robolectric.shadows.ShadowAccountManager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * @author Ruipeng Zhang
 */
@RunWith(RobolectricTestRunner.class)
public class GetAndroidAccountsTest {

    @Test(expected = TestSuccessException.class)
    public void execute() {
        Context context = ApplicationProvider.getApplicationContext();
        AccountManager manager = AccountManager.get(context);
        ShadowAccountManager shadowManager = Shadows.shadowOf(manager);
        for (int i = 0; i < 3; i++) {
            String type = RandomStringUtils.randomAlphabetic(6);
            String accountName = RandomStringUtils.randomAlphabetic(6);
            String packageName = RandomStringUtils.randomAlphabetic(12);
            shadowManager.addAccount(new Account(accountName, type));
            AuthenticatorDescription authenticator = new AuthenticatorDescription(type, packageName,
                    0, 0, 0, 0);
            shadowManager.addAuthenticator(authenticator);
        }
        new GetAndroidAccounts().execute(context, null, response -> {
            Account[] accounts = manager.getAccounts();
            AuthenticatorDescription[] authenticators = manager.getAuthenticatorTypes();
            Assert.assertEquals(accounts.length, response.getAccounts().size());
            for (int i = 0; i < accounts.length; i++) {
                Account expected = accounts[i];
                AndroidAccount actual = response.getAccounts().get(i);
                assertEquals(expected.name, actual.getName());
                assertEquals(expected.type, actual.getType());
                if (manager.getPreviousName(expected) == null) {
                    assertFalse(actual.hasPreviousName());
                } else {
                    assertEquals(manager.getPreviousName(expected),
                            actual.getPreviousName());
                }
            }
            Assert.assertEquals(authenticators.length, response.getAuthenticators().size());
            for (int i = 0; i < authenticators.length; i++) {
                AuthenticatorDescription expected = authenticators[i];
                AndroidAccountAuthenticator actual = response.getAuthenticators().get(i);
                assertEquals(expected.type, actual.getType());
                assertEquals(expected.packageName, actual.getPackageName());
            }
            throw new TestSuccessException();
        });
    }
}