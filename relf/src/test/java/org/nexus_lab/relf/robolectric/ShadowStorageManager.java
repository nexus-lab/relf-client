package org.nexus_lab.relf.robolectric;

import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.M;

import android.os.Build;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

import java.util.ArrayList;
import java.util.List;

/**
 * Robolectric shadow {@link StorageManager} class for testing.
 *
 * @author Ruipeng Zhang
 */
@Implements(value = StorageManager.class, minSdk = Build.VERSION_CODES.LOLLIPOP)
public class ShadowStorageManager extends org.robolectric.shadows.ShadowStorageManager {
    private static List<StorageVolume> volumes = new ArrayList<>();

    /**
     * Get all storage volumes from a list of added volumes
     *
     * @param userId ignored
     * @param flags  ignored
     * @return An array of {@link StorageVolume}
     */
    @Implementation(minSdk = M)
    protected static StorageVolume[] getVolumeList(int userId, int flags) {
        return volumes.toArray(new StorageVolume[0]);
    }

    /**
     * Gets the volume list from {@link #getVolumeList(int, int)}
     *
     * @return volume list
     */
    @Implementation(minSdk = LOLLIPOP)
    public StorageVolume[] getVolumeList() {
        return getVolumeList(0, 0);
    }

    /**
     * Adds a {@link StorageVolume} to the list returned by {@link #getStorageVolumes()}.
     *
     * @param storageVolume to add to list
     */
    public void addStorageVolume(StorageVolume storageVolume) {
        super.addStorageVolume(storageVolume);
        volumes.add(storageVolume);
    }
}
