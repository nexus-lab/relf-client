package org.nexus_lab.relf.lib;

import static org.junit.Assert.assertEquals;

import org.nexus_lab.relf.lib.config.ConfigLib;
import org.nexus_lab.relf.lib.config.FileConfig;

import org.junit.BeforeClass;
import org.junit.Test;

public class ConfigLibTest {
    public final static String FILE_CONFIG_ENTRY = ConfigLib.class.getClassLoader().getResource(
            "config/config.yaml").getFile();

    @BeforeClass
    public static void setup() {
        ConfigLib.use(new FileConfig(FILE_CONFIG_ENTRY));
    }

    @Test
    public void get() throws Exception {
        Integer versionMajor = ConfigLib.get("Template.version_major", 0);
        String foremanFreq = ConfigLib.get("Client.foreman_check_frequency", "");
        String domain = ConfigLib.get("Logging.domain", null);

        assertEquals(3, versionMajor.intValue());
        assertEquals("1800", foremanFreq);
        assertEquals("localhost", domain);
    }
}