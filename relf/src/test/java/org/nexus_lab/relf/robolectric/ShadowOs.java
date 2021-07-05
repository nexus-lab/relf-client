package org.nexus_lab.relf.robolectric;

import android.os.Build;
import android.system.OsConstants;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/**
 * Robolectric shadow {@link android.system.Os} class for testing.
 *
 * @author Ruipeng Zhang
 */
@Implements(value = android.system.Os.class, minSdk = Build.VERSION_CODES.LOLLIPOP)
public class ShadowOs {
    /**
     * See <a href="http://man7.org/linux/man-pages/man3/sysconf.3.html">sysconf(3)</a>.
     *
     * @param name system constant name
     * @return system constant value
     */
    @Implementation
    public static long sysconf(int name) {
        if (name == OsConstants._SC_CLK_TCK) {
            return 100;
        }
        if (name == OsConstants._SC_PAGE_SIZE || name == OsConstants._SC_PAGESIZE) {
            return 4096;
        }
        return -1;
    }
}
