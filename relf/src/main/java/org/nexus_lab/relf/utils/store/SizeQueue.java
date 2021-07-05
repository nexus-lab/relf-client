package org.nexus_lab.relf.utils.store;

import org.nexus_lab.relf.lib.rdfvalues.RDFValue;
import org.nexus_lab.relf.proto.GrrMessage;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;

/**
 * A Queue which limits the total memory size of its elements.
 *
 * @param <T> element type
 * @author Ruipeng Zhang
 */
public class SizeQueue<T extends RDFValue> {
    private long maxSize;
    private long totalSize;
    private LinkedList<ItemWrapper> queue;
    private LinkedList<ItemWrapper> reversed;

    public SizeQueue() {
        this(1024);
    }

    /**
     * @param maxSize max total size of elements in byte
     */
    public SizeQueue(long maxSize) {
        this.maxSize = maxSize;
        this.queue = new LinkedList<>();
        this.reversed = new LinkedList<>();
    }

    /**
     * Put an item on the queue, blocking if it is too full.
     *
     * @param item     element to be add
     * @param priority the priority of element
     * @param block    if true we block indefinitely
     * @throws QueueFullException if the queue cannot accept more elements
     */
    public void put(T item, GrrMessage.Priority priority, boolean block)
            throws QueueFullException {
        put(item, priority, block, 1000);
    }

    /**
     * Put an item on the queue, blocking if it is too full.
     *
     * @param item     element to be add
     * @param priority the priority of element
     * @param block    if true we block indefinitely
     * @param timeout  maximum time we spend waiting on the queue (1 sec resolution)
     * @throws QueueFullException if the queue cannot accept more elements
     */
    public void put(T item, GrrMessage.Priority priority, boolean block, int timeout)
            throws QueueFullException {
        // If high priority is set we don't care about the size of the queue.
        if (priority == null || priority.ordinal() < GrrMessage.Priority.HIGH_PRIORITY.ordinal()) {
            if (!block && totalSize > maxSize) {
                throw new QueueFullException();
            }
            int count = 0;
            while (totalSize >= maxSize) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                }
                count++;
                if (timeout > 0 && count > timeout) {
                    throw new QueueFullException();
                }
            }
        }
        synchronized (this) {
            byte[] message = item.serialize();
            queue.add(new ItemWrapper(item, message.length,
                    priority == null ? 0 : -priority.ordinal()));
            totalSize += message.length;
        }
    }

    /**
     * @return an iterable of the queue
     */
    public synchronized Iterable<T> get() {
        if (!reversed.isEmpty()) {
            Collections.reverse(reversed);
            LinkedList<ItemWrapper> temp = new LinkedList<>();
            temp.addAll(reversed);
            temp.addAll(queue);
            queue = temp;
        }
        Collections.sort(queue, (a, b) -> a.priority - b.priority);
        Collections.reverse(queue);
        reversed = queue;
        queue = new LinkedList<>();

        return () -> new Iterator<T>() {
            @Override
            public boolean hasNext() {
                return !reversed.isEmpty();
            }

            @Override
            public T next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                ItemWrapper wrapper = reversed.removeLast();
                totalSize -= wrapper.size;
                return wrapper.item;
            }
        };
    }

    /**
     * @return total size of the elements in bytes
     */
    public long size() {
        return totalSize;
    }

    /**
     * @return true if total size is greater than the limit
     */
    public boolean isFull() {
        return totalSize >= maxSize;
    }

    /**
     * An exception throws when {@link SizeQueue} cannot accept more elements
     */
    public static class QueueFullException extends IOException {
    }

    private class ItemWrapper {
        private final int size;
        private final T item;
        private final int priority;

        ItemWrapper(T item, int size, int priority) {
            this.item = item;
            this.size = size;
            this.priority = priority;
        }
    }
}
