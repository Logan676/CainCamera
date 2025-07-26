package com.cgfay.media.recorder

import androidx.compose.runtime.Immutable

/**
 * Speed mode used for recording.
 */
@Immutable
enum class SpeedMode(val type: Int, val speed: Float) {
    MODE_EXTRA_SLOW(1, 1f / 3f),
    MODE_SLOW(2, 0.5f),
    MODE_NORMAL(3, 1.0f),
    MODE_FAST(4, 2.0f),
    MODE_EXTRA_FAST(5, 3.0f);

    companion object {
        @JvmStatic
        fun valueOf(type: Int): SpeedMode = when (type) {
            1 -> MODE_EXTRA_SLOW
            2 -> MODE_SLOW
            3 -> MODE_NORMAL
            4 -> MODE_FAST
            5 -> MODE_EXTRA_FAST
            else -> MODE_NORMAL
        }

        @JvmStatic
        fun valueOf(speed: Float): SpeedMode = when (speed) {
            1f / 3f -> MODE_EXTRA_SLOW
            0.5f -> MODE_SLOW
            1.0f -> MODE_NORMAL
            2.0f -> MODE_FAST
            3.0f -> MODE_EXTRA_FAST
            else -> MODE_NORMAL
        }
    }
}
