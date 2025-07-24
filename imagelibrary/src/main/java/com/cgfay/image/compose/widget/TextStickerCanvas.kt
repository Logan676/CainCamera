package com.cgfay.image.compose.widget

import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput

/**
 * Compose version of TextStickerView.
 * Provides basic drag, scale and rotate for text stickers.
 * TODO implement delete and edit callbacks like original view.
 */
@Composable
fun TextStickerCanvas(
    text: String,
    color: Color = Color.White,
    modifier: Modifier = Modifier
) {
    var scale by remember { mutableStateOf(1f) }
    var rotation by remember { mutableStateOf(0f) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }

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
    ) {
        Text(
            text = text,
            color = color,
            modifier = Modifier.graphicsLayer(
                translationX = offsetX,
                translationY = offsetY,
                scaleX = scale,
                scaleY = scale,
                rotationZ = rotation
            )
        )
    }
}
