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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity

/**
 * Crop cover implemented with Jetpack Compose Canvas.
 * Supports dragging, resizing and optional aspect ratio locking similar to
 * the original CropCoverView.
 */
@Composable
fun CropCover(
    modifier: Modifier = Modifier,
    strokeColor: Color = Color.White,
    strokeWidth: Float = 2f,
    aspectRatio: Float = -1f,
    onRectChange: (Rect) -> Unit = {}
) {
    BoxWithConstraints(modifier = modifier) {
        val density = LocalDensity.current
        val widthPx = with(density) { maxWidth.toPx() }
        val heightPx = with(density) { maxHeight.toPx() }
        val touchSlop = with(density) { 24f }

        var rect by remember {
            mutableStateOf(
                Rect(
                    Offset(widthPx * 0.1f, heightPx * 0.1f),
                    Offset(widthPx * 0.9f, heightPx * 0.9f)
                )
            )
        }

        var mode by remember { mutableStateOf(DragMode.None) }

        fun enforceRatio() {
            if (aspectRatio <= 0f) return
            val width = rect.width
            val height = width / aspectRatio
            val bottom = rect.top + height
            rect = Rect(rect.topLeft, Offset(rect.right, bottom.coerceAtMost(heightPx)))
        }

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            mode = when {
                                offset.isNear(rect.topLeft, touchSlop) -> DragMode.TopLeft
                                offset.isNear(rect.bottomLeft, touchSlop) -> DragMode.BottomLeft
                                offset.isNear(rect.topRight, touchSlop) -> DragMode.TopRight
                                offset.isNear(rect.bottomRight, touchSlop) -> DragMode.BottomRight
                                kotlin.math.abs(offset.x - rect.left) < touchSlop -> DragMode.Left
                                kotlin.math.abs(offset.x - rect.right) < touchSlop -> DragMode.Right
                                kotlin.math.abs(offset.y - rect.top) < touchSlop -> DragMode.Top
                                kotlin.math.abs(offset.y - rect.bottom) < touchSlop -> DragMode.Bottom
                                rect.contains(offset) -> DragMode.Move
                                else -> DragMode.None
                            }
                        },
                        onDragEnd = {
                            mode = DragMode.None
                        }
                    ) { change, drag ->
                        change.consume()
                        var newRect = rect
                        when (mode) {
                            DragMode.Move -> {
                                val newLeft = (rect.left + drag.x).coerceIn(0f, widthPx - rect.width)
                                val newTop = (rect.top + drag.y).coerceIn(0f, heightPx - rect.height)
                                newRect = Rect(Offset(newLeft, newTop), rect.size)
                            }
                            DragMode.Left -> {
                                val left = (rect.left + drag.x).coerceIn(0f, rect.right - touchSlop)
                                newRect = Rect(Offset(left, rect.top), Offset(rect.right, rect.bottom))
                                enforceRatio()
                            }
                            DragMode.Right -> {
                                val right = (rect.right + drag.x).coerceIn(rect.left + touchSlop, widthPx)
                                newRect = Rect(rect.topLeft, Offset(right, rect.bottom))
                                enforceRatio()
                            }
                            DragMode.Top -> {
                                val top = (rect.top + drag.y).coerceIn(0f, rect.bottom - touchSlop)
                                newRect = Rect(Offset(rect.left, top), Offset(rect.right, rect.bottom))
                                if (aspectRatio > 0f) {
                                    val height = rect.bottom - top
                                    val width = height * aspectRatio
                                    val right = rect.left + width
                                    newRect = Rect(Offset(rect.left, top), Offset(right.coerceAtMost(widthPx), rect.bottom))
                                }
                            }
                            DragMode.Bottom -> {
                                val bottom = (rect.bottom + drag.y).coerceIn(rect.top + touchSlop, heightPx)
                                newRect = Rect(rect.topLeft, Offset(rect.right, bottom))
                                if (aspectRatio > 0f) {
                                    val height = bottom - rect.top
                                    val width = height * aspectRatio
                                    val right = rect.left + width
                                    newRect = Rect(rect.topLeft, Offset(right.coerceAtMost(widthPx), bottom))
                                }
                            }
                            DragMode.TopLeft -> {
                                val left = (rect.left + drag.x).coerceIn(0f, rect.right - touchSlop)
                                val top = (rect.top + drag.y).coerceIn(0f, rect.bottom - touchSlop)
                                newRect = Rect(Offset(left, top), rect.bottomRight)
                                enforceRatio()
                            }
                            DragMode.TopRight -> {
                                val right = (rect.right + drag.x).coerceIn(rect.left + touchSlop, widthPx)
                                val top = (rect.top + drag.y).coerceIn(0f, rect.bottom - touchSlop)
                                newRect = Rect(Offset(rect.left, top), Offset(right, rect.bottom))
                                enforceRatio()
                            }
                            DragMode.BottomLeft -> {
                                val left = (rect.left + drag.x).coerceIn(0f, rect.right - touchSlop)
                                val bottom = (rect.bottom + drag.y).coerceIn(rect.top + touchSlop, heightPx)
                                newRect = Rect(Offset(left, rect.top), Offset(rect.right, bottom))
                                enforceRatio()
                            }
                            DragMode.BottomRight -> {
                                val right = (rect.right + drag.x).coerceIn(rect.left + touchSlop, widthPx)
                                val bottom = (rect.bottom + drag.y).coerceIn(rect.top + touchSlop, heightPx)
                                newRect = Rect(rect.topLeft, Offset(right, bottom))
                                enforceRatio()
                            }
                            else -> {}
                        }
                        rect = newRect
                        onRectChange(newRect)
                    }
                }
        ) {
            drawRect(
                color = strokeColor,
                topLeft = rect.topLeft,
                size = rect.size,
                style = Stroke(width = strokeWidth)
            )
            val thirdW = rect.width / 3f
            val thirdH = rect.height / 3f
            for (i in 1..2) {
                val x = rect.left + thirdW * i
                drawLine(strokeColor, Offset(x, rect.top), Offset(x, rect.bottom), strokeWidth)
                val y = rect.top + thirdH * i
                drawLine(strokeColor, Offset(rect.left, y), Offset(rect.right, y), strokeWidth)
            }
        }
    }
}

private fun Offset.isNear(other: Offset, threshold: Float): Boolean {
    return kotlin.math.abs(x - other.x) <= threshold && kotlin.math.abs(y - other.y) <= threshold
}

private enum class DragMode {
    None, Move, Left, Right, Top, Bottom, TopLeft, TopRight, BottomLeft, BottomRight
}

