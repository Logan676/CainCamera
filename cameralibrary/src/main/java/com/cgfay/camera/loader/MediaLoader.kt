package com.cgfay.camera.loader

import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.annotation.NonNull

/**
 * Image loading interface used by the camera module.
 */
interface MediaLoader {
    fun loadThumbnail(
        @NonNull context: Context,
        imageView: ImageView,
        path: String,
        @DrawableRes placeholder: Int,
        radius: Int
    )

    fun loadThumbnail(
        @NonNull context: Context,
        imageView: ImageView,
        path: Uri,
        @DrawableRes placeholder: Int,
        radius: Int
    )

    fun loadThumbnail(
        @NonNull context: Context,
        imageView: ImageView,
        path: String,
        @DrawableRes placeholder: Int
    )

    fun loadThumbnail(
        context: Context,
        placeholder: Drawable,
        imageView: ImageView,
        uri: Uri
    )

    fun loadImage(
        context: Context,
        width: Int,
        height: Int,
        imageView: ImageView,
        uri: Uri
    )

    fun loadGifThumbnail(
        context: Context,
        placeholder: Drawable,
        imageView: ImageView,
        uri: Uri
    )

    fun loadGif(
        context: Context,
        width: Int,
        height: Int,
        imageView: ImageView,
        uri: Uri
    )
}
