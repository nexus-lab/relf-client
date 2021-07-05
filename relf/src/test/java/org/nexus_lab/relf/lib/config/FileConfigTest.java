package org.nexus_lab.relf.lib.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.nexus_lab.relf.utils.PathUtils;

import org.junit.BeforeClass;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class FileConfigTest {
    private static FileConfig config;

    @BeforeClass
    public static void setup() {
        String entry = FileConfigTest.class.getClassLoader().getResource(
                "config/config.yaml").getFile();
        config = new FileConfig(entry);
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
    public void isEmpty() throws Exception {
        assertFalse(config.isEmpty());
    }

    @Test
    public void write() throws Exception {
        int testData = new Random().nextInt();
        config.set("Test.write_test", testData);
        config.write();

        String entry = getClass().getClassLoader().getResource("config/config.yaml").getFile();
        String writebackPath = config.get("Config.writeback");
        writebackPath = PathUtils.join(PathUtils.getPath(entry), writebackPath);
        BufferedReader bufferedReader = new BufferedReader(new FileReader(writebackPath));
        String line;
        StringBuilder sb = new StringBuilder();
        while ((line = bufferedReader.readLine()) != null) {
            sb.append(line);
        }
        bufferedReader.close();

        assertTrue(sb.toString().contains(String.format("Test.write_test: %d", testData)));
    }
}