package com.cgfay.camera.loader

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Interface for loading and displaying media thumbnails with Compose.
 */
interface MediaLoader {

    /**
     * Display a thumbnail image using Compose.
     *
     * @param model data model for the image, typically a [String] path or [android.net.Uri].
     * @param modifier modifier applied to the image.
     * @param placeholder drawable resource used while loading the image.
     * @param radius corner radius for the image.
     */
    @Composable
    fun Thumbnail(
        model: Any,
        modifier: Modifier = Modifier,
        @DrawableRes placeholder: Int,
        radius: Dp = 0.dp
    )
}
