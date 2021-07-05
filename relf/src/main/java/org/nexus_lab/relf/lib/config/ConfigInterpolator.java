package org.nexus_lab.relf.lib.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A util class for filling in placeholders in the configuration values
 *
 * @author Ruipeng Zhang
 */
@SuppressWarnings("unchecked")
public final class ConfigInterpolator {
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("%\\((.*?)\\)",
            Pattern.MULTILINE);

    private ConfigInterpolator() {
    }

    private static String interpolate(Map<String, Object> config, String value, int depth) {
        String result = value;
        Matcher m = PLACEHOLDER_PATTERN.matcher(result);
        while (m.find()) {
            String field = m.group(1);
            if (config.get(field) != null) {
                String reference = String.valueOf(config.get(field));
                // if the interpolated value contains another interpolation reference, continue
                // interpolating, unless it appears to be a dead loop
                if (depth < 20 && PLACEHOLDER_PATTERN.matcher(reference).find()) {
                    reference = interpolate(config, reference, depth + 1);
                }
                // only support top-level string-type config values
                result = result.replace("%(" + field + ")", reference);
            }
        }
        return result;
    }

    private static void interpolate(Map<String, Object> config, Map<String, Object> map) {
        for (Map.Entry<String, Object> keyValue : new HashMap<>(map).entrySet()) {
            String key = keyValue.getKey();
            Object value = keyValue.getValue();
            if (value instanceof String) {
                map.put(key, interpolate(config, (String) value, 0));
            } else if (value instanceof Map) {
                interpolate(config, (Map<String, Object>) value);
            } else if (value instanceof List) {
                interpolate(config, (List<Object>) value);
            }
        }
    }

    private static void interpolate(Map<String, Object> config, List<Object> list) {
        for (int i = 0; i < list.size(); i++) {
            Object value = list.get(i);
            if (value instanceof String) {
                list.set(i, interpolate(config, (String) value, 0));
            } else if (value instanceof Map) {
                interpolate(config, (Map<String, Object>) value);
            } else if (value instanceof List) {
                interpolate(config, (List<Object>) value);
            }
        }
    }

    /**
     * Replace placeholders with referred valued in the configuration values
     *
     * @param config a {@link Map} object represents the configurations that need to be interpolated
     */
    public static void interpolate(Map<String, Object> config) {
        interpolate(config, config);
    }
}
