package com.cgfay.camera.widget

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.rememberInfiniteTransition
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import kotlin.math.min

@Composable
fun RecordButton(
    modifier: Modifier = Modifier,
    recordEnabled: Boolean = true,
    onRecordStart: () -> Unit,
    onRecordStop: () -> Unit,
    onZoom: (Float) -> Unit = {}
) {
    var pressed by remember { mutableStateOf(false) }
    var layoutSize by remember { mutableStateOf(androidx.compose.ui.geometry.Size.Zero) }
    var initY by remember { mutableStateOf(0f) }
    var infectionPoint by remember { mutableStateOf(0f) }

    val minStroke = with(Density.current) { 3.dp.toPx() }
    val maxStroke = with(Density.current) { 12.dp.toPx() }
    val minCorner = with(Density.current) { 5.dp.toPx() }

    val maxRectWidth by remember(layoutSize) { mutableStateOf(layoutSize.width / 3f) }
    val minRectWidth by remember(maxRectWidth) { mutableStateOf(maxRectWidth * 0.6f) }
    val minCircleRadius by remember(maxRectWidth) { mutableStateOf(maxRectWidth / 2 + minStroke + with(Density.current) { 5.dp.toPx() }) }
    val maxCircleRadius by remember(layoutSize) { mutableStateOf(min(layoutSize.width, layoutSize.height) / 2f - maxStroke) }
    val maxCorner by remember(maxRectWidth) { mutableStateOf(maxRectWidth / 2f) }

    val corner by animateFloatAsState(targetValue = if (pressed) minCorner else maxCorner, animationSpec = tween(500))
    val rectWidth by animateFloatAsState(targetValue = if (pressed) minRectWidth else maxRectWidth, animationSpec = tween(500))
    val circleRadius by animateFloatAsState(targetValue = if (pressed) maxCircleRadius else minCircleRadius, animationSpec = tween(500))

    val infiniteTransition = rememberInfiniteTransition()
    val strokeWidth by if (pressed) {
        infiniteTransition.animateFloat(
            initialValue = minStroke,
            targetValue = maxStroke,
            animationSpec = infiniteRepeatable(tween(1200), RepeatMode.Reverse)
        )
    } else {
        mutableStateOf(minStroke)
    }

    Box(
        modifier = modifier
            .pointerInput(recordEnabled) {
                if (recordEnabled) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            pressed = true
                            initY = offset.y
                            infectionPoint = offset.y
                            onRecordStart()
                        },
                        onDragEnd = {
                            pressed = false
                            onRecordStop()
                        },
                        onDragCancel = {
                            pressed = false
                            onRecordStop()
                        },
                        onDrag = { change, _ ->
                            val percent = ((infectionPoint - change.position.y) / initY).coerceIn(0f, 1f)
                            infectionPoint = change.position.y
                            onZoom(percent)
                        }
                    )
                }
            }
            .fillMaxSize()
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            layoutSize = Size(size.width, size.height)
            val centerX = size.width / 2f
            val centerY = size.height / 2f

            drawIntoCanvas { canvas ->
                val paint = androidx.compose.ui.graphics.Paint().apply {
                    color = Color(0x33FFFFFF)
                    this.strokeWidth = strokeWidth
                    style = androidx.compose.ui.graphics.PaintingStyle.Fill
                }
                val rectPaint = androidx.compose.ui.graphics.Paint().apply {
                    color = Color.White
                }
                canvas.drawCircle(centerX, centerY, circleRadius, paint)
                // transparent hole
                val transparent = androidx.compose.ui.graphics.Paint().apply {
                    color = Color.Black
                    blendMode = androidx.compose.ui.graphics.BlendMode.Clear
                }
                canvas.drawCircle(centerX, centerY, circleRadius - strokeWidth, transparent)
                val left = centerX - rectWidth / 2
                val top = centerY - rectWidth / 2
                val right = centerX + rectWidth / 2
                val bottom = centerY + rectWidth / 2
                canvas.drawRoundRect(Rect(left, top, right, bottom), corner, corner, rectPaint)
            }
        }
    }
}
