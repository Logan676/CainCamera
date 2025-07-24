package com.cgfay.picker.widget.subsamplingview.decoder

import android.content.ContentResolver
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.AssetManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Bitmap.Config
import android.graphics.BitmapFactory
import android.graphics.BitmapRegionDecoder
import android.graphics.Point
import android.graphics.Rect
import android.net.Uri
import android.text.TextUtils
import java.io.InputStream

class SkiaImageRegionDecoder : ImageRegionDecoder {

    private var decoder: BitmapRegionDecoder? = null
    private val decoderLock = Any()

    companion object {
        private const val FILE_PREFIX = "file://"
        private const val ASSET_PREFIX = "$FILE_PREFIX/android_asset/"
        private const val RESOURCE_PREFIX = ContentResolver.SCHEME_ANDROID_RESOURCE + "://"
    }

    @Throws(Exception::class)
    override fun init(context: Context, uri: Uri): Point {
        val uriString = uri.toString()
        if (uriString.startsWith(RESOURCE_PREFIX)) {
            val packageName = uri.authority
            val res: Resources = if (context.packageName == packageName) {
                context.resources
            } else {
                val pm: PackageManager = context.packageManager
                pm.getResourcesForApplication(packageName!!)
            }
            var id = 0
            val segments: List<String> = uri.pathSegments
            val size = segments.size
            if (size == 2 && segments[0] == "drawable") {
                val resName = segments[1]
                id = res.getIdentifier(resName, "drawable", packageName)
            } else if (size == 1 && TextUtils.isDigitsOnly(segments[0])) {
                try {
                    id = segments[0].toInt()
                } catch (_: NumberFormatException) {
                }
            }
            decoder = BitmapRegionDecoder.newInstance(context.resources.openRawResource(id), false)
        } else if (uriString.startsWith(ASSET_PREFIX)) {
            val assetName = uriString.substring(ASSET_PREFIX.length)
            decoder = BitmapRegionDecoder.newInstance(
                context.assets.open(assetName, AssetManager.ACCESS_RANDOM), false
            )
        } else if (uriString.startsWith(FILE_PREFIX)) {
            decoder = BitmapRegionDecoder.newInstance(uriString.substring(FILE_PREFIX.length), false)
        } else {
            var inputStream: InputStream? = null
            try {
                val contentResolver = context.contentResolver
                inputStream = contentResolver.openInputStream(uri)
                decoder = BitmapRegionDecoder.newInstance(inputStream, false)
            } finally {
                try {
                    inputStream?.close()
                } catch (_: Exception) {
                }
            }
        }
        val d = decoder ?: throw RuntimeException("Decoder initialization failed")
        return Point(d.width, d.height)
    }

    override fun decodeRegion(sRect: Rect, sampleSize: Int): Bitmap {
        synchronized(decoderLock) {
            val options = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Config.RGB_565
            }
            val bitmap = decoder?.decodeRegion(sRect, options)
            if (bitmap == null) {
                throw RuntimeException(
                    "Skia image decoder returned null bitmap - image format may not be supported"
                )
            }
            return bitmap
        }
    }

    override fun isReady(): Boolean {
        val d = decoder
        return d != null && !d.isRecycled
    }

    override fun recycle() {
        decoder?.recycle()
    }
}
