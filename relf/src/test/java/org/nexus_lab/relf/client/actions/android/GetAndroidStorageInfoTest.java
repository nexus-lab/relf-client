package org.nexus_lab.relf.client.actions.android;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.Parcel;
import android.os.UserHandle;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;

import androidx.test.core.app.ApplicationProvider;

import org.nexus_lab.relf.client.actions.ActionCallback;
import org.nexus_lab.relf.client.exceptions.TestSuccessException;
import org.nexus_lab.relf.lib.rdfvalues.android.RDFAndroidStorageInfo;
import org.nexus_lab.relf.proto.AndroidStorageInfo;
import org.nexus_lab.relf.robolectric.ShadowStorageManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.StorageVolumeBuilder;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static org.nexus_lab.relf.utils.ReflectionUtils.Parameter.from;
import static org.nexus_lab.relf.utils.ReflectionUtils.callConstructor;
import static org.nexus_lab.relf.utils.ReflectionUtils.callInstanceMethod;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Ruipeng Zhang
 */
@RunWith(RobolectricTestRunner.class)
@Config(shadows = {ShadowStorageManager.class})
public class GetAndroidStorageInfoTest {
    private Context context;
    private Map<String, StorageVolume> volumes;
    private final ActionCallback callback = new ActionCallback<RDFAndroidStorageInfo>() {
        private int i;

        @Override
        public void onResponse(RDFAndroidStorageInfo info) {
            try {
                StorageVolume volume = volumes.get(info.getDescription());
                assertNotNull(volume);
                assertEquals(volume.getUuid(), info.getFsUuid());
                assertEquals(GetAndroidStorageInfoTest.this.getVolumeState(volume),
                        info.getState());
                assertEquals(volume.getDescription(context), info.getDescription());
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
                    assertEquals((int)callInstanceMethod(volume, "getMtpReserveSpace"),
                            info.getMtpReserveSize().getValue().intValue());
                }
                assertEquals(volume.isPrimary(), info.getIsPrimary());
                assertEquals(volume.isEmulated(), info.getIsEmulated());
                assertEquals(volume.isRemovable(), info.getIsRemovable());
                assertEquals(callInstanceMethod(volume, "allowMassStorage"),
                        info.getIsAllowMassStorage());
                i++;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void onComplete() {
            assertEquals(volumes.size(), i);
            throw new TestSuccessException();
        }
    };

    private StorageVolume createVolume(int id, int descriptionId, String state) throws Exception {
        Parcel parcel = Parcel.obtain();
        parcel.writeInt(0);
        parcel.setDataPosition(0);
        UserHandle owner = new UserHandle(parcel);
        File path = new File("/dev/sda" + id);
        String description = context.getString(descriptionId);
        String uuid = "4213-3435:";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return new StorageVolumeBuilder(String.valueOf(id), path, description, owner,
                    state).build();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return callConstructor(
                    StorageVolume.class,
                    from(String.class, String.valueOf(id)), // String id,
                    from(int.class, 0), // int storageId
                    from(File.class, path), // File path,
                    from(String.class, description), // String description
                    from(boolean.class, true), // boolean primary,
                    from(boolean.class, false), // boolean removable,
                    from(boolean.class, false), // boolean emulated,
                    from(long.class, 0), //  long mtpReserveSize,
                    from(boolean.class, false), // boolean allowMassStorage
                    from(long.class, 100L), // long maxFileSize,
                    from(UserHandle.class, owner), // UserHandle owner,
                    from(String.class, uuid), //  String fsUuid,
                    from(String.class, state)); // String state
        } else {
            StorageVolume volume = callConstructor(
                    StorageVolume.class,
                    from(File.class, path), // File path,
                    from(int.class, descriptionId), // int descriptionId
                    from(boolean.class, true), // boolean primary,
                    from(boolean.class, false), // boolean removable,
                    from(boolean.class, false), // boolean emulated,
                    from(int.class, 0), //  int mtpReserveSize,
                    from(boolean.class, false), // boolean allowMassStorage
                    from(long.class, 100L), // long maxFileSize,
                    from(UserHandle.class, owner));// UserHandle owner
            callInstanceMethod(volume, "setStorageId", from(int.class, 0));
            callInstanceMethod(volume, "setUuid", from(String.class, uuid));
            callInstanceMethod(volume, "setState", from(String.class, state));
            return volume;
        }
    }

    private AndroidStorageInfo.StorageState getVolumeState(StorageVolume volume) {
        String state = volume.getState();
        if (state == null) {
            return AndroidStorageInfo.StorageState.UNKNOWN;
        }
        if (state.equals(Environment.MEDIA_MOUNTED_READ_ONLY)) {
            state = "MOUNTED_READ_ONLY";
        }
        state = state.toUpperCase();
        return AndroidStorageInfo.StorageState.valueOf(state);
    }

    @Before
    public void setup() throws Exception {
        context = ApplicationProvider.getApplicationContext();
        volumes = new HashMap<>();
        StorageManager manager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        ShadowStorageManager shadowStorageManager = (ShadowStorageManager) Shadows.shadowOf(
                manager);
        int[] names = new int[]{
                android.R.string.ok,
                android.R.string.cancel,
                android.R.string.unknownName
        };
        String[] states = new String[]{
                Environment.MEDIA_MOUNTED_READ_ONLY,
                Environment.MEDIA_MOUNTED,
                Environment.MEDIA_UNMOUNTED
        };
        for (int i = 0; i < names.length; i++) {
            StorageVolume volume = createVolume(i, names[i], states[i]);
            shadowStorageManager.addStorageVolume(volume);
            this.volumes.put(volume.getDescription(context), volume);
        }
    }

    @Test(expected = TestSuccessException.class)
    public void execute() {
        new GetAndroidStorageInfo().execute(context, null, callback);
    }
}