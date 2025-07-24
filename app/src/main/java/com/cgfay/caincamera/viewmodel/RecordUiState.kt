package com.cgfay.caincamera.viewmodel

import android.graphics.SurfaceTexture

/**
 * UI state for record screens.
 */
data class RecordUiState(
    val showViews: Boolean = true,
    val progress: Float = 0f,
    val progressSegments: List<Float> = emptyList(),
    val surfaceTexture: SurfaceTexture? = null,
    val textureSize: Pair<Int, Int>? = null,
    val showDialog: Boolean = false,
    val toast: String? = null,
    val frameAvailable: Boolean = false
)
