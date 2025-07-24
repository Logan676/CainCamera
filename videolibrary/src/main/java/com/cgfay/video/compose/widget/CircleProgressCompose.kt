package com.cgfay.video.compose.widget

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Compose implementation of [com.cgfay.video.widget.CircleProgressView].
 */
@Composable
fun CircleProgress(
    progress: Float,
    modifier: Modifier = Modifier,
    circleColor: Color = Color.Black,
    backgroundColor: Color = Color.Gray,
    textColor: Color = Color.Black,
    circleWidth: Dp = 20.dp,
    backgroundStrokeWidth: Dp = 5.dp,
    startAngle: Float = -90f,
    isTextEnabled: Boolean = true,
    textPrefix: String = "",
    textSuffix: String = "",
    textSize: TextUnit = 20.sp
) {
    val stroke = with(LocalDensity.current) { circleWidth.toPx() }
    val bgStroke = with(LocalDensity.current) { backgroundStrokeWidth.toPx() }
    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val diameter = size.minDimension
            val topLeftX = (size.width - diameter) / 2f
            val topLeftY = (size.height - diameter) / 2f
            val rect = Rect(topLeftX + stroke / 2, topLeftY + stroke / 2,
                topLeftX + diameter - stroke / 2, topLeftY + diameter - stroke / 2)
            drawArc(
                color = backgroundColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = rect.topLeft,
                size = rect.size,
                style = Stroke(width = bgStroke)
            )
            drawArc(
                color = circleColor,
                startAngle = startAngle,
                sweepAngle = 360f * (progress.coerceIn(0f, 100f) / 100f),
                useCenter = false,
                topLeft = rect.topLeft,
                size = rect.size,
                style = Stroke(width = stroke)
            )
        }
        if (isTextEnabled) {
            Text(
                text = "$textPrefix${progress.toInt()}$textSuffix",
                color = textColor,
                fontSize = textSize,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}
