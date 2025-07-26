package com.cgfay.picker.model

import android.database.Cursor
import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import androidx.compose.runtime.Stable
import com.cgfay.picker.loader.AlbumDataLoader

/**
 * Immutable model representing an album with a cover image and item count.
 */
@Stable
data class AlbumData(
    val id: String,
    val coverUri: Uri,
    private val rawDisplayName: String,
    val count: Long
) : Parcelable {

    val displayName: String
        get() = if (isAll) "所有照片" else rawDisplayName

    val isAll: Boolean
        get() = id == ALBUM_ID_ALL

    val isEmpty: Boolean
        get() = count == 0L

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(id)
        dest.writeParcelable(coverUri, flags)
        dest.writeString(rawDisplayName)
        dest.writeLong(count)
    }

    override fun describeContents(): Int = 0

    companion object {
        const val ALBUM_ID_ALL = "-1"
        const val ALBUM_NAME_ALL = "All"

        @JvmField
        val CREATOR: Parcelable.Creator<AlbumData> = object : Parcelable.Creator<AlbumData> {
            override fun createFromParcel(source: Parcel): AlbumData = AlbumData(
                id = source.readString() ?: "",
                coverUri = source.readParcelable(Uri::class.java.classLoader) ?: Uri.EMPTY,
                rawDisplayName = source.readString() ?: "",
                count = source.readLong()
            )

            override fun newArray(size: Int): Array<AlbumData?> = arrayOfNulls(size)
        }

        /**
         * Create [AlbumData] from a media store cursor.
         */
        @JvmStatic
        fun fromCursor(cursor: Cursor): AlbumData {
            val uriIndex = cursor.getColumnIndex(AlbumDataLoader.COLUMN_URI)
            val countIndex = cursor.getColumnIndex(AlbumDataLoader.COLUMN_COUNT)
            val uriString = if (uriIndex >= 0) cursor.getString(uriIndex) else null
            return AlbumData(
                id = cursor.getString(cursor.getColumnIndexOrThrow("bucket_id")),
                coverUri = Uri.parse(uriString ?: ""),
                rawDisplayName = cursor.getString(cursor.getColumnIndexOrThrow("bucket_display_name")),
                count = if (countIndex >= 0) cursor.getLong(countIndex) else 0L
            )
        }
    }
}
