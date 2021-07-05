package org.nexus_lab.relf.utils.os;

import android.util.Log;

import org.nexus_lab.relf.service.RelfService;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * System memory information.
 *
 * @author Ruipeng Zhang
 */
public final class Memory {
    private static final String TAG = Memory.class.getSimpleName();

    private final Map<String, Long> info = new HashMap<>();

    public Memory() {
        try (RelfService.LineReader reader = new RelfService.LineReader("/proc/meminfo")) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] entry = line.split(":");
                if (entry.length > 1) {
                    String key = entry[0];
                    long value = Long.parseLong(entry[1].trim().split("\\s")[0]);
                    info.put(key, value * 1024);
                }
            }
        } catch (IOException e) {
            Log.w(TAG, e);
        }
    }

    /**
     * @return total system memory in bytes.
     */
    @SuppressWarnings("ConstantConditions")
    public long memTotal() {
        if (info.containsKey("MemTotal")) {
            return info.get("MemTotal");
        }
        return -1;
    }
}
