package org.nexus_lab.relf.utils;

import android.content.Context;
import android.content.pm.PackageManager;

import androidx.core.content.ContextCompat;

import org.nexus_lab.relf.client.actions.UsesPermission;

import java.util.ArrayList;
import java.util.List;

/**
 * A utility class for checking action permissions
 *
 * @author Ruipeng Zhang
 */
public final class PermissionUtils {
    private PermissionUtils() {
    }

    /**
     * Check if current application has been granted the given permission
     *
     * @param context application context
     * @param permission permission name
     * @return true if permission is granted
     */
    public static boolean checkPermission(Context context, String permission) {
        int result = ContextCompat.checkSelfPermission(context, permission);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Get required permissions from class annotation
     *
     * @param clazz any class
     * @return a two dimensional array whose elements are strings. The first dimension represents
     * AND relation between the permissions, and the second dimension represents OR relation. For
     * example, { {A}, {B, C}, {D, E, F} } represents a logical relation of A && (B || C) && (D || E
     * || F)
     */
    public static String[][] getPermissions(Class clazz) {
        UsesPermission annotation = (UsesPermission) clazz.getAnnotation(UsesPermission.class);
        if (annotation == null) {
            return null;
        }

        int size = (!"".equals(annotation.value()) ? 1 : annotation.allOf().length) + 1;
        String[][] permissionGroups = new String[size][];
        if (!"".equals(annotation.value())) {
            permissionGroups[0] = new String[]{annotation.value()};
        } else {
            for (int i = 0; i < annotation.allOf().length; i++) {
                permissionGroups[i] = new String[]{annotation.allOf()[i]};
            }
        }
        permissionGroups[size - 1] = annotation.anyOf();

        return permissionGroups;
    }

    /**
     * Check if the current application satisfies permission requirements of a give class
     *
     * @param context application context
     * @param clazz any class
     * @return a two dimensional array of all the required permissions that are not granted, null if
     * all permissions are satisfied
     */
    public static String[][] checkPermissions(Context context, Class clazz) {
        String[][] permissionGroups = getPermissions(clazz);
        if (permissionGroups != null && permissionGroups.length > 0) {
            List<String[]> missing = new ArrayList<>();
            for (String[] permissions : permissionGroups) {
                if (permissions == null || permissions.length == 0) {
                    continue;
                }
                boolean passed = false;
                for (String permission : permissions) {
                    passed = passed || checkPermission(context, permission);
                }
                if (!passed) {
                    missing.add(permissions);
                }
            }
            if (missing.size() > 0) {
                return missing.toArray(new String[0][0]);
            }
        }
        return null;
    }
}
