package org.nexus_lab.relf.client.actions.android;

import static org.nexus_lab.relf.utils.ReflectionUtils.Parameter.from;
import static org.nexus_lab.relf.utils.ReflectionUtils.callInstanceMethod;
import static org.nexus_lab.relf.utils.ReflectionUtils.getFieldValue;

import android.content.Context;
import android.os.Build;
import android.os.UserHandle;
import android.os.UserManager;
import android.util.Log;
import android.util.Pair;

import org.nexus_lab.relf.client.actions.Action;
import org.nexus_lab.relf.client.actions.ActionCallback;
import org.nexus_lab.relf.lib.rdfvalues.RDFNull;
import org.nexus_lab.relf.lib.rdfvalues.android.RDFAndroidUserProfile;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * Requires system permission
 * android.Manifest.permission.MANAGE_USERS
 * android.Manifest.permission.INTERACT_ACROSS_USERS
 *
 * @author Ruipeng Zhang
 */
public class GetAndroidUserProfiles implements Action<RDFNull, RDFAndroidUserProfile> {
    private static final String TAG = GetAndroidUserProfiles.class.getSimpleName();

    @SuppressWarnings("unchecked")
    private static final Pair<String, String>[] USER_INFO_FIELD_MAP = new Pair[]{
            new Pair("id", "id"),
            new Pair("serialNumber", "serialNumber"),
            new Pair("name", "name"),
            new Pair("creationTime", "creationTimeFromEpoch"),
            new Pair("lastLoggedInTime", "lastLoggedInTimeFromEpoch"),
            new Pair("lastLoggedInFingerprint", "lastLoggedInFingerprint"),
            new Pair("partial", "isPartial"),
            new Pair("guestToRemove", "isGuestToRemove")
    };

    @SuppressWarnings("unchecked")
    private static final Pair<String, String>[] USER_INFO_METHOD_MAP = new Pair[]{
            new Pair("isPrimary", "isPrimary"),
            new Pair("isAdmin", "isAdmin"),
            new Pair("isGuest", "isGuest"),
            new Pair("isRestricted", "isRestricted"),
            new Pair("isManagedProfile", "isManagedProfile"),
            new Pair("isEnabled", "isEnabled"),
            new Pair("isQuietModeEnabled", "isQuietModeEnabled"),
            new Pair("isEphemeral", "isEphemeral"),
            new Pair("isInitialized", "isInitialized"),
            new Pair("isDemo", "isDemo"),
            new Pair("isSystemOnly", "isSystemOnly"),
            new Pair("supportsSwitchTo", "supportsSwitchTo"),
            new Pair("supportsSwitchToByUser", "supportsSwitchToByUser")
    };

    private static String getSetterName(String fieldName) {
        return "set" + StringUtils.capitalize(fieldName);
    }

    private static RDFAndroidUserProfile getUser(UserManager manager, Object user) {
        RDFAndroidUserProfile userInfo = new RDFAndroidUserProfile();
        for (Pair<String, String> map : USER_INFO_FIELD_MAP) {
            try {
                Object value = getFieldValue(user, map.first);
                if (value != null) {
                    callInstanceMethod(userInfo, getSetterName(map.second),
                            from(value.getClass(), value));
                }
            } catch (ReflectiveOperationException e) {
                Log.w(TAG, e);
            }
        }
        for (Pair<String, String> map : USER_INFO_METHOD_MAP) {
            try {
                Object value = callInstanceMethod(user, map.first);
                if (value != null) {
                    callInstanceMethod(userInfo, getSetterName(map.second),
                            from(value.getClass(), value));
                }
            } catch (ReflectiveOperationException e) {
                Log.d(TAG, "Cannot call UserInfo#" + map.first);
            }
        }
        try {
            int userId = getFieldValue(user, "id");
            userInfo.setIsSystem(userId == 0);
        } catch (ReflectiveOperationException e) {
            Log.w(TAG, e);
        }
        try {
            UserHandle userHandle = callInstanceMethod(user, "getUserHandle");
            userInfo.setIsUserRunning(manager.isUserRunning(userHandle));
            userInfo.setIsUserStopping(
                    !manager.isUserRunning(userHandle) && manager.isUserRunningOrStopping(
                            userHandle));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                userInfo.setIsUserUnlocked(manager.isUserUnlocked(userHandle));
            }
        } catch (ReflectiveOperationException e) {
            Log.w(TAG, e);
        }
        return userInfo;
    }

    @Override
    public void execute(Context context, RDFNull request, ActionCallback<RDFAndroidUserProfile> callback) {
        UserManager manager = (UserManager) context.getSystemService(Context.USER_SERVICE);
        try {
            List<Object> users = callInstanceMethod(manager, "getUsers");
            for (Object user : users) {
                callback.onResponse(getUser(manager, user));
            }
            callback.onComplete();
        } catch (ReflectiveOperationException e) {
            callback.onError(new Exception("Cannot get users"));
        }
    }
}
