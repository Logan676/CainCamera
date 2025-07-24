package com.cgfay.widget.compose

import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Represents a tab item for [CameraTabRow].
 */
data class TabItem(
    val title: String,
    val icon: ImageVector? = null
)
