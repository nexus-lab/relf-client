package org.nexus_lab.relf.utils.store;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Ruipeng Zhang
 */
public class FastStoreTest {
    private FastStore<Integer, Integer> cache;
    private List<Thread> executedThreads;

    @Before
    public void setup() {
        cache = new FastStore<>(3);
        cache.put(1, 1);
        cache.put(2, 2);
        cache.put(3, 3);
        cache.put(4, 4);
    }

    @Test
    public void expire() {
        assertNull(cache.get(1));
    }

    @Test
    public void put() {
        assertNotNull(cache.get(4));
    }

    @Test
    public void remove() {
        assertEquals((Object) 4, cache.remove(4));
        assertNotNull(cache.get(3));
        assertNotNull(cache.get(2));
    }

    @Test
    public void get() {
        assertEquals((Object) 2, cache.get(2));
        assertEquals((Object) 2, cache.keys().get(0));
    }

    @Test
    public void flush() {
        cache.flush();
        assertEquals(0, cache.size());
        assertNull(cache.get(2));
        assertNull(cache.get(3));
        assertNull(cache.get(4));
    }

    @Test
    public void keys() {
        assertArrayEquals(new Integer[]{2, 3, 4}, cache.keys().toArray(new Integer[0]));
    }

    @Test
    public void size() {
        assertEquals(3, cache.size());
    }

    @Test
    public void lock() throws Exception {
        executedThreads = new ArrayList<>();
        long now = System.currentTimeMillis();
        new Thread(new LockThread(now + 500)).start();
        Thread.sleep(10);
        new Thread(new LockThread(now + 1010)).start();
        Thread.sleep(1500);
        assertEquals(2, executedThreads.size());
    }

    @Test
    public void hashEqual() {
        FastStore<Integer, Integer> compare = new FastStore<>(3);
        compare.put(2, 2);
        compare.put(3, 3);
        compare.put(4, 4);
        assertEquals(cache, compare);
        assertEquals(cache.hashCode(), compare.hashCode());
        compare.put(1, 1);
        assertNotEquals(cache, compare);
        assertNotEquals(cache.hashCode(), compare.hashCode());
    }

    private final class LockThread implements Runnable {
        private long complete;

        LockThread(long complete) {
            this.complete = complete;
        }

        @Override
        public void run() {
            cache.lock();
            try {
                Thread.sleep(500);
            } catch (InterruptedException ignored) {
            }
            cache.unlock();
            long now = System.currentTimeMillis();
            long duration = Math.abs(now - complete);
            System.out.println(Thread.currentThread().getName() + " duration: " + duration);
            assertTrue(Thread.currentThread().getName() + " should finish before " + complete
                            + ", actual finished at " + now + ".", duration < 50);
            executedThreads.add(Thread.currentThread());
        }
    }
}