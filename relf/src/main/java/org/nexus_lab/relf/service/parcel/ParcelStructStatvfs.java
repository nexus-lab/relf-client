package org.nexus_lab.relf.service.parcel;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Parcel wrapper for {@link android.system.StructStatVfs} and implementation of the same-name AIDL.
 *
 * @author Ruipeng Zhang
 */
@SuppressWarnings({"checkstyle:membername", "checkstyle:visibilitymodifier"})
public class ParcelStructStatvfs implements Parcelable {
    /**
     * @inheritDoc
     */
    public static final Creator<ParcelStructStatvfs> CREATOR = new Creator<ParcelStructStatvfs>() {
        @Override
        public ParcelStructStatvfs createFromParcel(Parcel in) {
            return new ParcelStructStatvfs(in);
        }

        @Override
        public ParcelStructStatvfs[] newArray(int size) {
            return new ParcelStructStatvfs[size];
        }
    };
    /** Filesystem block size */
    public long f_bsize;
    /** Fragment size */
    public long f_frsize;
    /** Size of fs in f_frsize units */
    public long f_blocks;
    /** Number of free blocks */
    public long f_bfree;
    /** Number of free blocks for unprivileged users */
    public long f_bavail;
    /** Number of inodes */
    public long f_files;
    /** Number of free inodes */
    public long f_ffree;
    /** Number of free inodes for unprivileged users */
    public long f_favail;
    /** Filesystem ID */
    public long f_fsid;
    /** Mount flags */
    public long f_flag;
    /** Maximum filename length */
    public long f_namemax;

    protected ParcelStructStatvfs(Parcel in) {
        readFromParcel(in);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeLong(f_bsize);
        out.writeLong(f_frsize);
        out.writeLong(f_blocks);
        out.writeLong(f_bfree);
        out.writeLong(f_bavail);
        out.writeLong(f_files);
        out.writeLong(f_ffree);
        out.writeLong(f_favail);
        out.writeLong(f_fsid);
        out.writeLong(f_flag);
        out.writeLong(f_namemax);
    }

    /**
     * Convert a parcel into {@link ParcelStructStat} object.
     *
     * @param in the input parcel.
     */
    public void readFromParcel(Parcel in) {
        f_bsize = in.readLong();
        f_frsize = in.readLong();
        f_blocks = in.readLong();
        f_bfree = in.readLong();
        f_bavail = in.readLong();
        f_files = in.readLong();
        f_ffree = in.readLong();
        f_favail = in.readLong();
        f_fsid = in.readLong();
        f_flag = in.readLong();
        f_namemax = in.readLong();
    }
}
