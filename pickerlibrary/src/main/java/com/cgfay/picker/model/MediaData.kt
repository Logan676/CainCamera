package com.cgfay.picker.model

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.media.MediaExtractor
import android.media.MediaFormat
import android.net.Uri
import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import android.provider.MediaStore
import androidx.compose.runtime.Stable
import com.cgfay.picker.model.MimeType.Companion.isImage
import com.cgfay.picker.model.MimeType.Companion.isVideo

/**
 * Data class representing a media item from the device gallery.
 */
@Stable
data class MediaData(
    val id: Long,
    val mimeType: String,
    val contentUri: Uri,
    val size: Long,
    var durationMs: Long = 0L,
    var width: Int = 0,
    var height: Int = 0,
    var orientation: Int = 0
) : Parcelable {

    fun isImage(): Boolean = MimeType.isImage(mimeType)

    fun isVideo(): Boolean = MimeType.isVideo(mimeType)

    fun isGif(): Boolean = mimeType == MimeType.GIF.mimeType

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeLong(id)
        dest.writeString(mimeType)
        dest.writeParcelable(contentUri, flags)
        dest.writeLong(size)
        dest.writeLong(durationMs)
        dest.writeInt(width)
        dest.writeInt(height)
        dest.writeInt(orientation)
    }

    override fun describeContents(): Int = 0

    private constructor(source: Parcel) : this(
        id = source.readLong(),
        mimeType = source.readString() ?: "",
        contentUri = source.readParcelable(Uri::class.java.classLoader) ?: Uri.EMPTY,
        size = source.readLong(),
        durationMs = source.readLong(),
        width = source.readInt(),
        height = source.readInt(),
        orientation = source.readInt()
    )

    companion object {
        private const val EXTERNAL = "external"
        private const val KEY_DURATION = "duration"
        private const val KILO = 1000L

        @JvmField
        val CREATOR: Parcelable.Creator<MediaData> = object : Parcelable.Creator<MediaData> {
            override fun createFromParcel(source: Parcel): MediaData = MediaData(source)
            override fun newArray(size: Int): Array<MediaData?> = arrayOfNulls(size)
        }

        /**
         * Create [MediaData] from a cursor row.
         */
        @JvmStatic
        fun fromCursor(context: Context, cursor: Cursor): MediaData? = try {
            val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID))
            val mime = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.MIME_TYPE))
            var width = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.WIDTH))
            var height = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.HEIGHT))
            val size = cursor.getLong(cursor.getColumnIndex(MediaStore.MediaColumns.SIZE))
            val base = when {
                isImage(mime) -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                isVideo(mime) -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                else -> MediaStore.Files.getContentUri(EXTERNAL)
            }
            val uri = ContentUris.withAppendedId(base, id)
            var duration = 0L
            if (isVideo(mime)) {
                val durationId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    cursor.getColumnIndexOrThrow(MediaStore.Video.VideoColumns.DURATION)
                } else {
                    cursor.getColumnIndexOrThrow(KEY_DURATION)
                }
                duration = cursor.getLong(durationId)
                if (duration == 0L) {
                    val extractor = MediaExtractor()
                    try {
                        extractor.setDataSource(context, uri, null)
                        for (i in 0 until extractor.trackCount) {
                            val format = extractor.getTrackFormat(i)
                            if (format.containsKey(MediaFormat.KEY_MIME)) {
                                val formatMime = format.getString(MediaFormat.KEY_MIME)
                                if (!formatMime.isNullOrEmpty() && formatMime.startsWith("video/")) {
                                    duration = format.getLong(MediaFormat.KEY_DURATION) / KILO
                                    if (width == 0 || height == 0) {
                                        width = format.getInteger(MediaFormat.KEY_WIDTH)
                                        height = format.getInteger(MediaFormat.KEY_HEIGHT)
                                    }
                                    break
                                }
                            }
                        }
                    } finally {
                        extractor.release()
                    }
                }
            }
            MediaData(id, mime, uri, size, duration, width, height, 0)
        } catch (e: Exception) {
            null
        }
    }
}
