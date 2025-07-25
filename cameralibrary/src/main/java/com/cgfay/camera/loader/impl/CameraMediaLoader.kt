package com.cgfay.camera.loader.impl

import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import android.widget.ImageView
import androidx.annotation.NonNull
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.cgfay.camera.loader.MediaLoader

/**
 * Glide implementation of [MediaLoader].
 */
class CameraMediaLoader : MediaLoader {
    override fun loadThumbnail(
        @NonNull context: Context,
        imageView: ImageView,
        path: String,
        placeholder: Int,
        radius: Int
    ) {
        Glide.with(context)
            .asBitmap()
            .load(path)
            .apply(
                RequestOptions.bitmapTransform(RoundedCorners(radius))
                    .placeholder(placeholder)
                    .centerCrop()
            )
            .into(imageView)
    }

    override fun loadThumbnail(
        @NonNull context: Context,
        imageView: ImageView,
        path: Uri,
        placeholder: Int,
        radius: Int
    ) {
        Glide.with(context)
            .asBitmap()
            .load(path)
            .apply(
                RequestOptions.bitmapTransform(RoundedCorners(radius))
                    .placeholder(placeholder)
                    .centerCrop()
            )
            .into(imageView)
    }

    override fun loadThumbnail(
        @NonNull context: Context,
        imageView: ImageView,
        path: String,
        placeholder: Int
    ) {
        Glide.with(context)
            .asBitmap()
            .load(path)
            .apply(
                RequestOptions()
                    .placeholder(placeholder)
                    .centerCrop()
            )
            .into(imageView)
    }

    override fun loadThumbnail(
        context: Context,
        placeholder: Drawable,
        imageView: ImageView,
        uri: Uri
    ) {
        Glide.with(context)
            .asBitmap()
            .load(uri)
            .apply(
                RequestOptions()
                    .placeholder(placeholder)
                    .centerCrop()
            )
            .into(imageView)
    }

    override fun loadImage(
        context: Context,
        width: Int,
        height: Int,
        imageView: ImageView,
        uri: Uri
    ) {
        Glide.with(context)
            .load(uri)
            .apply(
                RequestOptions()
                    .override(width, height)
                    .priority(Priority.HIGH)
                    .fitCenter()
            )
            .into(imageView)
    }

    override fun loadGifThumbnail(
        context: Context,
        placeholder: Drawable,
        imageView: ImageView,
        uri: Uri
    ) {
        Glide.with(context)
            .asBitmap()
            .load(uri)
            .apply(
                RequestOptions()
                    .placeholder(placeholder)
                    .centerCrop()
            )
            .into(imageView)
    }

    override fun loadGif(
        context: Context,
        width: Int,
        height: Int,
        imageView: ImageView,
        uri: Uri
    ) {
        Glide.with(context)
            .asGif()
            .load(uri)
            .apply(
                RequestOptions()
                    .override(width, height)
                    .priority(Priority.HIGH)
                    .fitCenter()
            )
            .into(imageView)
    }
}
