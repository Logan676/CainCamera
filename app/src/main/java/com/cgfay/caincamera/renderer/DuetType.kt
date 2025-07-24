package com.cgfay.caincamera.renderer

/**
 * Same-frame duet modes.
 */
enum class DuetType(val value: Int) {
    /** No duet applied */
    DUET_TYPE_NONE(0),

    /** Split screen left-right */
    DUET_TYPE_LEFT_RIGHT(1),

    /** Split screen top-bottom */
    DUET_TYPE_UP_DOWN(2),

    /** Picture-in-picture */
    DUET_TYPE_BIG_SMALL(3);

    companion object {
        fun valueOf(value: Int): DuetType = when (value) {
            1 -> DUET_TYPE_LEFT_RIGHT
            2 -> DUET_TYPE_UP_DOWN
            3 -> DUET_TYPE_BIG_SMALL
            else -> DUET_TYPE_NONE
        }
    }
}
