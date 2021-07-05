package org.nexus_lab.relf.lib.config;

import java.util.List;

/**
 * ReLF configuration interface
 *
 * @author Ruipeng Zhang
 */
public interface ConfigContract {
    /**
     * Get configuration value by name
     *
     * @param field configuration field name
     * @param <T>   configuration value type
     * @return configuration value
     */
    <T> T get(String field);

    /**
     * List top-level configurations field names
     *
     * @return list all of top-level configurations field names
     */
    List<String> list();

    /**
     * Set configuration value by name.
     * This also add the new configuration value to a changelist which can be write back to the new
     * configuration file
     *
     * @param field configuration field name
     * @param value desired configuration value
     */
    void set(String field, Object value);

    /**
     * Write configuration changes to config writeback file
     */
    void write();

    /**
     * Check if there is any configuration
     *
     * @return true if there is no configuration
     */
    boolean isEmpty();

    /**
     * merge configurations from another {@link ConfigContract} instance
     * <p>
     * NOTE: This will not override existing main configuration file. After you call {@link
     * #write()},
     * all changes will be saved to the writeback file.
     *
     * @param config {@link ConfigContract} instance to import from
     */
    default void merge(ConfigContract config) {
        for (String key : config.list()) {
            set(key, config.get(key));
        }
    }
}
