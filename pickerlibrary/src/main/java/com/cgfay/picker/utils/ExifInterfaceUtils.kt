package com.cgfay.picker.utils

import android.media.ExifInterface
import android.text.TextUtils
import android.util.Log
import java.io.IOException
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone

/** Utility for accessing ExifInterface information. */
object ExifInterfaceUtils {
    private val TAG = ExifInterfaceUtils::class.java.simpleName

    /** Create a new [ExifInterface] from the given file path. */
    @JvmStatic
    @Throws(IOException::class)
    fun newInstance(filePath: String): ExifInterface {
        require(!TextUtils.isEmpty(filePath)) { "filePath should not be empty!" }
        return ExifInterface(filePath)
    }

    private fun getExifDateTime(filePath: String): Date? {
        val exif = try {
            newInstance(filePath)
        } catch (ex: IOException) {
            Log.e(TAG, "getExifDateTime: cannot read exif:", ex)
            return null
        }
        val date = exif.getAttribute(ExifInterface.TAG_DATETIME) ?: return null
        return try {
            val format = SimpleDateFormat("yyyy:MM:dd HH:mm:ss")
            format.timeZone = TimeZone.getTimeZone("UTC")
            format.parse(date)
        } catch (e: ParseException) {
            Log.e(TAG, "getExifDateTime: failed to parse date token:", e)
            null
        }
    }

    /** Get the EXIF date time in milliseconds. */
    @JvmStatic
    fun getExifDateTimeInMillis(filePath: String): Long {
        val date = getExifDateTime(filePath) ?: return -1
        return date.time
    }

    /**
     * Get orientation degrees stored in EXIF.
     * @return rotation degrees or 0 if unavailable
     */
    @JvmStatic
    fun getExifOrientation(filePath: String): Int {
        val exif = try {
            newInstance(filePath)
        } catch (ex: IOException) {
            Log.e(TAG, "getExifOrientation: cannot read exif:", ex)
            return -1
        }
        return when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0)) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90
            ExifInterface.ORIENTATION_ROTATE_180 -> 180
            ExifInterface.ORIENTATION_ROTATE_270 -> 270
            else -> 0
        }
    }
}
