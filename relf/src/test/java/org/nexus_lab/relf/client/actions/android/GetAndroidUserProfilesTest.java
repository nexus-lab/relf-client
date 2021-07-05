package org.nexus_lab.relf.client.actions.android;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.os.UserManager;

import androidx.test.core.app.ApplicationProvider;

import org.nexus_lab.relf.client.actions.ActionCallback;
import org.nexus_lab.relf.client.exceptions.TestSuccessException;
import org.nexus_lab.relf.lib.rdfvalues.android.RDFAndroidUserProfile;
import org.nexus_lab.relf.robolectric.ShadowUserManager;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;

/**
 * @author Ruipeng Zhang
 */
@RunWith(RobolectricTestRunner.class)
@Config(shadows = {ShadowUserManager.class})
public class GetAndroidUserProfilesTest {
    private static final int FLAG_PRIMARY = 0x00000001;
    private static final int FLAG_ADMIN = 0x00000002;
    private static final int FLAG_GUEST = 0x00000004;
    private static final int FLAG_RESTRICTED = 0x00000008;
    private static final int FLAG_DISABLED = 0x00000040;

    @Test(expected = TestSuccessException.class)
    public void execute() {
        Context context = ApplicationProvider.getApplicationContext();
        UserManager manager = (UserManager) context.getSystemService(Context.USER_SERVICE);
        ShadowUserManager shadowManager = (ShadowUserManager) Shadows.shadowOf(manager);
        shadowManager.setUserUnlocked(true);
        shadowManager.addUser(1, "abc", FLAG_ADMIN | FLAG_DISABLED);
        shadowManager.addUser(2, "def", FLAG_GUEST | FLAG_RESTRICTED);
        shadowManager.addUser(3, "ghi", FLAG_PRIMARY);
        new GetAndroidUserProfiles().execute(context, null, new ActionCallback<RDFAndroidUserProfile>() {
            private int i;

            @Override
            public void onResponse(RDFAndroidUserProfile user) {
                assertEquals(i, user.getId().intValue());
                switch (i) {
                    case 0:
                        assertEquals("system_user", user.getName());
                        assertTrue(user.getIsPrimary());
                        assertTrue(user.getIsAdmin());
                        break;
                    case 1:
                        assertEquals("abc", user.getName());
                        assertTrue(user.getIsAdmin());
                        assertFalse(user.getIsEnabled());
                        break;
                    case 2:
                        assertEquals("def", user.getName());
                        assertTrue(user.getIsGuest());
                        assertTrue(user.getIsRestricted());
                        break;
                    case 3:
                        assertEquals("ghi", user.getName());
                        assertTrue(user.getIsPrimary());
                        break;
                }
                i++;
            }

            @Override
            public void onComplete() {
                assertEquals(4, i);
                throw new TestSuccessException();
            }
        });
    }
}