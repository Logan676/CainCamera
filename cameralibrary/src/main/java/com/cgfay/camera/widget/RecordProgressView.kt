package com.cgfay.camera.widget

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawRoundRect
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun RecordProgressView(
    progress: Float,
    progressSegments: List<Float>,
    modifier: Modifier = Modifier,
    radius: Dp = 4.dp,
    backgroundColor: Color = Color(0x22000000),
    contentColor: Color = Color(0xFFFACE15),
    dividerColor: Color = Color.White,
    dividerWidth: Dp = 2.dp
) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val radiusPx = radius.toPx()
        val dividerPx = dividerWidth.toPx()

        // background
        drawRoundRect(
            color = backgroundColor,
            size = Size(width, height),
            cornerRadius = CornerRadius(radiusPx, radiusPx)
        )

        val totalProgress = progressSegments.sum() + progress
        val progressWidth = totalProgress * width
        drawRoundRect(
            color = contentColor,
            topLeft = Offset.Zero,
            size = Size(progressWidth, height),
            cornerRadius = CornerRadius(radiusPx, radiusPx)
        )
        if (progressWidth > radiusPx) {
            drawRect(
                color = contentColor,
                topLeft = Offset(radiusPx, 0f),
                size = Size(progressWidth - radiusPx, height)
            )
        }

        var left = 0f
        for (seg in progressSegments) {
            left += seg * width
            drawRect(
                color = dividerColor,
                topLeft = Offset(left - dividerPx, 0f),
                size = Size(dividerPx, height)
            )
        }
    }
}
