package org.nexus_lab.relf.lib.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.test.core.app.ApplicationProvider;

import org.nexus_lab.relf.R;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * @author Ruipeng Zhang
 */
@RunWith(RobolectricTestRunner.class)
public class SharedPreferenceConfigTest {
    private static SharedPreferenceConfig config;
    private static Context context;

    @Before
    public void setup() {
        // remove previous configurations
        context = ApplicationProvider.getApplicationContext();
        SharedPreferences preferences = context.getSharedPreferences(
                context.getString(R.string.config_file_main), Context.MODE_PRIVATE);
        preferences.edit().clear().commit();
        SharedPreferences writeback = context.getSharedPreferences(
                context.getString(R.string.config_file_writeback), Context.MODE_PRIVATE);
        writeback.edit().clear().commit();

        config = new SharedPreferenceConfig(context);
        assertTrue(config.isEmpty());
        config.merge(new AssetsConfig(context, "config.yaml"));
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
        int testData = new Random().nextInt();
        config.set("Test.write_test", testData);
        config.write();

        SharedPreferences writeback = context.getSharedPreferences(
                context.getString(R.string.config_file_writeback), Context.MODE_PRIVATE);
        assertEquals(testData, writeback.getInt("Test.write_test",
                (testData == Integer.MAX_VALUE || testData == Integer.MIN_VALUE) ? 0
                        : testData + 1));
    }

    @Test
    public void isEmpty() throws Exception {
        assertFalse(config.isEmpty());
    }
}
