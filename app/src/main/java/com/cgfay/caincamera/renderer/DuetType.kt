package com.cgfay.caincamera.renderer

/**
 * 同框类型
 */
enum class DuetType(val value: Int) {
    DUET_TYPE_NONE(0),          // 没有同框
    DUET_TYPE_LEFT_RIGHT(1),    // 左右同框
    DUET_TYPE_UP_DOWN(2),       // 上下同框
    DUET_TYPE_BIG_SMALL(3);     // 大小同框

    companion object {
        @JvmStatic
        fun valueOf(value: Int): DuetType = when (value) {
            1 -> DUET_TYPE_LEFT_RIGHT
            2 -> DUET_TYPE_UP_DOWN
            3 -> DUET_TYPE_BIG_SMALL
            else -> DUET_TYPE_NONE
        }
    }
}
