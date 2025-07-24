package com.cgfay.video.compose.widget

import androidx.compose.material.RangeSlider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

/**
 * Simplified compose version of [com.cgfay.video.widget.VideoCutViewBar].
 */
@Composable
fun VideoCutBar(
    range: MutableState<ClosedFloatingPointRange<Float>> = remember { mutableStateOf(0f..1f) },
    max: Float = 1f,
    modifier: Modifier = Modifier,
    onRangeChange: (ClosedFloatingPointRange<Float>) -> Unit = {}
) {
    RangeSlider(
        values = range.value,
        onValueChange = {
            range.value = it
            onRangeChange(it)
        },
        valueRange = 0f..max,
        modifier = modifier
    )
}
