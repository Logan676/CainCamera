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
import android.text.TextUtils
import android.util.Log

/**
 * 媒体数据对象
 */
class MediaData : Parcelable {

    var id: Long
    var mimeType: String
    var contentUri: Uri
    var size: Long
    var durationMs: Long
    var width: Int
    var height: Int
    var orientation: Int

    @Throws(Exception::class)
    constructor(context: Context, cursor: Cursor) {
        id = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)).toLong()
        mimeType = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.MIME_TYPE))
        width = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.WIDTH))
        height = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.HEIGHT))
        size = cursor.getLong(cursor.getColumnIndex(MediaStore.MediaColumns.SIZE))
        val uri = when {
            isImage() -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            isVideo() -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            else -> MediaStore.Files.getContentUri(EXTERNAL)
        }
        contentUri = ContentUris.withAppendedId(uri, id)
        if (isVideo()) {
            val durationId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                cursor.getColumnIndexOrThrow(MediaStore.Video.VideoColumns.DURATION)
            } else {
                cursor.getColumnIndexOrThrow(KEY_DURATION)
            }
            durationMs = cursor.getLong(durationId)
            if (durationMs == 0L) {
                extractVideoMetadata(context)
            }
        } else {
            durationMs = 0
        }
        orientation = 0
        Log.d(TAG, "MediaData: $this")
    }

    private constructor(parcel: Parcel) {
        id = parcel.readLong()
        mimeType = parcel.readString() ?: ""
        contentUri = parcel.readParcelable(Uri::class.java.classLoader) ?: Uri.EMPTY
        size = parcel.readLong()
        durationMs = parcel.readLong()
        width = parcel.readInt()
        height = parcel.readInt()
        orientation = parcel.readInt()
    }

    private fun extractVideoMetadata(context: Context) {
        val extractor = MediaExtractor()
        try {
            extractor.setDataSource(context, contentUri, null)
            for (i in 0 until extractor.trackCount) {
                val format = extractor.getTrackFormat(i)
                if (format.containsKey(MediaFormat.KEY_MIME)) {
                    val mime = format.getString(MediaFormat.KEY_MIME)
                    if (!TextUtils.isEmpty(mime) && mime!!.startsWith("video/")) {
                        durationMs = format.getLong(MediaFormat.KEY_DURATION) / KILO
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

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeLong(id)
        dest.writeString(mimeType)
        dest.writeParcelable(contentUri, 0)
        dest.writeLong(size)
        dest.writeLong(durationMs)
        dest.writeInt(width)
        dest.writeInt(height)
        dest.writeInt(orientation)
    }

    override fun describeContents(): Int = 0

    fun isImage(): Boolean {
        if (TextUtils.isEmpty(mimeType)) return false
        return mimeType == MimeType.JPEG.mimeType || mimeType == MimeType.JPG.mimeType ||
                mimeType == MimeType.BMP.mimeType || mimeType == MimeType.PNG.mimeType
    }

    fun isVideo(): Boolean {
        if (TextUtils.isEmpty(mimeType)) return false
        return mimeType == MimeType.MPEG.mimeType || mimeType == MimeType.MP4.mimeType ||
                mimeType == MimeType.GPP.mimeType || mimeType == MimeType.MKV.mimeType ||
                mimeType == MimeType.AVI.mimeType
    }

    fun isGif(): Boolean {
        if (TextUtils.isEmpty(mimeType)) return false
        return mimeType == MimeType.GIF.mimeType
    }

    override fun equals(other: Any?): Boolean {
        if (other !is MediaData) return false
        return mimeType == other.mimeType &&
                contentUri == other.contentUri &&
                size == other.size &&
                durationMs == other.durationMs &&
                width == other.width &&
                height == other.height
    }

    override fun hashCode(): Int {
        var result = 1
        result = DEFAULT_HASHCODE * result + id.hashCode()
        result = DEFAULT_HASHCODE * result + mimeType.hashCode()
        result = DEFAULT_HASHCODE * result + contentUri.hashCode()
        result = DEFAULT_HASHCODE * result + size.hashCode()
        result = DEFAULT_HASHCODE * result + durationMs.hashCode()
        result = DEFAULT_HASHCODE * result + width.hashCode()
        result = DEFAULT_HASHCODE * result + height.hashCode()
        return result
    }

    companion object {
        private const val TAG = "MediaData"
        private const val EXTERNAL = "external"
        private const val KEY_DURATION = "duration"
        private const val KILO = 1000
        private const val DEFAULT_HASHCODE = 31

        @JvmField
        val CREATOR: Parcelable.Creator<MediaData> = object : Parcelable.Creator<MediaData> {
            override fun createFromParcel(source: Parcel): MediaData = MediaData(source)
            override fun newArray(size: Int): Array<MediaData?> = arrayOfNulls(size)
        }

        @JvmStatic
        fun valueOf(context: Context, cursor: Cursor): MediaData? {
            var mediaData: MediaData? = null
            try {
                mediaData = MediaData(context, cursor)
            } catch (_: Exception) {
            }
            return mediaData
        }
    }
}
