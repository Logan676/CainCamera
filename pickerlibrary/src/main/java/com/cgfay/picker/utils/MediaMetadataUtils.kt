package com.cgfay.picker.utils

import android.app.Activity
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.graphics.BitmapFactory
import android.graphics.Point
import android.media.ExifInterface
import android.media.MediaExtractor
import android.media.MediaFormat
import android.net.Uri
import android.provider.MediaStore
import android.text.TextUtils
import android.util.DisplayMetrics
import android.util.Log
import androidx.annotation.NonNull
import com.cgfay.picker.model.MediaData
import java.io.*
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*

object MediaMetadataUtils {
    private val TAG = MediaMetadataUtils::class.java.simpleName
    private const val MAX_WIDTH = 2160
    private const val SCHEME_CONTENT = "content"

    fun buildImageMetadata(@NonNull context: Context, @NonNull mediaData: MediaData) {
        if (mediaData.width > 0 && mediaData.height > 0) {
            return
        }
        val path = getPath(context.contentResolver, mediaData.contentUri)
        if (TextUtils.isEmpty(path)) {
            return
        }
        val file = File(path!!)
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        var bis: BufferedInputStream? = null
        try {
            bis = BufferedInputStream(FileInputStream(file))
            BitmapFactory.decodeStream(bis, null, options)
            mediaData.width = options.outWidth
            mediaData.height = options.outHeight
        } catch (e: FileNotFoundException) {
            Log.w(TAG, e.localizedMessage ?: "")
        } finally {
            try {
                bis?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    fun buildVideoMetadata(@NonNull context: Context, @NonNull mediaData: MediaData) {
        if (mediaData.width > 0 && mediaData.height > 0) {
            return
        }
        val size = getDimensions(getPath(context.contentResolver, mediaData.contentUri))
        mediaData.width = size[0]
        mediaData.height = size[1]
    }

    private fun getDimensions(path: String?): IntArray {
        val dimension = IntArray(2)
        if (!TextUtils.isEmpty(path)) {
            var mediaExtractor: MediaExtractor? = null
            var fis: FileInputStream? = null
            try {
                mediaExtractor = MediaExtractor()
                val file = File(path!!)
                fis = FileInputStream(file)
                val fd = fis.fd
                mediaExtractor.setDataSource(fd)
                val numTracks = mediaExtractor.trackCount
                var format: MediaFormat? = null
                for (i in 0 until numTracks) {
                    format = mediaExtractor.getTrackFormat(i)
                    val mimeType = format.getString(MediaFormat.KEY_MIME) ?: continue
                    if (mimeType.startsWith("video")) {
                        if (format.containsKey("display-width")) {
                            dimension[0] = format.getInteger("display-width")
                        }
                        if (dimension[0] == 0 && format.containsKey(MediaFormat.KEY_WIDTH)) {
                            dimension[0] = format.getInteger(MediaFormat.KEY_WIDTH)
                        }
                        if (format.containsKey("display-height")) {
                            dimension[1] = format.getInteger("display-height")
                        }
                        if (dimension[1] == 0 && format.containsKey(MediaFormat.KEY_HEIGHT)) {
                            dimension[1] = format.getInteger(MediaFormat.KEY_HEIGHT)
                        }
                        break
                    }
                }
            } catch (_: Throwable) {
            } finally {
                try {
                    fis?.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                mediaExtractor?.release()
            }
        }
        return dimension
    }

    fun getPixelsCount(resolver: ContentResolver, uri: Uri): Int {
        val size = getBitmapBound(resolver, uri)
        return size.x * size.y
    }

    fun getImageContentUri(@NonNull context: Context, @NonNull file: File): Uri? {
        val filePath = file.absolutePath
        val cursor = context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            arrayOf(MediaStore.Images.Media._ID),
            MediaStore.Images.Media.DATA + "=? ",
            arrayOf(filePath),
            null
        )
        return if (cursor != null && cursor.moveToFirst()) {
            val id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID))
            val baseUri = Uri.parse("content://media/external/images/media")
            Uri.withAppendedPath(baseUri, "$id")
        } else {
            if (file.exists()) {
                val values = ContentValues()
                values.put(MediaStore.Images.Media.DATA, filePath)
                context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            } else {
                null
            }
        }
    }

    fun getBitmapSize(activity: Activity?, path: String): Point {
        if (activity == null) {
            return Point(MAX_WIDTH, MAX_WIDTH)
        }
        val uri = getImageContentUri(activity, File(path)) ?: return Point(MAX_WIDTH, MAX_WIDTH)
        return getBitmapSize(activity, uri)
    }

    fun getBitmapSize(activity: Activity, uri: Uri): Point {
        val resolver = activity.contentResolver
        val imageSize = getBitmapBound(resolver, uri)
        var width = imageSize.x
        var height = imageSize.y
        if (shouldRotate(resolver, uri)) {
            width = imageSize.y
            height = imageSize.x
        }
        if (width == 0 || height == 0) {
            return Point(MAX_WIDTH, MAX_WIDTH)
        }
        val metrics = DisplayMetrics()
        activity.windowManager.defaultDisplay.getMetrics(metrics)
        val screenWidth = metrics.widthPixels.toFloat()
        val screenHeight = metrics.heightPixels.toFloat()
        val widthScale = screenWidth / width
        val heightScale = screenHeight / height
        return if (widthScale > heightScale) {
            Point((width * widthScale).toInt(), (height * heightScale).toInt())
        } else {
            Point((width * widthScale).toInt(), (height * heightScale).toInt())
        }
    }

    private fun getBitmapBound(resolver: ContentResolver, uri: Uri): Point {
        var input: InputStream? = null
        return try {
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            input = resolver.openInputStream(uri)
            BitmapFactory.decodeStream(input, null, options)
            val width = options.outWidth
            val height = options.outHeight
            Point(width, height)
        } catch (_: FileNotFoundException) {
            Point(0, 0)
        } finally {
            try {
                input?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    fun getPath(@NonNull context: Context, @NonNull uri: Uri): String? {
        return getPath(context.contentResolver, uri)
    }

    fun getPath(resolver: ContentResolver, uri: Uri?): String? {
        if (uri == null) {
            return null
        }
        if (SCHEME_CONTENT == uri.scheme) {
            var cursor: Cursor? = null
            return try {
                cursor = resolver.query(uri, arrayOf(MediaStore.Images.ImageColumns.DATA), null, null, null)
                if (cursor == null || !cursor.moveToFirst()) {
                    null
                } else {
                    cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA))
                }
            } finally {
                cursor?.close()
            }
        }
        return uri.path
    }

    private fun shouldRotate(resolver: ContentResolver, uri: Uri): Boolean {
        val exif = try {
            ExifInterfaceUtils.newInstance(getPath(resolver, uri))
        } catch (e: IOException) {
            Log.e(TAG, "shouldRotate: could not read exif info of image:" + uri)
            return false
        }
        val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1)
        return orientation == ExifInterface.ORIENTATION_ROTATE_90 || orientation == ExifInterface.ORIENTATION_ROTATE_270
    }

    fun getMBSize(byteSize: Long): Float {
        val df = NumberFormat.getNumberInstance(Locale.US) as DecimalFormat
        df.applyPattern("0.0")
        var result = df.format(byteSize.toFloat() / 1024 / 1024)
        result = result.replace(",", ".")
        return result.toFloat()
    }
}
