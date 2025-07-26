package com.cgfay.video.bean

import androidx.compose.runtime.Immutable

/**
 * Speed enumeration for video playback.
 */
@Immutable
enum class VideoSpeed(val type: Int, val speed: Float) {
    SPEED_L1(-1, 0.5f),
    SPEED_L2(0, 1.0f),
    SPEED_L3(1, 2.0f);
}
