package org.nexus_lab.relf.lib.fingerprint;

import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Map;

import lombok.Getter;

/**
 * A Finger defines how to hash a file to get specific fingerprints.
 * <p>
 * The Finger contains one or more hash functions, a set of ranges in the
 * file that are to be processed with these hash functions, and relevant
 * metadata and accessor methods.
 * <p>
 * While one Finger provides potentially multiple hashers, they all get
 * fed the same ranges of the file.
 *
 * @author Ruipeng Zhang
 */
public class Finger {
    @Getter
    private long[][] ranges;
    @Getter
    private MessageDigest[] hashers;
    @Getter
    private Map<String, Object> metadata;

    /**
     * @param hashers  hash calculators
     * @param ranges   working ranges of the Finger
     * @param metadata finger metadata
     */
    public Finger(MessageDigest[] hashers, long[][] ranges, Map<String, Object> metadata) {
        this.hashers = hashers;
        this.ranges = ranges;
        this.metadata = metadata;
    }

    /**
     * @return The working range of this Finger. Returns null if there is none
     */
    public long[] currentRange() {
        if (ranges != null && ranges.length > 0) {
            return ranges[0];
        }
        return null;
    }

    /**
     * Consumes an entire range, or part thereof.
     * <p>
     * If the finger has no ranges left, or the curent range start is higher
     * than the end of the consumed block, nothing happens. Otherwise,
     * the current range is adjusted for the consumed block, or removed,
     * if the entire block is consumed. For things to work, the consumed
     * range and the current finger starts must be equal, and the length
     * of the consumed range may not exceed the length of the current range.
     *
     * @param start beginning of range to be consumed
     * @param end   first offset after the consumed range (end + 1)
     */
    public void consumeRange(long start, long end) {
        long[] old = currentRange();
        if (old == null) {
            return;
        }
        if (old[0] > start) {
            if (old[0] < end) {
                throw new RuntimeException("Block end too high.");
            }
            return;
        }
        if (old[0] < start) {
            throw new RuntimeException("Block start too high.");
        }
        if (old[1] == end) {
            ranges = Arrays.copyOfRange(ranges, 1, ranges.length);
        } else if (old[1] > end) {
            ranges[0][0] = end;
            ranges[0][1] = old[1];
        } else {
            throw new RuntimeException("Block length exceeds range.");
        }
    }

    /**
     * Given a data block, feed it to all the registered hashers
     *
     * @param block block data in bytes
     */
    public void hashBlock(byte[] block) {
        for (MessageDigest hasher : hashers) {
            hasher.update(block);
        }
    }
}
