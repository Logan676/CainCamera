package com.cgfay.picker.widget

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Composable that maintains a square aspect ratio for its content.
 */
@Composable
fun SquareBox(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(modifier = modifier.aspectRatio(1f)) {
        content()
    }
}
