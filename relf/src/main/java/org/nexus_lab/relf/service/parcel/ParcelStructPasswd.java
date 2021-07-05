package org.nexus_lab.relf.service.parcel;

import android.os.Parcel;
import android.os.Parcelable;

import org.nexus_lab.relf.service.struct.StructPasswd;

/**
 * Parcel wrapper for {@link StructPasswd}.
 *
 * @author Ruipeng Zhang
 */
@SuppressWarnings({"checkstyle:membername", "checkstyle:visibilitymodifier"})
public class ParcelStructPasswd extends StructPasswd implements Parcelable {
    /**
     * @inheritDoc
     */
    public static final Creator<ParcelStructPasswd> CREATOR = new Creator<ParcelStructPasswd>() {
        @Override
        public ParcelStructPasswd createFromParcel(Parcel in) {
            return new ParcelStructPasswd(in);
        }

        @Override
        public ParcelStructPasswd[] newArray(int size) {
            return new ParcelStructPasswd[size];
        }
    };

    protected ParcelStructPasswd(Parcel in) {
        readFromParcel(in);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(pw_name);
        out.writeString(pw_passwd);
        out.writeInt(pw_uid);
        out.writeInt(pw_gid);
        out.writeString(pw_gecos);
        out.writeString(pw_dir);
        out.writeString(pw_shell);
    }

    /**
     * Convert a parcel into {@link ParcelStructPasswd} object.
     *
     * @param in the input parcel.
     */
    public void readFromParcel(Parcel in) {
        pw_name = in.readString();
        pw_passwd = in.readString();
        pw_uid = in.readInt();
        pw_gid = in.readInt();
        pw_gecos = in.readString();
        pw_dir = in.readString();
        pw_shell = in.readString();
    }
}
