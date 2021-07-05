package org.nexus_lab.relf.lib.config;

import android.content.Context;
import android.util.Log;

import org.nexus_lab.relf.utils.PathUtils;

import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Reading configuration from Android APK assets
 *
 * @author Ruipeng Zhang
 */
@SuppressWarnings("unchecked")
public class AssetsConfig implements ConfigContract {
    private static final String INCLUDE_FIELD = "Config.includes";

    private final Context context;
    private Map<String, Object> config = new HashMap<>();

    /**
     * @param context application context
     * @param path    assets file path which relative to the assets root folder
     */
    public AssetsConfig(Context context, String path) {
        this.context = context;
        loadConfig(path);
        if (config.get(INCLUDE_FIELD) != null) {
            for (String includePath : (ArrayList<String>) config.get(INCLUDE_FIELD)) {
                String includeConfig = PathUtils.normalize(includePath);
                loadConfig(includeConfig);
            }
        }
    }

    private void loadConfig(String path) {
        Yaml yaml = new Yaml();
        try (InputStream stream = context.getAssets().open(path)) {
            Map<String, Object> partialConfig = yaml.load(stream);
            if (partialConfig != null) {
                config.putAll(partialConfig);
            }
            ConfigInterpolator.interpolate(config);
        } catch (IOException e) {
            Log.w(AssetsConfig.class.getName(),
                    String.format("Failed to load config from file %s, reason: %s",
                            path, e.getMessage()));
        }
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
    }

    @Override
    public void write() {
        throw new RuntimeException("write() is not supported by AssetsConfig");
    }

    @Override
    public boolean isEmpty() {
        return config.isEmpty();
    }
}
