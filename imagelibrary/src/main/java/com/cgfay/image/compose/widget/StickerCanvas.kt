package com.cgfay.image.compose.widget

import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput

/**
 * Compose implementation of the original StickerView.
 * Supports drag, scale, rotate, flip via double tap and delete via long press.
 */
@Composable
fun StickerCanvas(
    bitmap: ImageBitmap,
    modifier: Modifier = Modifier,
    onDelete: () -> Unit = {},
    onFlipped: (Boolean) -> Unit = {}
) {
    var scale by remember { mutableStateOf(1f) }
    var rotation by remember { mutableStateOf(0f) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    var flipped by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, rotate ->
                    scale *= zoom
                    rotation += rotate
                    offsetX += pan.x
                    offsetY += pan.y
                }
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        flipped = !flipped
                        onFlipped(flipped)
                    },
                    onLongPress = { onDelete() }
                )
            }
    ) {
        Image(
            bitmap = bitmap,
            contentDescription = null,
            modifier = Modifier.graphicsLayer(
                translationX = offsetX,
                translationY = offsetY,
                scaleX = if (flipped) -scale else scale,
                scaleY = scale,
                rotationZ = rotation
            )
        )
    }
}
