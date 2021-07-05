package org.nexus_lab.relf.lib.config;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Ruipeng Zhang
 */
public class ConfigInterpolatorTest {
    @Test
    public void interpolate() throws Exception {
        Map<String, Object> config = new HashMap<>();
        config.put("include_before_define2", "%(include_before_define)");
        config.put("include_before_define", "%(string_val)");
        config.put("int_val", 1);
        config.put("string_val", "int_val=%(int_val)");
        config.put("list_val",
                Arrays.asList("int_val=\"%(int_val)\"", "string_val=\"%(string_val)\""));

        Map<String, String> mapValue = new HashMap<>();
        mapValue.put("map_string_val", "int_val=%(int_val)");
        config.put("map_val", mapValue);

        config.put("cause_loop1", "%(cause_loop2)");
        config.put("cause_loop2", "%(cause_loop3)");
        config.put("cause_loop3", "%(cause_loop1)");

        ConfigInterpolator.interpolate(config);

        assertEquals("int_val=1", config.get("include_before_define"));
        assertEquals(config.get("include_before_define"),
                config.get("include_before_define2"));

        assertEquals(1, config.get("int_val"));
        assertEquals("int_val=1", config.get("string_val"));
        assertEquals(Arrays.asList("int_val=\"1\"", "string_val=\"int_val=1\"").toString(),
                config.get("list_val").toString());
        assertEquals("int_val=1", ((Map) config.get("map_val")).get("map_string_val"));

        assertEquals("%(cause_loop2)", config.get("cause_loop1"));
        assertEquals("%(cause_loop3)", config.get("cause_loop2"));
        assertEquals("%(cause_loop1)", config.get("cause_loop3"));
    }
}