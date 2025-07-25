package com.cgfay.camera.widget

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged

/**
 * Composable replacement for [CameraMeasureFrameLayout]. It reports its size
 * whenever it is measured.
 */
@Composable
fun CameraMeasureBox(
    modifier: Modifier = Modifier,
    onMeasure: (width: Int, height: Int) -> Unit,
    content: @Composable BoxScope.() -> Unit = {}
) {
    Box(modifier = modifier.onSizeChanged { size ->
        onMeasure(size.width, size.height)
    }) {
        content()
    }
}
