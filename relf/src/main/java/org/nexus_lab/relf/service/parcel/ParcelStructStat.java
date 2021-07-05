package org.nexus_lab.relf.service.parcel;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Parcel wrapper for {@link android.system.StructStat} and implementation of the same-name AIDL.
 *
 * @author Ruipeng Zhang
 */
@SuppressWarnings({"checkstyle:membername", "checkstyle:visibilitymodifier"})
public class ParcelStructStat implements Parcelable {
    /**
     * @inheritDoc
     */
    public static final Creator<ParcelStructStat> CREATOR = new Creator<ParcelStructStat>() {
        @Override
        public ParcelStructStat createFromParcel(Parcel in) {
            return new ParcelStructStat(in);
        }

        @Override
        public ParcelStructStat[] newArray(int size) {
            return new ParcelStructStat[size];
        }
    };
    /** ID of device containing file */
    public long st_dev;
    /** inode number */
    public long st_ino;
    /** protection */
    public long st_mode;
    /** number of hard links */
    public long st_nlink;
    /** user ID of owner */
    public long st_uid;
    /** group ID of owner */
    public long st_gid;
    /** device ID (if special file) */
    public long st_rdev;
    /** total size, in bytes */
    public long st_size;
    /** blocksize for file system I/O */
    public long st_blksize;
    /** number of 512B blocks allocated */
    public long st_blocks;
    /** time of last access */
    public long st_atime;
    /** time of last modification */
    public long st_mtime;
    /** time of last status change */
    public long st_ctime;

    protected ParcelStructStat(Parcel in) {
        readFromParcel(in);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeLong(st_dev);
        out.writeLong(st_ino);
        out.writeLong(st_mode);
        out.writeLong(st_nlink);
        out.writeLong(st_uid);
        out.writeLong(st_gid);
        out.writeLong(st_rdev);
        out.writeLong(st_size);
        out.writeLong(st_blksize);
        out.writeLong(st_blocks);
        out.writeLong(st_atime);
        out.writeLong(st_mtime);
        out.writeLong(st_ctime);
    }

    /**
     * Convert a parcel into {@link ParcelStructStat} object.
     *
     * @param in the input parcel.
     */
    public void readFromParcel(Parcel in) {
        st_dev = in.readLong();
        st_ino = in.readLong();
        st_mode = in.readLong();
        st_nlink = in.readLong();
        st_uid = in.readLong();
        st_gid = in.readLong();
        st_rdev = in.readLong();
        st_size = in.readLong();
        st_blksize = in.readLong();
        st_blocks = in.readLong();
        st_atime = in.readLong();
        st_mtime = in.readLong();
        st_ctime = in.readLong();
    }
}
