package com.cgfay.video.compose.widget

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

/**
 * Simplified compose version of [com.cgfay.video.widget.WaveCutView].
 */
@Composable
fun WaveCutView(
    progress: MutableState<Float> = remember { mutableStateOf(0f) },
    maxCount: Int = 50,
    selectedCount: Int = 15,
    modifier: Modifier = Modifier,
    onDragFinished: (Float) -> Unit = {}
) {
    val heights = listOf(20,27,23,34,42,36,32,41,21,27,16)
    Box(modifier = modifier.background(Color.Transparent)) {
        Canvas(modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    progress.value = (progress.value + dragAmount.x).coerceIn(0f, size.width.toFloat())
                    change.consume()
                }
            }) {
            val waveWidth = size.width / 45f
            val waveMargin = waveWidth / 4f
            val centerY = size.height / 2
            heights.forEachIndexed { i, h ->
                val index = i % heights.size
                val height = h.dp.toPx()
                val left = waveWidth * i + 0f
                drawRect(
                    color = if (left < progress.value && left + waveWidth - waveMargin < progress.value + (size.width * selectedCount / maxCount)) MaterialTheme.colors.primary else Color.White.copy(alpha = 0.5f),
                    topLeft = Offset(left + waveMargin/2, centerY - height/2),
                    size = androidx.compose.ui.geometry.Size(waveWidth - waveMargin, height)
                )
            }
        }
    }
}
