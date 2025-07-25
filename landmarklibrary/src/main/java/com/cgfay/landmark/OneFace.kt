package com.cgfay.landmark

import android.util.SparseArray

/**
 * Data class holding landmark results for one face.
 */
class OneFace {
    var confidence: Float = 0f
    var pitch: Float = 0f
    var yaw: Float = 0f
    var roll: Float = 0f
    var age: Float = 0f
    var gender: Int = GENDER_MAN
    var vertexPoints: FloatArray? = null

    fun clone(): OneFace {
        val copy = OneFace()
        copy.confidence = confidence
        copy.pitch = pitch
        copy.yaw = yaw
        copy.roll = roll
        copy.age = age
        copy.gender = gender
        copy.vertexPoints = vertexPoints?.clone()
        return copy
    }

    companion object {
        const val GENDER_MAN = 0
        const val GENDER_WOMAN = 1

        @JvmStatic
        fun arrayCopy(origin: Array<OneFace>?): Array<OneFace>? {
            if (origin == null) return null
            return Array(origin.size) { i -> origin[i].clone() }
        }

        @JvmStatic
        fun arrayCopy(origin: SparseArray<OneFace>?): Array<OneFace>? {
            if (origin == null) return null
            return Array(origin.size()) { i -> origin.get(i).clone() }
        }
    }
}
