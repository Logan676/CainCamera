package com.cgfay.filter.ndkfilter

import android.graphics.Bitmap
import android.util.Log

/**
 * Image filters implemented with native code.
 */
object ImageFilter {
    private const val TAG = "ImageFilter"

    init {
        try {
            System.loadLibrary("nativefilter")
        } catch (e: UnsatisfiedLinkError) {
            Log.e(TAG, "Failed to load native library nativefilter", e)
            throw RuntimeException("Failed to load native library: nativefilter", e)
        }
    }

    /** Obtain the singleton instance for Java callers. */
    @JvmStatic
    fun getInstance(): ImageFilter = this

    private external fun nativeMosaic(source: Bitmap, radius: Int): Int
    private external fun nativeLookupTable(bitmap: Bitmap, lookupTable: Bitmap): Int
    private external fun nativeInvertFilter(bitmap: Bitmap): Int
    private external fun nativeBlackWhiteFilter(bitmap: Bitmap): Int
    private external fun nativeBrightContrastFilter(bitmap: Bitmap, brightness: Float, contrast: Float): Int
    private external fun nativeColorQuantizeFilter(bitmap: Bitmap, levels: Float): Int
    private external fun nativeHistogramEqualFilter(bitmap: Bitmap): Int
    private external fun nativeShiftFilter(bitmap: Bitmap, amount: Int): Int
    private external fun nativeVignetteFilter(bitmap: Bitmap, size: Float): Int
    private external fun nativeGaussianBlurFilter(bitmap: Bitmap): Int
    private external fun nativeStackBlurFilter(bitmap: Bitmap, radius: Int): Int

    /** Mosaic filter. */
    fun filterMosaic(bitmap: Bitmap, radius: Int): Int =
        nativeMosaic(bitmap, radius)

    /** Lookup table filter. */
    fun filterLookupTable512(bitmap: Bitmap, lookupTable: Bitmap): Int =
        nativeLookupTable(bitmap, lookupTable)

    /** Invert color filter. */
    fun filterInvert(source: Bitmap): Int =
        nativeInvertFilter(source)

    /** Black & white filter. */
    fun filterBlackWhite(source: Bitmap): Int =
        nativeBlackWhiteFilter(source)

    /** Brightness and contrast filter. */
    fun filterBrightContrast(bitmap: Bitmap, brightness: Float, contrast: Float): Int =
        nativeBrightContrastFilter(bitmap, brightness, contrast)

    /** Color quantization filter. */
    fun filterColorQuantize(bitmap: Bitmap, levels: Float): Int =
        nativeColorQuantizeFilter(bitmap, levels)

    /** Histogram equalization filter. */
    fun filterHistogramEqual(bitmap: Bitmap): Int =
        nativeHistogramEqualFilter(bitmap)

    /** Pixel shift filter. */
    fun filterShift(bitmap: Bitmap, amount: Int): Int =
        nativeShiftFilter(bitmap, amount)

    /** Vignette filter. */
    fun filterVignette(bitmap: Bitmap, size: Float): Int =
        nativeVignetteFilter(bitmap, size)

    /** Gaussian blur filter. */
    fun filterGaussianBlur(bitmap: Bitmap): Int =
        nativeGaussianBlurFilter(bitmap)

    /** Stack blur filter. */
    fun filterStackBlur(bitmap: Bitmap, radius: Int): Int =
        nativeStackBlurFilter(bitmap, radius)
}

