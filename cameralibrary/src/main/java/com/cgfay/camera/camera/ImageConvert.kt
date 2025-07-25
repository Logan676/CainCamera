package com.cgfay.camera.camera

import android.graphics.ImageFormat
import android.graphics.Rect
import android.media.Image
import android.util.Log
import androidx.annotation.IntDef
import androidx.annotation.RestrictTo
import androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP
import java.nio.ByteBuffer

/**
 * Image data convert utils
 */
object ImageConvert {

    private const val TAG = "ImageConvert"
    private const val VERBOSE = false

    const val COLOR_FORMAT_I420 = 1
    const val COLOR_FORMAT_NV21 = 2

    @RestrictTo(LIBRARY_GROUP)
    @IntDef(value = [COLOR_FORMAT_I420, COLOR_FORMAT_NV21])
    @Retention(AnnotationRetention.SOURCE)
    annotation class ColorFormat

    /**
     * Get image byte array from [Image]
     */
    @JvmStatic
    fun getDataFromImage(image: Image, @ColorFormat colorFormat: Int): ByteArray {
        require(colorFormat == COLOR_FORMAT_I420 || colorFormat == COLOR_FORMAT_NV21) {
            "only support COLOR_FORMAT_I420 and COLOR_FORMAT_NV21"
        }
        require(isImageFormatSupported(image)) {
            "can't convert Image to byte array, format ${image.format}"
        }
        val crop: Rect = image.cropRect
        val format: Int = image.format
        val width = crop.width()
        val height = crop.height()
        val planes = image.planes
        val data = ByteArray(width * height * ImageFormat.getBitsPerPixel(format) / 8)
        val rowData = ByteArray(planes[0].rowStride)
        if (VERBOSE) {
            Log.v(TAG, "get data from ${planes.size} planes")
        }
        var yLength = 0
        var stride = 1
        for (i in planes.indices) {
            when (i) {
                0 -> {
                    yLength = 0
                    stride = 1
                }
                1 -> {
                    if (colorFormat == COLOR_FORMAT_I420) {
                        yLength = width * height
                        stride = 1
                    } else {
                        yLength = width * height + 1
                        stride = 2
                    }
                }
                2 -> {
                    if (colorFormat == COLOR_FORMAT_I420) {
                        yLength = (width * height * 1.25).toInt()
                        stride = 1
                    } else {
                        yLength = width * height
                        stride = 2
                    }
                }
            }
            val buffer: ByteBuffer = planes[i].buffer
            val rowStride: Int = planes[i].rowStride
            val pixelStride: Int = planes[i].pixelStride
            if (VERBOSE) {
                Log.v(TAG, "pixelStride $pixelStride")
                Log.v(TAG, "rowStride $rowStride")
                Log.v(TAG, "width $width")
                Log.v(TAG, "height $height")
                Log.v(TAG, "buffer size ${buffer.remaining()}")
            }
            val shift = if (i == 0) 0 else 1
            val w = width shr shift
            val h = height shr shift
            buffer.position(rowStride * (crop.top shr shift) + pixelStride * (crop.left shr shift))
            for (row in 0 until h) {
                val length: Int
                if (pixelStride == 1 && stride == 1) {
                    length = w
                    buffer.get(data, yLength, length)
                    yLength += length
                } else {
                    length = (w - 1) * pixelStride + 1
                    buffer.get(rowData, 0, length)
                    var col = 0
                    while (col < w) {
                        data[yLength] = rowData[col * pixelStride]
                        yLength += stride
                        col++
                    }
                }
                if (row < h - 1) {
                    buffer.position(buffer.position() + rowStride - length)
                }
            }
            if (VERBOSE) {
                Log.v(TAG, "Finished reading data from plane $i")
            }
        }
        return data
    }

    private fun isImageFormatSupported(image: Image): Boolean {
        return when (image.format) {
            ImageFormat.YUV_420_888, ImageFormat.NV21, ImageFormat.YV12 -> true
            else -> false
        }
    }
}
