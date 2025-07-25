package com.cgfay.filter.ndkfilter

import android.graphics.Bitmap

class ImageFilter private constructor() {

    companion object {
        init {
            System.loadLibrary("nativefilter")
        }

        private val INSTANCE: ImageFilter by lazy { ImageFilter() }
        @JvmStatic
        fun getInstance(): ImageFilter = INSTANCE
    }

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

    fun filterMosaic(bitmap: Bitmap, radius: Int): Int =
        nativeMosaic(bitmap, radius)

    fun filterLookupTable512(bitmap: Bitmap, lookupTable: Bitmap): Int =
        nativeLookupTable(bitmap, lookupTable)

    fun filterInvert(source: Bitmap): Int =
        nativeInvertFilter(source)

    fun filterBlackWhite(source: Bitmap): Int =
        nativeBlackWhiteFilter(source)

    fun filterBrightContrast(bitmap: Bitmap, brightness: Float, contrast: Float): Int =
        nativeBrightContrastFilter(bitmap, brightness, contrast)

    fun filterColorQuantize(bitmap: Bitmap, levels: Float): Int =
        nativeColorQuantizeFilter(bitmap, levels)

    fun filterHistogramEqual(bitmap: Bitmap): Int =
        nativeHistogramEqualFilter(bitmap)

    fun filterShift(bitmap: Bitmap, amount: Int): Int =
        nativeShiftFilter(bitmap, amount)

    fun filterVignette(bitmap: Bitmap, size: Float): Int =
        nativeVignetteFilter(bitmap, size)

    fun filterGaussianBlur(bitmap: Bitmap): Int =
        nativeGaussianBlurFilter(bitmap)

    fun filterStackBlur(bitmap: Bitmap, radius: Int): Int =
        nativeStackBlurFilter(bitmap, radius)
}
