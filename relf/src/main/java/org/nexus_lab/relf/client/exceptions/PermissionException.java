package org.nexus_lab.relf.client.exceptions;

/**
 * @author Ruipeng Zhang
 */
public class PermissionException extends RuntimeException {
    public PermissionException(String message) {
        super(message);
    }

    public PermissionException(String[][] permissionGroups) {
        super("The following permissions are not granted by the system: " +
                formatMissingPermissions(permissionGroups));
    }

    /**
     * Transform permission array to plain text
     *
     * @param permissionGroups array of missing permissions
     * @return plain text. For example, a permission array of {{A}, {B, C}, {D, E, F}} will be
     * formatted to "A, (B, or C), and (D, E, or F)"
     */
    private static String formatMissingPermissions(String[][] permissionGroups) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < permissionGroups.length; i++) {
            String[] permissions = permissionGroups[i];
            if (i > 0 && i == permissionGroups.length - 1) {
                builder.append("and ");
            }
            if (permissions.length > 1) {
                builder.append("(");
            }
            for (int j = 0; j < permissions.length; j++) {
                if (j > 0 && j == permissions.length - 1) {
                    builder.append("or ");
                }
                builder.append(permissions[j]);
                if (j != permissions.length - 1) {
                    builder.append(", ");
                }
            }
            if (permissions.length > 1) {
                builder.append(")");
            }
            if (i != permissionGroups.length - 1) {
                builder.append(", ");
            }
        }
        return builder.toString();
    }
}
