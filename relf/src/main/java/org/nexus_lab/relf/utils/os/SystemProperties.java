package org.nexus_lab.relf.utils.os;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * @author Ruipeng Zhang
 */
public final class SystemProperties {
    /**
     * Operating system name. Here will always be "Android"
     */
    public static final String OS_NAME = "Android";
    /**
     * Operating system version.
     */
    public static final String OS_VERSION;
    /**
     * Operating system release name.
     */
    public static final String OS_RELEASE = Build.VERSION.RELEASE;
    /**
     * Operating system Linux kernel version.
     */
    public static final String OS_KERNEL = System.getProperty("os.version");
    /**
     * Operating system architecture.
     */
    public static final String OS_ARCH = System.getProperty("os.arch");

    static {
        String versionName = "unknown";
        Field[] fields = Build.VERSION_CODES.class.getFields();
        for (Field field : fields) {
            String fieldName = field.getName();
            try {
                if (!field.getType().equals(int.class)) {
                    continue;
                }
                int fieldValue = field.getInt(new Object());
                if (fieldValue == Build.VERSION.SDK_INT) {
                    versionName = WordUtils.capitalize(fieldName, '_');
                }
            } catch (IllegalArgumentException | NullPointerException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        OS_VERSION = StringUtils.join(versionName.split("_"), " ");
    }

    private SystemProperties() {
    }

    /**
     * Get the system property value for the given key from {@link android.os.SystemProperties}.
     *
     * @param context application context
     * @param key     property name
     * @return property value, null if failed to invoke API
     */
    @SuppressLint("PrivateApi")
    @SuppressWarnings({"unchecked", "JavadocReference"})
    private static Object get(Context context, String key) {
        try {
            Class systemProperties = context.getClassLoader().loadClass(
                    "android.os.SystemProperties");
            Method method = systemProperties.getMethod("get", String.class);
            return method.invoke(systemProperties, key);
        } catch (ReflectiveOperationException e) {
            return null;
        }
    }

    /**
     * Get the system property value for the given key from {@link android.os.SystemProperties}.
     *
     * @param <T>          system property type
     * @param context      application context
     * @param key          property name
     * @param defaultValue default property value if key is not found
     * @return property value
     */
    @SuppressWarnings({"unchecked", "JavadocReference"})
    public static <T> T get(Context context, String key, T defaultValue) {
        Object value = get(context, key);
        if (value == null) {
            return defaultValue;
        }
        return (T) value;
    }
}