package com.cgfay.picker.widget.subsamplingview.decoder

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri

interface ImageDecoder {
    @Throws(Exception::class)
    fun decode(context: Context, uri: Uri): Bitmap
}
