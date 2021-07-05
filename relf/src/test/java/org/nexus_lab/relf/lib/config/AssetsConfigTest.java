package org.nexus_lab.relf.lib.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.List;
import java.util.Map;

/**
 * @author Ruipeng Zhang
 */
@RunWith(RobolectricTestRunner.class)
public class AssetsConfigTest {
    private static AssetsConfig config;

    @Before
    public void setup() {
        config = new AssetsConfig(ApplicationProvider.getApplicationContext(), "config.yaml");
    }

    @Test
    public void get() throws Exception {
        Map<String, Object> intpObject = config.get("Test.interpolate");
        List intpArray = (List) intpObject.get("interpolate.array");
        assertEquals("string:relf", intpObject.get("interpolate.string"));
        assertEquals("string:relf", intpArray.get(0));
        assertEquals("array:relf", ((List) intpArray.get(1)).get(0));
        assertEquals("array:relf", ((List) intpArray.get(1)).get(1));
    }

    @Test
    public void list() throws Exception {
        assertTrue(config.list().contains("Client.server_urls"));
        assertTrue(config.list().contains("CA.certificate"));
        assertTrue(config.list().contains("Client.name"));
        assertTrue(config.list().contains("Test.interpolate"));
    }

    @Test
    public void set() throws Exception {
        Object dummyObject = new Object();
        config.set("Test.dummy_object", dummyObject);
        assertEquals(dummyObject, config.get("Test.dummy_object"));
    }

    @Test
    public void write() throws Exception {
        try {
            config.write();
        } catch (RuntimeException e) {
            return;
        }
        throw new RuntimeException("write() method should throw a RuntimeException");
    }

    @Test
    public void isEmpty() throws Exception {
        assertFalse(config.isEmpty());
    }
}
