package com.cgfay.video.compose.widget

import android.view.TextureView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.matchParentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

/**
 * Compose implementation of a video preview TextureView.
 */
@Composable
fun VideoTexture(
    modifier: Modifier = Modifier,
    videoWidth: Int = 0,
    videoHeight: Int = 0,
    rotation: Float = 0f
) {
    val aspect = if (videoWidth > 0 && videoHeight > 0) {
        val width = if (rotation == 90f || rotation == 270f) videoHeight else videoWidth
        val height = if (rotation == 90f || rotation == 270f) videoWidth else videoHeight
        width.toFloat() / height.toFloat()
    } else 1f

    Box(modifier = modifier.then(Modifier.aspectRatio(aspect))) {
        AndroidView(
            factory = { context -> TextureView(context) },
            modifier = Modifier.matchParentSize(),
            update = { view ->
                view.rotation = rotation
            }
        )
    }
}
