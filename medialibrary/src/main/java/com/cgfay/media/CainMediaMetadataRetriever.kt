package com.cgfay.media

import android.content.ContentResolver
import android.content.Context
import android.content.res.AssetFileDescriptor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import com.cgfay.media.annotations.AccessedByNative
import com.cgfay.uitls.utils.BitmapUtils
import com.cgfay.uitls.utils.NativeLibraryLoader
import java.io.FileDescriptor
import java.io.FileNotFoundException
import java.io.IOException
import java.util.HashMap

class CainMediaMetadataRetriever {

    companion object {
        private const val TAG = "CainMediaMetadataRetriever"

        init {
            NativeLibraryLoader.loadLibraries(
                "ffmpeg",
                "metadata_retriever"
            )
            native_init()
        }

        @JvmStatic
        private external fun native_init()
    }

    @AccessedByNative
    private var mNativeContext: Long = 0

    private val EMBEDDED_PICTURE_TYPE_ANY = 0xFFFF

    init {
        native_setup()
    }

    external fun setDataSource(path: String)

    fun setDataSource(uri: String, headers: Map<String, String>) {
        val keys = Array(headers.size) { "" }
        val values = Array(headers.size) { "" }
        var i = 0
        for ((k, v) in headers) {
            keys[i] = k
            values[i] = v
            i++
        }
        _setDataSource(uri, keys, values)
    }

    private external fun _setDataSource(uri: String, keys: Array<String>, values: Array<String>)

    external fun setDataSource(fd: FileDescriptor, offset: Long, length: Long)

    fun setDataSource(fd: FileDescriptor) {
        setDataSource(fd, 0, 0x7ffffffffffffffL)
    }

    fun setDataSource(context: Context, uri: Uri) {
        if (uri.scheme == null || uri.scheme == "file") {
            setDataSource(uri.path!!)
            return
        }
        var fd: AssetFileDescriptor? = null
        try {
            val resolver: ContentResolver = context.contentResolver
            fd = resolver.openAssetFileDescriptor(uri, "r")
            if (fd == null) throw IllegalArgumentException()
            val descriptor = fd.fileDescriptor
            if (!descriptor.valid()) throw IllegalArgumentException()
            if (fd.declaredLength < 0) {
                setDataSource(descriptor)
            } else {
                setDataSource(descriptor, fd.startOffset, fd.declaredLength)
            }
            return
        } catch (e: FileNotFoundException) {
            throw IllegalArgumentException()
        } catch (ex: SecurityException) {
        } finally {
            try { fd?.close() } catch (_: IOException) {}
        }
        setDataSource(uri.toString())
    }

    external fun extractMetadata(keyCode: String): String?
    external fun extractMetadataFromChapter(keyCode: String, chapter: Int): String?

    fun getMetadata(): CainMetadata? {
        val map = _getAllMetadata() ?: return null
        return CainMetadata(map)
    }

    private external fun _getAllMetadata(): HashMap<String, String>?

    fun getFrameAtTime(timeUs: Long, option: Int): Bitmap? {
        if (option < OPTION_PREVIOUS_SYNC || option > OPTION_CLOSEST) {
            throw IllegalArgumentException("Unsupported option: $option")
        }
        val picture = _getFrameAtTime(timeUs, option) ?: return null
        val options = BitmapFactory.Options()
        options.inDither = false
        return BitmapFactory.decodeByteArray(picture, 0, picture.size, options)
    }

    fun getFrameAtTime(timeUs: Long): Bitmap? {
        val picture = _getFrameAtTime(timeUs, OPTION_CLOSEST_SYNC) ?: return null
        val options = BitmapFactory.Options()
        options.inDither = false
        return BitmapFactory.decodeByteArray(picture, 0, picture.size, options)
    }

    fun getFrameAtTime(): Bitmap? = getFrameAtTime(-1, OPTION_CLOSEST_SYNC)

    private external fun _getFrameAtTime(timeUs: Long, option: Int): ByteArray?

    fun getScaledFrameAtTime(timeUs: Long, option: Int, width: Int, height: Int): Bitmap? {
        if (option < OPTION_PREVIOUS_SYNC || option > OPTION_CLOSEST) {
            throw IllegalArgumentException("Unsupported option: $option")
        }
        val picture = _getScaledFrameAtTime(timeUs, option, width, height) ?: return null
        val options = BitmapFactory.Options()
        options.inDither = false
        val rotate = extractMetadata(METADATA_KEY_ROTAE)?.toIntOrNull() ?: 0
        return if (rotate % 90 != 0) {
            BitmapUtils.rotateBitmap(picture, rotate)
        } else {
            BitmapFactory.decodeByteArray(picture, 0, picture.size, options)
        }
    }

    fun getScaledFrameAtTime(timeUs: Long, width: Int, height: Int): Bitmap? {
        val picture = _getScaledFrameAtTime(timeUs, OPTION_CLOSEST_SYNC, width, height) ?: return null
        val options = BitmapFactory.Options()
        options.inDither = false
        val rotate = extractMetadata(METADATA_KEY_ROTAE)?.toIntOrNull() ?: 0
        return if (rotate != 0) {
            BitmapUtils.rotateBitmap(picture, rotate)
        } else {
            BitmapFactory.decodeByteArray(picture, 0, picture.size, options)
        }
    }

    private external fun _getScaledFrameAtTime(timeUs: Long, option: Int, width: Int, height: Int): ByteArray?

    fun getEmbeddedPicture(): ByteArray? = getEmbeddedPicture(EMBEDDED_PICTURE_TYPE_ANY)
    private external fun getEmbeddedPicture(pictureType: Int): ByteArray?

    external fun release()
    private external fun native_setup()
    private external fun native_finalize()

    protected fun finalize() {
        native_finalize()
    }

    companion object Keys {
        const val OPTION_PREVIOUS_SYNC = 0x00
        const val OPTION_NEXT_SYNC = 0x01
        const val OPTION_CLOSEST_SYNC = 0x02
        const val OPTION_CLOSEST = 0x03

        const val METADATA_KEY_ALBUM = "album"
        const val METADATA_KEY_ALBUMARTIST = "album_artist"
        const val METADATA_KEY_ARTIST = "artist"
        const val METADATA_KEY_COMMENT = "comment"
        const val METADATA_KEY_COMPOSER = "composer"
        const val METADATA_KEY_COPYRIGHT = "copyright"
        const val METADATA_KEY_CREATION_TIME = "creation_time"
        const val METADATA_KEY_DATE = "date"
        const val METADATA_KEY_DISC_NUMBER = "disc"
        const val METADATA_KEY_ENCODER = "encoder"
        const val METADATA_KEY_ENCODED_BY = "encoded_by"
        const val METADATA_KEY_FILENAME = "filename"
        const val METADATA_KEY_GENRE = "genre"
        const val METADATA_KEY_LANGUAGE = "language"
        const val METADATA_KEY_PERFORMER = "performer"
        const val METADATA_KEY_PUBLISHER = "publisher"
        const val METADATA_KEY_SERVICE_NAME = "service_name"
        const val METADATA_KEY_SERVICE_PROVIDER = "service_provider"
        const val METADATA_KEY_TITLE = "title"
        const val METADATA_KEY_TRACK = "track"
        const val METADATA_KEY_BITRATE = "bitrate"
        const val METADATA_KEY_DESCRIPTION = "description"
        const val METADATA_KEY_YEAR = "year"
        const val METADATA_KEY_DURATION = "duration"
        const val METADATA_KEY_AUDIO_CODEC = "audio_codec"
        const val METADATA_KEY_VIDEO_CODEC = "video_codec"
        const val METADATA_KEY_ICY_METADATA = "icy_metadata"
        const val METADATA_KEY_ROTAE = "rotate"
        const val METADATA_KEY_FRAME_RATE = "frame_rate"
        const val METADATA_KEY_CHAPTER_START = "chapter_start"
        const val METADATA_KEY_CHAPTER_END = "chapter_end"
        const val METADATA_KEY_CHAPTER_COUNT = "chapter_count"
        const val METADATA_KEY_FILE_SIZE = "file_size"
        const val METADATA_KEY_VIDEO_WIDTH = "video_width"
        const val METADATA_KEY_VIDEO_HEIGHT = "video_height"
    }
}
