package org.nexus_lab.relf.utils.store;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;

public class TimeBasedCacheTest {
    private TimeBasedCache<Integer, Integer> cache;

    @Before
    public void setup() {
        cache = new TimeBasedCache<>(10, 500);
        cache.put(1, 1);
    }

    @Test
    public void get() throws Exception {
        Thread.sleep(250);
        assertEquals((Object) 1, cache.get(1));
        Thread.sleep(510);
        assertNull(cache.get(1));
    }

    @Test
    public void put() {
        cache.put(2, 2);
        assertEquals((Object) 2, cache.get(2));
    }
}