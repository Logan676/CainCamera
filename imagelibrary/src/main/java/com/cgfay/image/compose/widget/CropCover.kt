package com.cgfay.image.compose.widget

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.input.pointer.pointerInput

/**
 * Simple crop cover implemented with Jetpack Compose Canvas.
 * Supports dragging of the crop area. Resizing and ratio handling are TODO.
 */
@Composable
fun CropCover(
    modifier: Modifier = Modifier,
    strokeColor: Color = Color.White,
    strokeWidth: Float = 2f
) {
    BoxWithConstraints(modifier = modifier) {
        val widthPx = with(LocalDensity.current) { maxWidth.toPx() }
        val heightPx = with(LocalDensity.current) { maxHeight.toPx() }
        var rect by remember {
            mutableStateOf(
                Rect(
                    Offset(widthPx * 0.1f, heightPx * 0.1f),
                    Offset(widthPx * 0.9f, heightPx * 0.9f)
                )
            )
        }
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures { change, drag ->
                        change.consume()
                        rect = rect.translate(drag)
                    }
                }
        ) {
            drawRect(
                color = strokeColor,
                topLeft = rect.topLeft,
                size = rect.size,
                style = Stroke(width = strokeWidth)
            )
            // TODO draw grid lines and corners similar to original view
        }
    }
}

private fun Rect.translate(offset: Offset) = Rect(topLeft + offset, bottomRight + offset)
