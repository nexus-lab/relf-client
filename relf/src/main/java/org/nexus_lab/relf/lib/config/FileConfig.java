package org.nexus_lab.relf.lib.config;

import android.util.Log;

import org.nexus_lab.relf.utils.PathUtils;

import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Local file configuration implementation
 *
 * @author Ruipeng Zhang
 */
@SuppressWarnings("unchecked")
public class FileConfig implements ConfigContract {
    private static final String TAG = FileConfig.class.getSimpleName();

    private static final String INCLUDE_FIELD = "Config.includes";

    private String writebackPath;
    private Map<String, Object> config = new HashMap<>();
    private Map<String, Object> changes = new HashMap<>();

    /**
     * @param path configuration file absolute path
     */
    public FileConfig(String path) {
        loadConfig(path);
        if (config.get(INCLUDE_FIELD) != null) {
            for (String includePath : (ArrayList<String>) config.get(INCLUDE_FIELD)) {
                String includeConfig = PathUtils.join(PathUtils.getPath(path), includePath);
                loadConfig(includeConfig);
            }
        }
        writebackPath = (String) config.get("Config.writeback");
        writebackPath = PathUtils.join(PathUtils.getPath(path), writebackPath);
        loadConfig(writebackPath);
    }

    private void loadConfig(String path) {
        Yaml yaml = new Yaml();
        File file = new File(path);
        if (file.exists()) {
            try (InputStream stream = new FileInputStream(file)) {
                Map<String, Object> partialConfig = yaml.load(stream);
                if (partialConfig != null) {
                    config.putAll(partialConfig);
                }
                ConfigInterpolator.interpolate(config);
            } catch (IOException e) {
                Log.e(TAG, "Failed to load config from file " + path + ".", e);
            }
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
        changes.put(field, value);
    }

    @Override
    public void write() {
        Yaml yaml = new Yaml();
        Map<String, Object> writeback = null;
        try {
            if (new File(writebackPath).exists()) {
                FileInputStream input = new FileInputStream(writebackPath);
                writeback = yaml.load(input);
            }
            if (writeback == null) {
                writeback = new HashMap<>(64);
            }
            writeback.putAll(changes);
            FileWriter output = new FileWriter(writebackPath);
            yaml.dump(writeback, output);
            changes.clear();
        } catch (IOException e) {
            Log.e(TAG, "Failed to save config changes to file " + writebackPath + ".", e);
        }
    }

    @Override
    public boolean isEmpty() {
        return config.isEmpty();
    }
}
