package com.cgfay.picker

import android.content.Context
import android.net.Uri
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.imageLoader
import coil.request.ImageRequest
import coil.size.Size
import com.cgfay.picker.loader.MediaLoader

/**
 * Coil based implementation supporting both View and Compose loading.
 */
class PickerMediaLoader : MediaLoader {

    override fun loadThumbnail(
        context: Context,
        imageView: ImageView,
        uri: Uri,
        @DrawableRes placeholder: Int,
        @DrawableRes error: Int
    ) {
        val request = ImageRequest.Builder(context)
            .data(uri)
            .placeholder(placeholder)
            .error(error)
            .size(Size.ORIGINAL)
            .target(imageView)
            .build()
        context.imageLoader.enqueue(request)
    }

    override fun loadThumbnail(
        context: Context,
        imageView: ImageView,
        uri: Uri,
        resize: Int,
        @DrawableRes placeholder: Int,
        @DrawableRes error: Int
    ) {
        val request = ImageRequest.Builder(context)
            .data(uri)
            .placeholder(placeholder)
            .error(error)
            .size(resize)
            .target(imageView)
            .build()
        context.imageLoader.enqueue(request)
    }

    override fun loadImage(
        context: Context,
        width: Int,
        height: Int,
        imageView: ImageView,
        uri: String
    ) {
        val request = ImageRequest.Builder(context)
            .data(uri)
            .size(width, height)
            .target(imageView)
            .build()
        context.imageLoader.enqueue(request)
    }

    override fun loadGifThumbnail(
        context: Context,
        imageView: ImageView,
        uri: Uri,
        resize: Int,
        @DrawableRes placeholder: Int,
        @DrawableRes error: Int
    ) {
        val request = ImageRequest.Builder(context)
            .data(uri)
            .placeholder(placeholder)
            .error(error)
            .size(resize)
            .target(imageView)
            .build()
        context.imageLoader.enqueue(request)
    }

    override fun loadGif(
        context: Context,
        width: Int,
        height: Int,
        imageView: ImageView,
        uri: Uri
    ) {
        val request = ImageRequest.Builder(context)
            .data(uri)
            .size(width, height)
            .target(imageView)
            .build()
        context.imageLoader.enqueue(request)
    }

    /**
     * Compose helper for displaying thumbnails.
     */
    @Composable
    fun Thumbnail(
        model: Any,
        modifier: Modifier = Modifier,
        @DrawableRes placeholder: Int,
        radius: Dp = 0.dp
    ) {
        AsyncImage(
            model = model,
            contentDescription = null,
            placeholder = painterResource(id = placeholder),
            contentScale = ContentScale.Crop,
            modifier = modifier.clip(RoundedCornerShape(radius))
        )
    }
}

