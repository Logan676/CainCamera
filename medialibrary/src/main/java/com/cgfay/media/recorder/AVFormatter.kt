package com.cgfay.media.recorder

import android.media.AudioFormat

/**
 * Format definitions aligned with the native layer. This Kotlin version
 * provides a small Compose component for selecting audio sample formats.
 */
object AVFormatter {
    // Pixel formats
    const val PIXEL_FORMAT_NONE = 0
    const val PIXEL_FORMAT_NV21 = 1
    const val PIXEL_FORMAT_YV12 = 2
    const val PIXEL_FORMAT_NV12 = 3
    const val PIXEL_FORMAT_YUV420P = 4
    const val PIXEL_FORMAT_YUV420SP = 5
    const val PIXEL_FORMAT_ARGB = 6
    const val PIXEL_FORMAT_ABGR = 7
    const val PIXEL_FORMAT_RGBA = 8

    // Sample formats
    const val SAMPLE_FORMAT_NONE = 0
    const val SAMPLE_FORMAT_8BIT = 8
    const val SAMPLE_FORMAT_16BIT = 16
    const val SAMPLE_FORMAT_FLOAT = 32

    /**
     * Translate Android's [AudioFormat] value to a sample format constant.
     */
    fun getSampleFormat(audioFormat: Int): Int = when (audioFormat) {
        AudioFormat.ENCODING_PCM_8BIT -> SAMPLE_FORMAT_8BIT
        AudioFormat.ENCODING_PCM_16BIT -> SAMPLE_FORMAT_16BIT
        AudioFormat.ENCODING_PCM_FLOAT -> SAMPLE_FORMAT_FLOAT
        else -> SAMPLE_FORMAT_NONE
    }

}

