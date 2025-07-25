package com.cgfay.filter.glfilter.utils

/**
 * Based on GPUImage rotation helpers.
 */
enum class Rotation {
    NORMAL, ROTATION_90, ROTATION_180, ROTATION_270;

    fun asInt(): Int = when (this) {
        NORMAL -> 0
        ROTATION_90 -> 90
        ROTATION_180 -> 180
        ROTATION_270 -> 270
    }

    companion object {
        @JvmStatic
        fun fromInt(rotation: Int): Rotation = when (rotation) {
            0, 360 -> NORMAL
            90 -> ROTATION_90
            180 -> ROTATION_180
            270 -> ROTATION_270
            else -> throw IllegalStateException("$rotation is an unknown rotation. Needs to be either 0, 90, 180 or 270!")
        }
    }
}
