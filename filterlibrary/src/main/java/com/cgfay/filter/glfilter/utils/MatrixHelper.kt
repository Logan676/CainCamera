package com.cgfay.filter.glfilter.utils

import kotlin.math.tan

/**
 * Utility to create perspective projection matrices.
 */
object MatrixHelper {
    @JvmStatic
    fun perspectiveM(
        m: FloatArray,
        offset: Int,
        fovy: Float,
        aspect: Float,
        zNear: Float,
        zFar: Float
    ) {
        val f = 1.0f / tan(fovy * (Math.PI / 360.0)).toFloat()
        val rangeReciprocal = 1.0f / (zNear - zFar)
        m[offset + 0] = f / aspect
        m[offset + 1] = 0f
        m[offset + 2] = 0f
        m[offset + 3] = 0f
        m[offset + 4] = 0f
        m[offset + 5] = f
        m[offset + 6] = 0f
        m[offset + 7] = 0f
        m[offset + 8] = 0f
        m[offset + 9] = 0f
        m[offset + 10] = (zFar + zNear) * rangeReciprocal
        m[offset + 11] = -1f
        m[offset + 12] = 0f
        m[offset + 13] = 0f
        m[offset + 14] = 2f * zFar * zNear * rangeReciprocal
        m[offset + 15] = 0f
    }
}
