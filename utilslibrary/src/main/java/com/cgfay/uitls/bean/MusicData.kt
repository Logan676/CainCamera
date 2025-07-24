package com.cgfay.uitls.bean

import android.database.Cursor
import android.os.Parcel
import android.os.Parcelable
import android.provider.MediaStore

/**
 * Music data entity
 */
data class MusicData(
    val id: Long,
    val name: String?,
    val path: String?,
    val duration: Long
) : Parcelable {
    constructor(source: Parcel) : this(
        source.readLong(),
        source.readString(),
        source.readString(),
        source.readLong()
    )

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeLong(id)
        dest.writeString(name)
        dest.writeString(path)
        dest.writeLong(duration)
    }

    override fun describeContents(): Int = 0

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MusicData) return false
        return id == other.id && name == other.name && path == other.path && duration == other.duration
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + (name?.hashCode() ?: 0)
        result = 31 * result + (path?.hashCode() ?: 0)
        result = 31 * result + duration.hashCode()
        return result
    }

    companion object {
        @JvmStatic
        val CREATOR: Parcelable.Creator<MusicData> = object : Parcelable.Creator<MusicData> {
            override fun createFromParcel(source: Parcel): MusicData = MusicData(source)
            override fun newArray(size: Int): Array<MusicData?> = arrayOfNulls(size)
        }

        fun valueof(cursor: Cursor): MusicData {
            return MusicData(
                cursor.getLong(cursor.getColumnIndex(MediaStore.Files.FileColumns._ID)),
                cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)),
                cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA)),
                cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION))
            )
        }
    }
}
