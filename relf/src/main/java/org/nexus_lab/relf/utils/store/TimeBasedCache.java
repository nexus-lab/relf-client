package org.nexus_lab.relf.utils.store;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * A Cache which expires based on time
 *
 * @param <K> key type
 * @param <V> value type
 * @author Ruipeng Zhang
 */
public class TimeBasedCache<K, V> extends FastStore<K, V> {
    private static final Set<TimeBasedCache> CACHES =
            Collections.newSetFromMap(new WeakHashMap<TimeBasedCache, Boolean>());

    static {
        new Thread(new CleaningThread()).start();
    }

    private final int maxAge;
    private Map<K, Long> ages;

    public TimeBasedCache() {
        this(10);
    }

    /**
     * @param maxSize maximum number of objects in this cache
     */
    public TimeBasedCache(int maxSize) {
        this(maxSize, 600 * 1000);
    }

    /**
     * @param maxSize maximum number of objects in this cache
     * @param maxAge  maximum time for an object to be alive in milliseconds
     */
    public TimeBasedCache(int maxSize, int maxAge) {
        super(maxSize);
        this.maxAge = maxAge;
        this.ages = new HashMap<>();
        CACHES.add(this);
    }

    @Override
    public synchronized V get(K key) {
        Long age = ages.get(key);
        long now = System.currentTimeMillis();
        if (age == null || age + maxAge < now) {
            return null;
        }
        ages.put(key, now);
        return super.get(key);
    }

    @Override
    public synchronized K put(K key, V value) {
        ages.put(key, System.currentTimeMillis());
        return super.put(key, value);
    }

    @Override
    public synchronized V remove(K key) {
        ages.remove(key);
        return super.remove(key);
    }

    private static class CleaningThread implements Runnable {
        private boolean sleep() {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                return false;
            }
            return true;
        }

        @SuppressWarnings("unchecked")
        private void cleanCache(TimeBasedCache cache) {
            cache.lock();
            try {
                List keys = new ArrayList<>(cache.keys());
                for (Object key : keys) {
                    Long age = (Long) cache.ages.get(key);
                    if (age == null || age + cache.maxAge < System.currentTimeMillis()) {
                        Object value = cache.remove(key);
                        cache.killObject(value);
                    }
                }
            } finally {
                cache.unlock();
            }
        }

        @Override
        public void run() {
            while (true) {
                for (TimeBasedCache cache : CACHES) {
                    cleanCache(cache);
                }
                if (!sleep()) {
                    break;
                }
            }
        }
    }
}
