package com.cgfay.filter.glfilter.makeup.bean

/**
 * Types of makeup effects.
 */
enum class MakeupType(val typeName: String, val index: Int) {
    NONE("none", -1),
    SHADOW("shadow", 0),
    PUPIL("pupil", 1),
    EYESHADOW("eyeshadow", 2),
    EYELINER("eyeliner", 3),
    EYELASH("eyelash", 4),
    EYELID("eyelid", 5),
    EYEBROW("eyebrow", 6),
    BLUSH("blush", 7),
    LIPSTICK("lipstick", 8);

    companion object {
        fun getType(name: String): MakeupType = when (name) {
            "shadow" -> SHADOW
            "pupil" -> PUPIL
            "eyeshadow" -> EYESHADOW
            "eyeliner" -> EYELINER
            "eyelash" -> EYELASH
            "eyelid" -> EYELID
            "eyebrow" -> EYEBROW
            "blush" -> BLUSH
            "lipstick" -> LIPSTICK
            else -> NONE
        }

        fun getType(index: Int): MakeupType = when (index) {
            0 -> SHADOW
            1 -> PUPIL
            2 -> EYESHADOW
            3 -> EYELINER
            4 -> EYELASH
            5 -> EYELID
            6 -> EYEBROW
            7 -> BLUSH
            8 -> LIPSTICK
            else -> NONE
        }
    }

    object MakeupIndex {
        const val LipstickIndex = 0
        const val BlushIndex = 1
        const val ShadowIndex = 2
        const val EyebrowIndex = 3
        const val EyeshadowIndex = 4
        const val EyelinerIndex = 5
        const val EyelashIndex = 6
        const val EyelidIndex = 7
        const val PupilIndex = 8
        const val MakeupSize = 9
    }
}
