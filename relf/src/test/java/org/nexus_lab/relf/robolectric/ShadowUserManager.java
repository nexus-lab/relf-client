package org.nexus_lab.relf.robolectric;


import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;
import static android.os.Build.VERSION_CODES.N;

import android.os.UserHandle;
import android.os.UserManager;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/**
 * Robolectric implementation of {@link android.os.UserManager}.
 */
@Implements(value = UserManager.class, minSdk = JELLY_BEAN_MR1)
public class ShadowUserManager extends org.robolectric.shadows.ShadowUserManager {
    /**
     * Return whether the given user is running in an "unlocked" state.
     *
     * @param user to retrieve the unlocked state for.
     * @return true if user is running in an "unlocked" state.
     */
    @Implementation(minSdk = N)
    public boolean isUserUnlocked(UserHandle user) {
        return false;
    }
}
