package com.cgfay.uitls.utils

import android.text.TextPaint
import java.text.SimpleDateFormat
import java.util.*

/**
 * 字符串工具
 * Created by cain.huang on 2017/12/29.
 */
object StringUtils {
    const val EMPTY = ""
    private const val DEFAULT_DATE_PATTERN = "yyyy-MM-dd"
    private const val DEFAULT_DATETIME_PATTERN = "yyyy-MM-dd hh:mm:ss"
    private const val DEFAULT_FILE_PATTERN = "yyyy-MM-dd-HH-mm-ss"
    private const val KB = 1024.0
    private const val MB = 1048576.0
    private const val GB = 1073741824.0

    @JvmField
    val DATE_FORMAT_PART = SimpleDateFormat("HH:mm")

    /** 获取当前时间的字符串格式 */
    @JvmStatic
    fun getCurrentTimeString(): String = DATE_FORMAT_PART.format(Calendar.getInstance().time)

    /** char类型 */
    @JvmStatic
    fun chatAt(pinyin: String?, index: Int): Char {
        return if (pinyin != null && pinyin.isNotEmpty()) pinyin[index] else ' '
    }

    /** 获取字符串宽度 */
    @JvmStatic
    fun GetTextWidth(text: String?, size: Float): Float {
        if (isEmpty(text)) {
            return 0f
        }
        val fontPaint = TextPaint()
        fontPaint.textSize = size
        return fontPaint.measureText(text!!.trim()) + (size * 0.1f)
    }

    /** 格式化日期字符串 */
    @JvmStatic
    fun formatDate(date: Date, pattern: String): String {
        val format = SimpleDateFormat(pattern)
        return format.format(date)
    }

    @JvmStatic
    fun formatDate(date: Long, pattern: String): String {
        val format = SimpleDateFormat(pattern)
        return format.format(Date(date))
    }

    @JvmStatic
    fun formatDate(date: Date): String = formatDate(date, DEFAULT_DATE_PATTERN)

    @JvmStatic
    fun formatDate(date: Long): String = formatDate(Date(date), DEFAULT_DATE_PATTERN)

    @JvmStatic
    fun getDate(): String = formatDate(Date(), DEFAULT_DATE_PATTERN)

    @JvmStatic
    fun createFileName(): String {
        val date = Date(System.currentTimeMillis())
        val format = SimpleDateFormat(DEFAULT_FILE_PATTERN)
        return format.format(date)
    }

    @JvmStatic
    fun getDateTime(): String = formatDate(Date(), DEFAULT_DATETIME_PATTERN)

    @JvmStatic
    fun formatDateTime(date: Date): String = formatDate(date, DEFAULT_DATETIME_PATTERN)

    @JvmStatic
    fun formatDateTime(date: Long): String = formatDate(Date(date), DEFAULT_DATETIME_PATTERN)

    @JvmStatic
    fun formatGMTDate(gmt: String): String {
        val timeZoneLondon = TimeZone.getTimeZone(gmt)
        return formatDate(Calendar.getInstance(timeZoneLondon).timeInMillis)
    }

    @JvmStatic
    fun join(array: ArrayList<String>?, separator: String): String {
        val result = StringBuffer()
        if (array != null && array.size > 0) {
            for (str in array) {
                result.append(str)
                result.append(separator)
            }
            result.delete(result.length - 1, result.length)
        }
        return result.toString()
    }

    @JvmStatic
    fun join(iter: Iterator<String>?, separator: String): String {
        val result = StringBuffer()
        if (iter != null) {
            while (iter.hasNext()) {
                val key = iter.next()
                result.append(key)
                result.append(separator)
            }
            if (result.isNotEmpty()) result.delete(result.length - 1, result.length)
        }
        return result.toString()
    }

    @JvmStatic
    fun isEmpty(str: String?): Boolean {
        return str == null || str.length == 0 || str.equals("null", ignoreCase = true)
    }

    @JvmStatic
    fun trim(str: String?): String {
        return str?.trim() ?: EMPTY
    }

    @JvmStatic
    fun generateTime(time: Long): String {
        val millisSeconds = (time % 1000).toInt()
        val totalSeconds = (time / 1000).toInt()
        val seconds = totalSeconds % 60
        val minutes = totalSeconds / 60 % 60
        val hours = totalSeconds / 3600
        return if (hours > 0) String.format("%02d:%02d:%02d", hours, minutes, seconds)
        else if (minutes > 0) String.format("%02d:%02d", minutes, seconds)
        else if (totalSeconds > 0) String.format("%02d''%02d", totalSeconds, millisSeconds)
        else String.format("''%02d", millisSeconds)
    }

    @JvmStatic
    fun generateMillisTime(totalMillisSeconds: Int): String {
        val millisSeconds = totalMillisSeconds % 1000
        val totalSeconds = totalMillisSeconds / 1000
        val seconds = totalSeconds % 60
        val minutes = totalSeconds / 60 % 60
        return if (minutes > 0)
            String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
        else if (seconds > 0)
            String.format(Locale.getDefault(), "%02d''%02d", seconds, millisSeconds)
        else
            String.format(Locale.getDefault(), "''%02d", millisSeconds)
    }

    @JvmStatic
    fun generateStandardTime(totalMillisSeconds: Int): String {
        val totalSeconds = totalMillisSeconds / 1000
        val seconds = totalSeconds % 60
        val minutes = totalSeconds / 60 % 60
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    }

    @JvmStatic
    fun generateStandardTime(totalMillisSeconds: Long): String {
        val millisSeconds = (totalMillisSeconds % 1000).toInt()
        val totalSeconds = (totalMillisSeconds / 1000).toInt()
        val seconds = totalSeconds % 60
        val minutes = totalSeconds / 60 % 60
        val hours = totalSeconds / 3600
        return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
    }

    @JvmStatic
    fun generateFileSize(size: Long): String {
        val fileSize: String = when {
            size < KB -> size.toString() + "B"
            size < MB -> String.format("%.1f", size / KB) + "KB"
            size < GB -> String.format("%.1f", size / MB) + "MB"
            else -> String.format("%.1f", size / GB) + "GB"
        }
        return fileSize
    }

    @JvmStatic
    fun findString(search: String, start: String, end: String): String {
        val startLen = start.length
        val startPos = if (isEmpty(start)) 0 else search.indexOf(start)
        if (startPos > -1) {
            val endPos = if (isEmpty(end)) -1 else search.indexOf(end, startPos + startLen)
            if (endPos > -1) return search.substring(startPos + start.length, endPos)
        }
        return ""
    }

    @JvmStatic
    fun substring(search: String, start: String, end: String, defaultValue: String): String {
        val startLen = start.length
        val startPos = if (isEmpty(start)) 0 else search.indexOf(start)
        if (startPos > -1) {
            val endPos = if (isEmpty(end)) -1 else search.indexOf(end, startPos + startLen)
            return if (endPos > -1) search.substring(startPos + start.length, endPos)
            else search.substring(startPos + start.length)
        }
        return defaultValue
    }

    @JvmStatic
    fun substring(search: String, start: String, end: String): String =
        substring(search, start, end, "")

    @JvmStatic
    fun concat(vararg strs: String?): String {
        val result = StringBuffer()
        if (strs != null) {
            for (str in strs) {
                if (str != null) result.append(str)
            }
        }
        return result.toString()
    }

    @JvmStatic
    fun makeSafe(s: String?): String = s ?: ""
}
