package org.nexus_lab.relf.utils.store;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import lombok.EqualsAndHashCode;

/**
 * This is a LRU(Least Recent Used) cache which expires objects in oldest first manner.
 *
 * @param <K> type of key
 * @param <V> type of value
 * @author Ruipeng Zhang
 */
@EqualsAndHashCode(exclude = "lock")
public class FastStore<K, V> {
    private final Lock lock;

    private int limit;
    private Map<K, V> hash;
    private LinkedList<K> keys;

    /**
     * @param maxSize maximum cache size
     */
    public FastStore(int maxSize) {
        this.limit = maxSize;
        this.hash = new HashMap<>();
        this.keys = new LinkedList<>();
        this.lock = new ReentrantLock();
    }

    /**
     * Perform cleanup on objects when they expire.
     *
     * @param object The expired object which was stored in the cache
     */
    public synchronized void killObject(V object) {
        // To be overridden if needed
    }

    /**
     * Remove and cleanup least recent used objects when cache exceeds its max size
     */
    public synchronized void expire() {
        while (keys.size() > limit) {
            K key = keys.pop();
            V value = hash.remove(key);
            killObject(value);
        }
    }

    /**
     * Add the object to the cache
     *
     * @param key   the key to identify the object
     * @param value the object to be added
     * @return the same key that is inputted
     */
    public synchronized K put(K key, V value) {
        hash.remove(key);
        keys.remove(key);

        hash.put(key, value);
        keys.add(key);

        expire();

        return key;
    }

    /**
     * Remove the object by its key
     *
     * @param key the key to identify the object
     * @return the removed object
     */
    public synchronized V remove(K key) {
        keys.remove(key);
        return hash.remove(key);
    }

    /**
     * Get the object by its key, also moves the object to the front of the cache
     *
     * @param key the key to identify the object
     * @return object or null if key is not found
     */
    public synchronized V get(K key) {
        if (!hash.containsKey(key)) {
            return null;
        }
        keys.remove(key);
        keys.add(0, key);

        return hash.get(key);
    }

    /**
     * Clear the cache by removing and killing all the objects
     */
    public synchronized void flush() {
        while (keys.size() > 0) {
            K key = keys.pop();
            killObject(hash.remove(key));
        }
        hash.clear();
    }

    /**
     * @return all the keys currently in the cache
     */
    public LinkedList<K> keys() {
        return keys;
    }

    /**
     * @return number of items stored
     */
    public int size() {
        return keys.size();
    }

    /**
     * Lock this instance from being accessed by other threads
     */
    protected void lock() {
        lock.lock();
    }

    /**
     * Unlock this instance from being accessed by other threads
     */
    protected void unlock() {
        lock.unlock();
    }
}
