package com.cgfay.camera.loader.impl

import androidx.annotation.DrawableRes
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import coil.compose.AsyncImage
import com.cgfay.camera.loader.MediaLoader

/**
 * Glide based implementation for Compose image loading.
 */
class CameraMediaLoader : MediaLoader {

    @Composable
    override fun Thumbnail(
        model: Any,
        modifier: Modifier,
        @DrawableRes placeholder: Int,
        radius: Dp
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
