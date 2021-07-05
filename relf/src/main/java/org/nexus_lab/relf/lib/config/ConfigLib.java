package org.nexus_lab.relf.lib.config;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

/**
 * Configuration helper class
 *
 * @author Ruipeng Zhang
 */
public final class ConfigLib {
    private static ConfigContract config;

    /**
     * Set the default config instance for the {@link ConfigLib}
     *
     * @param config {@link ConfigContract} instance
     */
    public static void use(ConfigContract config) {
        ConfigLib.config = config;
    }

    /**
     * Get a configuration value by field name
     *
     * @param field configuration field name
     * @param <T>   supposed configuration value type
     * @return configuration value
     */
    public static <T> T get(String field) {
        if (config != null) {
            return config.get(field);
        }
        return null;
    }

    /**
     * Get a configuration value by field name and indicate value type
     *
     * @param field configuration field name
     * @param type  configuration value type class
     * @param <T>   supposed configuration value type
     * @return configuration value
     */
    @SuppressWarnings("unchecked")
    public static <T> T get(String field, Class<?> type) {
        T result = get(field);
        if (result != null && type != null) {
            if (type.isInstance(result)) {
                return result;
            }
            if (type.equals(String.class)) {
                return (T) String.valueOf(result);
            } else {
                try {
                    Constructor<?> constructor = type.getConstructor(String.class);
                    if (constructor != null) {
                        return (T) constructor.newInstance(String.valueOf(result));
                    }
                } catch (ReflectiveOperationException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return result;
    }

    /**
     * Get a configuration value by field name and provide default value.
     * The default value type also indicates the configuration value type
     *
     * @param field        configuration field name
     * @param defaultValue configuration default value
     * @param <T>          supposed configuration value type
     * @return configuration value
     */
    public static <T> T get(String field, T defaultValue) {
        T result = defaultValue != null ? get(field, defaultValue.getClass()) : get(field);
        return result == null ? defaultValue : result;
    }

    /**
     * List all the top-level configuration field names
     *
     * @return top-level configuration field names
     */
    public static List<String> list() {
        return config == null ? new ArrayList() : config.list();
    }

    /**
     * Set a value to a configuration field
     *
     * @param field configuration field name
     * @param value new configuration value
     */
    public static void set(String field, Object value) {
        if (config != null) {
            config.set(field, value);
        }
    }

    /**
     * Write configurations back to the storage
     */
    public static void write() {
        if (config != null) {
            config.write();
        }
    }

    /**
     * Check if there is any configurations
     *
     * @return true if there is no configuration
     */
    public static boolean isEmpty() {
        return config == null || config.isEmpty();
    }
}
