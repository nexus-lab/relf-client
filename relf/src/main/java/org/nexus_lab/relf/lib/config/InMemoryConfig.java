package org.nexus_lab.relf.lib.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Pure in-memory configuration implementation
 *
 * @author Ruipeng Zhang
 */
@SuppressWarnings("unchecked")
public class InMemoryConfig implements ConfigContract {
    private final Map<String, Object> config = new HashMap<>();

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
        // not implemented
    }

    @Override
    public boolean isEmpty() {
        return config.isEmpty();
    }
}
