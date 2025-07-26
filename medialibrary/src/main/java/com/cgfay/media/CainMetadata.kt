package com.cgfay.media

import java.util.*

class CainMetadata(private val parcelMetadata: HashMap<String, String>) {

    fun containsKey(key: String): Boolean = parcelMetadata.containsKey(key)

    fun getString(key: String): String? = parcelMetadata[key]

    fun getInt(key: String): Int = parcelMetadata[key]?.toInt() ?: 0

    fun getLong(key: String): Long = parcelMetadata[key]?.toLong() ?: 0L

    fun getDouble(key: String): Double = parcelMetadata[key]?.toDouble() ?: 0.0

    fun getByteArray(key: String): ByteArray? = parcelMetadata[key]?.toByteArray()

    fun getDate(key: String): Date? {
        if (!containsKey(key)) return null
        val timeDefault = parcelMetadata[key]?.toLong() ?: return null
        val timeZoneStr = parcelMetadata[key] ?: ""
        return if (timeZoneStr.isEmpty()) {
            Date(timeDefault)
        } else {
            val timeZone = TimeZone.getTimeZone(timeZoneStr)
            val cal = Calendar.getInstance(timeZone)
            cal.timeInMillis = timeDefault
            cal.time
        }
    }

    override fun toString(): String {
        return "MediaMetadata{ \n" +
                "parcelMetadata = " + parcelMetadata.toString() +
                "\n}"
    }
}
