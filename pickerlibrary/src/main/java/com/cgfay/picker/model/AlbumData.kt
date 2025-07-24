package com.cgfay.picker.model

import android.database.Cursor
import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import com.cgfay.picker.loader.AlbumDataLoader

/**
 * 相册数据对象
 */
class AlbumData(
    val id: String,
    val coverUri: Uri,
    private val rawDisplayName: String,
    var count: Long
) : Parcelable {

    val displayName: String
        get() = if (isAll()) "所有照片" else rawDisplayName

    private constructor(source: Parcel) : this(
        source.readString() ?: "",
        source.readParcelable(Uri::class.java.classLoader) ?: Uri.EMPTY,
        source.readString() ?: "",
        source.readLong()
    )

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(id)
        dest.writeParcelable(coverUri, 0)
        dest.writeString(rawDisplayName)
        dest.writeLong(count)
    }

    override fun describeContents(): Int = 0

    /** 加入拍摄item */
    fun addCaptureCount() {
        count++
    }

    fun isAll(): Boolean = ALBUM_ID_ALL == id

    fun isEmpty(): Boolean = count == 0L

    override fun toString(): String {
        return "AlbumData(id='$id', coverUri=$coverUri, displayName='$rawDisplayName', count=$count)"
    }

    companion object {
        const val ALBUM_ID_ALL = "-1"
        const val ALBUM_NAME_ALL = "All"

        @JvmField
        val CREATOR: Parcelable.Creator<AlbumData> = object : Parcelable.Creator<AlbumData> {
            override fun createFromParcel(source: Parcel): AlbumData = AlbumData(source)
            override fun newArray(size: Int): Array<AlbumData?> = arrayOfNulls(size)
        }

        @JvmStatic
        fun valueOf(cursor: Cursor): AlbumData {
            val index = cursor.getColumnIndex(AlbumDataLoader.COLUMN_URI)
            val countIndex = cursor.getColumnIndex(AlbumDataLoader.COLUMN_COUNT)
            val uri = if (index >= 0) cursor.getString(index) else null
            return AlbumData(
                cursor.getString(cursor.getColumnIndex("bucket_id")),
                Uri.parse(uri ?: ""),
                cursor.getString(cursor.getColumnIndex("bucket_display_name")),
                if (countIndex > 0) cursor.getLong(countIndex) else 0
            )
        }
    }
}
