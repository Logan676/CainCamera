package com.cgfay.media.compose

import android.view.SurfaceView
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun VideoPreview(modifier: Modifier = Modifier, onSurface: (SurfaceView) -> Unit) {
    AndroidView(
        factory = { context ->
            SurfaceView(context).also(onSurface)
        },
        modifier = modifier
    )
}
