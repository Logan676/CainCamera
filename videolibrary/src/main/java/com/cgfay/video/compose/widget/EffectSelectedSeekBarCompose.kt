package com.cgfay.video.compose.widget

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Slider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.cgfay.video.bean.EffectDuration

/**
 * Compose variant of [com.cgfay.video.widget.EffectSelectedSeekBar].
 */
@Composable
fun EffectSelectedSeekBar(
    progress: MutableState<Float> = remember { mutableStateOf(0f) },
    max: Float = 1f,
    effects: List<EffectDuration> = emptyList(),
    modifier: Modifier = Modifier,
    onProgressChanged: (Float) -> Unit = {}
) {
    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxWidth().align(Alignment.Center)) {
            val barHeight = 4.dp.toPx()
            // background line
            drawLine(
                color = Color.White.copy(alpha = 0.5f),
                start = androidx.compose.ui.geometry.Offset(0f, barHeight / 2),
                end = androidx.compose.ui.geometry.Offset(size.width, barHeight / 2),
                strokeWidth = barHeight
            )
            // draw effect ranges
            effects.forEach { effect ->
                val start = (effect.start / max) * size.width
                val end = (effect.end / max) * size.width
                drawLine(
                    color = Color(effect.color),
                    start = androidx.compose.ui.geometry.Offset(start, barHeight / 2),
                    end = androidx.compose.ui.geometry.Offset(end, barHeight / 2),
                    strokeWidth = barHeight
                )
            }
        }
        Slider(
            value = progress.value,
            onValueChange = {
                progress.value = it
                onProgressChanged(it)
            },
            valueRange = 0f..max,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
