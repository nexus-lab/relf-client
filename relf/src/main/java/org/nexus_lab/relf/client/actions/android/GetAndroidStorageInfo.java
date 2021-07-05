package org.nexus_lab.relf.client.actions.android;

import static org.nexus_lab.relf.utils.ReflectionUtils.Parameter.from;
import static org.nexus_lab.relf.utils.ReflectionUtils.boxType;
import static org.nexus_lab.relf.utils.ReflectionUtils.callInstanceMethod;
import static org.nexus_lab.relf.utils.ReflectionUtils.getFieldValue;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.storage.StorageManager;

import org.nexus_lab.relf.client.actions.Action;
import org.nexus_lab.relf.client.actions.ActionCallback;
import org.nexus_lab.relf.lib.rdfvalues.ByteSize;
import org.nexus_lab.relf.lib.rdfvalues.RDFNull;
import org.nexus_lab.relf.lib.rdfvalues.android.RDFAndroidStorageInfo;
import org.nexus_lab.relf.proto.AndroidStorageInfo;

import org.apache.commons.lang3.StringUtils;

import java.io.File;

/**
 * TODO: 1) cannot get internal storage on Android 5.x 2) cannot get sdcard size
 *
 * @author Ruipeng Zhang
 */
public class GetAndroidStorageInfo implements Action<RDFNull, RDFAndroidStorageInfo> {
    private static final int FLAG_REAL_STATE = 1 << 9;
    private static final int FLAG_INCLUDE_INVISIBLE = 1 << 10;

    @SuppressLint("PrivateApi")
    private Object[] getVolumes(Context context) throws ReflectiveOperationException {
        StorageManager storageManager = (StorageManager) context.getSystemService(
                Context.STORAGE_SERVICE);
        Object[] volumes = new Object[0];
        if (storageManager != null) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                volumes = callInstanceMethod(storageManager, "getVolumeList");
            } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                volumes = callInstanceMethod(storageManager, "getVolumeList",
                        from(int.class, 0),
                        from(int.class, FLAG_REAL_STATE | FLAG_INCLUDE_INVISIBLE));
            } else {
                volumes = storageManager.getStorageVolumes().toArray(volumes);
            }
        }
        return volumes;
    }

    private void setObjectField(RDFAndroidStorageInfo target, Object source, String fieldName) {
        setObjectField(target, fieldName.substring(1), source, fieldName);
    }

    private void setObjectField(RDFAndroidStorageInfo target, String targetField, Object source,
            String sourceField) {
        try {
            Object fieldValue = getFieldValue(source, sourceField);
            if (fieldValue == null) {
                return;
            }
            Class<?> fieldClass = boxType(fieldValue.getClass());
            callInstanceMethod(target, "set" + StringUtils.capitalize(targetField),
                    from(fieldClass, fieldValue));
        } catch (ReflectiveOperationException ignored) {
        }
    }

    private AndroidStorageInfo.StorageState getStorageState(Object volume)
            throws ReflectiveOperationException {
        String state = getFieldValue(volume, "mState");
        if (state == null) {
            return AndroidStorageInfo.StorageState.UNKNOWN;
        }
        switch (state) {
            case Environment.MEDIA_REMOVED:
                return AndroidStorageInfo.StorageState.REMOVED;
            case Environment.MEDIA_UNMOUNTED:
                return AndroidStorageInfo.StorageState.UNMOUNTED;
            case Environment.MEDIA_CHECKING:
                return AndroidStorageInfo.StorageState.CHECKING;
            case Environment.MEDIA_NOFS:
                return AndroidStorageInfo.StorageState.NOFS;
            case Environment.MEDIA_MOUNTED:
                return AndroidStorageInfo.StorageState.MOUNTED;
            case Environment.MEDIA_MOUNTED_READ_ONLY:
                return AndroidStorageInfo.StorageState.MOUNTED_READ_ONLY;
            case Environment.MEDIA_SHARED:
                return AndroidStorageInfo.StorageState.SHARED;
            case Environment.MEDIA_BAD_REMOVAL:
                return AndroidStorageInfo.StorageState.BAD_REMOVAL;
            case Environment.MEDIA_UNMOUNTABLE:
                return AndroidStorageInfo.StorageState.UNMOUNTABLE;
            case Environment.MEDIA_EJECTING:
                return AndroidStorageInfo.StorageState.EJECTING;
            default:
                return AndroidStorageInfo.StorageState.UNKNOWN;
        }
    }

    private RDFAndroidStorageInfo createStorageInfo(Context context, Object volume) {
        if (!"android.os.storage.StorageVolume".equals(volume.getClass().getName())) {
            return null;
        }
        RDFAndroidStorageInfo info = new RDFAndroidStorageInfo();
        setObjectField(info, volume, "mId");
        setObjectField(info, volume, "mDescription");
        setObjectField(info, volume, "mMaxFileSize");
        setObjectField(info, volume, "mFsUuid");
        setObjectField(info, "isPrimary", volume, "mPrimary");
        setObjectField(info, "isRemovable", volume, "mRemovable");
        setObjectField(info, "isEmulated", volume, "mEmulated");
        setObjectField(info, "isAllowMassStorage", volume, "mAllowMassStorage");
        setObjectField(info, "fsUuid", volume, "mUuid");

        try {
            info.setDescription(getFieldValue(volume, "mDescription"));
        } catch (ReflectiveOperationException ignored) {
            try {
                info.setDescription(context.getString(getFieldValue(volume, "mDescriptionId")));
            } catch (ReflectiveOperationException ignored1) {
            }
        }

        try {
            long size = getFieldValue(volume, "mMtpReserveSize");
            info.setMtpReserveSize(new ByteSize(size * 1024 * 1024));
        } catch (ReflectiveOperationException ignored) {
            try {
                int size = getFieldValue(volume, "mMtpReserveSpace");
                info.setMtpReserveSize(new ByteSize(size * 1024 * 1024));
            } catch (ReflectiveOperationException ignored1) {
            }
        }

        try {
            File path = getFieldValue(volume, "mPath");
            info.setPath(path.getAbsolutePath());
            info.setTotalSpace(new ByteSize(path.getTotalSpace()));
            info.setUsedSpace(new ByteSize(path.getTotalSpace() - path.getFreeSpace()));
        } catch (ReflectiveOperationException ignored) {
        }

        try {
            info.setState(getStorageState(volume));
        } catch (ReflectiveOperationException ignored) {
        }

        return info;
    }

    @Override
    public void execute(Context context, RDFNull request, ActionCallback<RDFAndroidStorageInfo> callback) {
        try {
            Object[] volumes = getVolumes(context);
            for (Object volume : volumes) {
                callback.onResponse(createStorageInfo(context, volume));
            }
            callback.onComplete();
        } catch (ReflectiveOperationException e) {
            callback.onError(e);
        }
    }
}
