package org.nexus_lab.relf.lib.config;

import android.content.Context;
import android.content.SharedPreferences;

import org.nexus_lab.relf.R;

import org.yaml.snakeyaml.Yaml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Android {@link SharedPreferences} configuration implementation
 *
 * @author Ruipeng Zhang
 */
@SuppressWarnings("unchecked")
public class SharedPreferenceConfig implements ConfigContract {
    private final String writeback;
    private final Context context;
    private final Map<String, Object> config = new HashMap<>();
    private final Map<String, Object> changes = new HashMap<>();

    /**
     * @param context application context
     */
    public SharedPreferenceConfig(Context context) {
        this.context = context;
        loadConfig(context.getString(R.string.config_file_main));

        String includesKey = context.getString(R.string.config_file_includes_key);
        if (config.get(includesKey) != null) {
            for (String include : (ArrayList<String>) config.get(includesKey)) {
                loadConfig(include);
            }
        }
        writeback = context.getString(R.string.config_file_writeback);
        loadConfig(writeback);
    }

    /**
     * Read configurations to a {@link SharedPreferences} file
     *
     * @param source source {@link SharedPreferences}
     * @param config configuration object
     */
    private static void read(SharedPreferences source, Map<String, Object> config) {
        Yaml yaml = new Yaml();
        for (Map.Entry<String, ?> entry : source.getAll().entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (key.startsWith("[yaml]")) {
                key = key.substring(6);
                value = yaml.load(value.toString());
            }
            config.put(key, value);
        }
        ConfigInterpolator.interpolate(config);
    }

    private void loadConfig(String filename) {
        SharedPreferences preferences = context.getSharedPreferences(filename,
                Context.MODE_PRIVATE);
        read(preferences, config);
    }

    @Override
    public <T> T get(String field) {
        return (T) config.get(field);
    }

    @Override
    public List<String> list() {
        return new ArrayList(config.keySet());
    }

    @Override
    public void set(String field, Object value) {
        config.put(field, value);
        changes.put(field, value);
    }

    /**
     * Write configurations to a given shared preference file
     *
     * @param filename shared preference file name
     */
    public void write(String filename) {
        Yaml yaml = new Yaml();
        SharedPreferences preferences = context.getSharedPreferences(filename,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        for (Map.Entry<String, Object> entry : changes.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value instanceof List || value instanceof Map) {
                editor.putString("[yaml]" + key, yaml.dump(value));
            } else if (value instanceof Boolean) {
                editor.putBoolean(key, (Boolean) value);
            } else if (value instanceof Integer) {
                editor.putInt(key, (Integer) value);
            } else if (value instanceof Long) {
                editor.putLong(key, (Long) value);
            } else if (value instanceof Float) {
                editor.putFloat(key, (Float) value);
            } else if (value instanceof Double) {
                editor.putFloat(key, ((Double) value).floatValue());
            } else {
                editor.putString(key, value.toString());
            }
        }
        editor.commit();
        changes.clear();
    }

    @Override
    public void write() {
        write(writeback);
    }

    @Override
    public boolean isEmpty() {
        return config.isEmpty();
    }
}
