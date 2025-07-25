package com.cgfay.camera.utils

import android.content.Context
import android.os.Environment

import java.io.File

/**
 * Utility functions for generating cache paths.
 */
object PathConstraints {

    /**
     * Returns an image cache path inside external cache when available.
     */
    fun getImageCachePath(context: Context): String {
        val directoryPath = if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()) {
            context.externalCacheDir?.absolutePath ?: context.cacheDir.absolutePath
        } else {
            context.cacheDir.absolutePath
        }
        val path = directoryPath + File.separator + "CainCamera_" + System.currentTimeMillis() + ".jpeg"
        File(path).parentFile?.mkdirs()
        return path
    }

    /**
     * Returns a video cache path inside external cache when available.
     */
    fun getVideoCachePath(context: Context): String {
        val directoryPath = if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()) {
            context.externalCacheDir?.absolutePath ?: context.cacheDir.absolutePath
        } else {
            context.cacheDir.absolutePath
        }
        val path = directoryPath + File.separator + "CainCamera_" + System.currentTimeMillis() + ".mp4"
        File(path).parentFile?.mkdirs()
        return path
    }

    /**
     * Returns a temporary audio file path.
     */
    fun getAudioTempPath(context: Context): String {
        val directoryPath = if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()) {
            context.externalCacheDir?.absolutePath ?: context.cacheDir.absolutePath
        } else {
            context.cacheDir.absolutePath
        }
        val path = directoryPath + File.separator + "temp.aac"
        File(path).parentFile?.mkdirs()
        return path
    }

    /**
     * Returns a temporary video file path.
     */
    fun getVideoTempPath(context: Context): String {
        val directoryPath = if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState() &&
            context.externalCacheDir != null) {
            context.externalCacheDir!!.absolutePath
        } else {
            context.cacheDir.absolutePath
        }
        val path = directoryPath + File.separator + "temp.mp4"
        File(path).parentFile?.mkdirs()
        return path
    }
}

