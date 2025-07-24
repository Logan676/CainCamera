package com.cgfay.video.compose.widget

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.cgfay.video.widget.VideoTextureView

/**
 * Compose wrapper for [VideoTextureView].
 */
@Composable
fun VideoTexture(
    modifier: Modifier = Modifier,
    videoWidth: Int = 0,
    videoHeight: Int = 0,
    rotation: Float = 0f
) {
    AndroidView(
        factory = { context -> VideoTextureView(context) },
        modifier = modifier,
        update = { view ->
            view.setVideoSize(videoWidth, videoHeight)
            view.rotation = rotation
        }
    )
}
