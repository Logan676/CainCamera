package com.cgfay.picker.loader

import android.content.Context
import android.net.Uri
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.annotation.NonNull

/**
 * 图片加载器
 */
interface MediaLoader {

    /**
     * 加载缩略图
     */
    fun loadThumbnail(
        @NonNull context: Context,
        @NonNull imageView: ImageView,
        @NonNull uri: Uri,
        @DrawableRes placeholder: Int,
        @DrawableRes error: Int
    )

    /**
     * 加载缩略图
     */
    fun loadThumbnail(
        @NonNull context: Context,
        @NonNull imageView: ImageView,
        @NonNull uri: Uri,
        resize: Int,
        @DrawableRes placeholder: Int,
        @DrawableRes error: Int
    )

    /**
     * 加载图片
     */
    fun loadImage(
        @NonNull context: Context,
        width: Int,
        height: Int,
        @NonNull imageView: ImageView,
        @NonNull uri: String
    )

    /**
     * 加载GIF缩略图
     */
    fun loadGifThumbnail(
        @NonNull context: Context,
        @NonNull imageView: ImageView,
        @NonNull uri: Uri,
        resize: Int,
        @DrawableRes placeholder: Int,
        @DrawableRes error: Int
    )

    /**
     * 加载GIF
     */
    fun loadGif(
        @NonNull context: Context,
        width: Int,
        height: Int,
        @NonNull imageView: ImageView,
        @NonNull uri: Uri
    )
}
