package com.cgfay.picker.widget.subsamplingview.decoder

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Point
import android.graphics.Rect
import android.net.Uri

interface ImageRegionDecoder {
    @Throws(Exception::class)
    fun init(context: Context, uri: Uri): Point

    fun decodeRegion(sRect: Rect, sampleSize: Int): Bitmap

    fun isReady(): Boolean

    fun recycle()
}
