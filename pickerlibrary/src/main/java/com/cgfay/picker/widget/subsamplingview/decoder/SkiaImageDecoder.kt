package com.cgfay.picker.widget.subsamplingview.decoder

import android.content.ContentResolver
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.text.TextUtils
import java.io.InputStream

class SkiaImageDecoder : ImageDecoder {

    companion object {
        private const val FILE_PREFIX = "file://"
        private const val ASSET_PREFIX = "$FILE_PREFIX/android_asset/"
        private const val RESOURCE_PREFIX = ContentResolver.SCHEME_ANDROID_RESOURCE + "://"
    }

    @Throws(Exception::class)
    override fun decode(context: Context, uri: Uri): Bitmap {
        val uriString = uri.toString()
        val options = BitmapFactory.Options()
        options.inPreferredConfig = Bitmap.Config.RGB_565
        val bitmap: Bitmap? = when {
            uriString.startsWith(RESOURCE_PREFIX) -> {
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
                BitmapFactory.decodeResource(context.resources, id, options)
            }
            uriString.startsWith(ASSET_PREFIX) -> {
                val assetName = uriString.substring(ASSET_PREFIX.length)
                BitmapFactory.decodeStream(context.assets.open(assetName), null, options)
            }
            uriString.startsWith(FILE_PREFIX) -> {
                BitmapFactory.decodeFile(uriString.substring(FILE_PREFIX.length), options)
            }
            else -> {
                var inputStream: InputStream? = null
                try {
                    val contentResolver = context.contentResolver
                    inputStream = contentResolver.openInputStream(uri)
                    BitmapFactory.decodeStream(inputStream, null, options)
                } finally {
                    try {
                        inputStream?.close()
                    } catch (_: Exception) {
                    }
                }
            }
        }
        return bitmap ?: throw RuntimeException(
            "Skia image region decoder returned null bitmap - image format may not be supported"
        )
    }
}
