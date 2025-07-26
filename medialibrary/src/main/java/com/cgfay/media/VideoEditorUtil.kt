package com.cgfay.media

import android.content.Context
import android.os.Environment
import java.io.File
import java.io.IOException
import java.util.Calendar

/**
 * Utility helpers for video editing.
 */
object VideoEditorUtil {

    const val TAG = "VideoEditorUtil"

    /**
     * Create a file path inside [dir] with [suffix].
     */
    @JvmStatic
    fun createPath(dir: String, suffix: String): String {
        val c = Calendar.getInstance()
        val nameBuilder = StringBuilder(dir)
        val d = File(dir)
        if (!d.exists()) {
            d.mkdir()
        }
        nameBuilder.append('/')
        nameBuilder.append(c.get(Calendar.YEAR) - 2000)
        nameBuilder.append(c.get(Calendar.MONTH) + 1)
        nameBuilder.append(c.get(Calendar.DAY_OF_MONTH))
        nameBuilder.append(c.get(Calendar.HOUR_OF_DAY))
        nameBuilder.append(c.get(Calendar.MINUTE))
        nameBuilder.append(c.get(Calendar.SECOND))
        nameBuilder.append(c.get(Calendar.MILLISECOND))
        if (!suffix.startsWith('.')) {
            nameBuilder.append('.')
        }
        nameBuilder.append(suffix)
        return nameBuilder.toString()
    }

    /**
     * Create a path in the app cache directory.
     */
    @JvmStatic
    fun createPathInBox(context: Context, suffix: String): String {
        val dir = if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState() &&
            context.externalCacheDir != null
        ) {
            context.externalCacheDir!!.absolutePath
        } else {
            context.cacheDir.absolutePath
        }
        return createPath(dir, suffix)
    }

    /**
     * Create a file in [dir] with [suffix].
     */
    @JvmStatic
    fun createFile(dir: String, suffix: String): String {
        val c = Calendar.getInstance()
        val nameBuilder = StringBuilder(dir)
        val d = File(dir)
        if (!d.exists()) {
            d.mkdir()
        }
        nameBuilder.append('/')
        nameBuilder.append(c.get(Calendar.YEAR) - 2000)
        nameBuilder.append(c.get(Calendar.MONTH) + 1)
        nameBuilder.append(c.get(Calendar.DAY_OF_MONTH))
        nameBuilder.append(c.get(Calendar.HOUR_OF_DAY))
        nameBuilder.append(c.get(Calendar.MINUTE))
        nameBuilder.append(c.get(Calendar.SECOND))
        nameBuilder.append(c.get(Calendar.MILLISECOND))
        if (!suffix.startsWith('.')) {
            nameBuilder.append('.')
        }
        nameBuilder.append(suffix)
        try {
            Thread.sleep(1)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        val filePath = nameBuilder.toString()
        val file = File(filePath)
        if (!file.exists()) {
            try {
                file.createNewFile()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return filePath
    }

    /**
     * Create a file in the app cache directory with [suffix].
     */
    @JvmStatic
    fun createFileInBox(context: Context, suffix: String): String {
        val dir = if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState() &&
            context.externalCacheDir != null
        ) {
            context.externalCacheDir!!.absolutePath
        } else {
            context.cacheDir.absolutePath
        }
        return createFile(dir, suffix)
    }
}
