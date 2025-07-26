package com.cgfay.media

import java.util.Calendar
import java.util.Date
import java.util.HashMap
import java.util.TimeZone

/**
 * metadata数据对象
 */
class CainMetadata(private val parcelMetadata: HashMap<String, String>) {

    /** 是否包含key */
    fun containsKey(key: String): Boolean = parcelMetadata.containsKey(key)

    fun getString(key: String): String? =
        if (containsKey(key)) parcelMetadata[key]?.let { it } else null

    fun getInt(key: String): Int =
        if (containsKey(key)) parcelMetadata[key]?.toInt() ?: 0 else 0

    fun getLong(key: String): Long =
        if (containsKey(key)) parcelMetadata[key]?.toLong() ?: 0 else 0

    fun getDouble(key: String): Double =
        if (containsKey(key)) parcelMetadata[key]?.toDouble() ?: 0.0 else 0.0

    fun getByteArray(key: String): ByteArray? =
        if (containsKey(key)) parcelMetadata[key]?.toByteArray() else null

    fun getDate(key: String): Date? {
        if (containsKey(key)) {
            val timeDefault = parcelMetadata[key]?.toLong() ?: return null
            val timeZoneStr = parcelMetadata[key] ?: return Date(timeDefault)
            return if (timeZoneStr.isEmpty()) {
                Date(timeDefault)
            } else {
                val timeZone = TimeZone.getTimeZone(timeZoneStr)
                val cal = Calendar.getInstance(timeZone)
                cal.timeInMillis = timeDefault
                cal.time
            }
        }
        return null
    }

    override fun toString(): String =
        "MediaMetadata{ \n" +
                "mParcelMetadata = " + parcelMetadata.toString() +
                "\n}"
}
