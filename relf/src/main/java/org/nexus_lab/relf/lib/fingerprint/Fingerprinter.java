package org.nexus_lab.relf.lib.fingerprint;

import org.nexus_lab.relf.client.vfs.VFSFile;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Compute different types of cryptographic hashes over a file.
 * <p>
 * Depending on type of file and mode of invocation, filetype-specific or
 * generic hashes get computed over a file. Different hashes can cover
 * different ranges of the file. The file is read only once. Memory
 * use of class objects is dominated by min(file size, block size),
 * as defined below.
 * <p>
 * The class delivers an array with dicts of hashes by file type. Where
 * appropriate, embedded signature data is also returned from the file.
 */
public class Fingerprinter {
    private static final int BLOCK_SIZE = 1000000;
    private final MessageDigest[] genericHashers = new MessageDigest[4];
    private VFSFile file;
    private long fileSize;
    private ArrayList<Finger> fingers;

    {
        try {
            genericHashers[0] = MessageDigest.getInstance("MD5");
            genericHashers[1] = MessageDigest.getInstance("SHA-1");
            genericHashers[2] = MessageDigest.getInstance("SHA-256");
            genericHashers[3] = MessageDigest.getInstance("SHA-512");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Cannot instantiate hashers", e);
        }
    }

    public Fingerprinter(VFSFile file) {
        this.fingers = new ArrayList<>();
        this.file = file;
        this.fileSize = file.size();
    }

    /**
     * For all fingers, inspect their next expected range, and return the
     * lowest uninterrupted range of interest. If the range is larger than
     * BLOCK_SIZE, truncate it.
     *
     * @return next range of interest in a Range namedtuple
     */
    protected long[] getNextInterval() {
        Set<Long> starts = new HashSet<>();
        Set<Long> ends = new HashSet<>();
        for (Finger finger : this.fingers) {
            if (finger.getRanges() != null && finger.getRanges().length > 0) {
                starts.add(finger.currentRange()[0]);
                ends.add(finger.currentRange()[1]);
            }
        }
        if (starts.size() == 0) {
            return null;
        }
        long minStart = Collections.min(starts);
        starts.remove(minStart);
        ends.addAll(starts);
        long minEnd = Collections.min(ends);
        if (minEnd - minStart > BLOCK_SIZE) {
            minEnd = minStart + BLOCK_SIZE;
        }
        return new long[]{minStart, minEnd};
    }

    private void adjustIntervals(long start, long end) {
        for (Finger finger : this.fingers) {
            finger.consumeRange(start, end);
        }
    }

    /**
     * Feed data blocks into the hashers of fingers.
     * <p>
     * This function must be called before adjusting fingers for next
     * interval, otherwise the lack of remaining ranges will cause the
     * block not to be hashed for a specific finger.
     * <p>
     * Start and end are used to validate the expected ranges, to catch
     * unexpected use of that logic.
     *
     * @param block the data block
     * @param start beginning offset of this block
     * @param end   offset of the next byte after the block
     */
    private void hashBlock(byte[] block, long start, long end) {
        for (Finger finger : this.fingers) {
            long[] expectedRange = finger.currentRange();
            if (expectedRange == null) {
                continue;
            }
            if (start > expectedRange[0] || (start == expectedRange[0] && end > expectedRange[1])
                    || (start < expectedRange[0] && end > expectedRange[1])) {
                throw new RuntimeException("Cutting across fingers.");
            }
            if (start == expectedRange[0]) {
                finger.hashBlock(block);
            }
        }
    }

    /**
     * Convert {@link MessageDigest} algorithm name to ReLF accepted algorithm name
     *
     * @param hasher hash digest object
     * @return ReLF accepted hash algorithm name
     */
    private String convertHasherName(MessageDigest hasher) {
        String hasherName = hasher.getAlgorithm();
        switch (hasher.getAlgorithm()) {
            case "MD5":
                hasherName = "md5";
                break;
            case "SHA-1":
                hasherName = "sha1";
                break;
            case "SHA-256":
                hasherName = "sha256";
                break;
            case "SHA-512":
                hasherName = "sha512";
                break;
            default:
                hasherName = "sha256";
                break;
        }
        return hasherName;
    }

    /**
     * Finalizing function for the Fingerprint class.
     * <p>
     * This method applies all the different hash functions over the
     * previously specified different ranges of the input file, and
     * computes the resulting hashes.
     * <p>
     * After calling hash(), the state of the object is reset to its
     * initial state, with no fingers defined.
     *
     * @return an list of maps, with each map containing name of the
     * fingerprint type, names of hashes and values, and type-dependent
     * key/value pairs.
     */
    public List<Map<String, Object>> hash() {
        while (true) {
            long[] interval = getNextInterval();
            if (interval == null) {
                break;
            }
            file.seek(interval[0], 0);
            try {
                byte[] block = file.read(interval[1] - interval[0]);
                if (block == null || block.length != interval[1] - interval[0]) {
                    throw new RuntimeException("Short read on file.");
                }
                this.hashBlock(block, interval[0], interval[1]);
                this.adjustIntervals(interval[0], interval[1]);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        List<Map<String, Object>> results = new ArrayList<>();
        for (Finger finger : this.fingers) {
            Map<String, Object> result = new HashMap<>();
            long[] leftover = finger.currentRange();
            if (leftover != null) {
                if (finger.getRanges().length > 1 || leftover[0] != this.fileSize
                        || leftover[1] != this.fileSize) {
                    throw new RuntimeException("Non-empty range remains.");
                }
            }
            result.putAll(finger.getMetadata());
            for (MessageDigest hasher : finger.getHashers()) {
                result.put(convertHasherName(hasher), hasher.digest());
                hasher.reset();
            }
            results.add(result);
        }
        this.fingers.clear();
        Collections.sort(results, (a, b) -> {
            String aName = (String) a.get("name");
            String bName = (String) b.get("name");
            return String.CASE_INSENSITIVE_ORDER.compare(aName, bName);
        });
        return results;
    }

    /**
     * Causes the entire file to be hashed by the given hash functions.
     * <p>
     * This sets up a {@link Finger} for fingerprinting, where the entire file
     * is passed through a pre-defined (or user defined) set of hash functions.
     *
     * @param hashers a list of {@link MessageDigest}. If not provided, the default
     *                hashers will be used. To invoke this without hashers, provide an empty list.
     */
    public void evalGeneric(MessageDigest[] hashers) {
        MessageDigest[] newHashers = hashers == null ? genericHashers : hashers;
        for (MessageDigest hasher : newHashers) {
            hasher.reset();
        }
        HashMap<String, Object> metadata = new HashMap<>();
        metadata.put("name", "generic");
        Finger finger = new Finger(newHashers, new long[][]{new long[]{0, this.fileSize}},
                metadata);
        this.fingers.add(finger);
    }
}
