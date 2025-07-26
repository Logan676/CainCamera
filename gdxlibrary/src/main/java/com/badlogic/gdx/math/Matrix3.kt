package com.badlogic.gdx.math

import androidx.compose.runtime.Stable
import android.opengl.Matrix
import java.io.Serializable

@Stable
class Matrix3() : Serializable {
    companion object {
        const val M00 = 0
        const val M01 = 3
        const val M02 = 6
        const val M10 = 1
        const val M11 = 4
        const val M12 = 7
        const val M20 = 2
        const val M21 = 5
        const val M22 = 8
    }

    val `val`: FloatArray = FloatArray(9)
    private val tmp = FloatArray(9)

    init { idt() }

    fun idt(): Matrix3 {
        `val`[M00] = 1f; `val`[M01] = 0f; `val`[M02] = 0f
        `val`[M10] = 0f; `val`[M11] = 1f; `val`[M12] = 0f
        `val`[M20] = 0f; `val`[M21] = 0f; `val`[M22] = 1f
        return this
    }

    fun set(matrix: Matrix3): Matrix3 { System.arraycopy(matrix.`val`,0,`val`,0,9); return this }

    fun set(values: FloatArray): Matrix3 { System.arraycopy(values,0,`val`,0,9); return this }

    fun mul(m: Matrix3): Matrix3 {
        val matA = `val`
        val matB = m.`val`
        val v00 = matA[M00]*matB[M00] + matA[M01]*matB[M10] + matA[M02]*matB[M20]
        val v01 = matA[M00]*matB[M01] + matA[M01]*matB[M11] + matA[M02]*matB[M21]
        val v02 = matA[M00]*matB[M02] + matA[M01]*matB[M12] + matA[M02]*matB[M22]
        val v10 = matA[M10]*matB[M00] + matA[M11]*matB[M10] + matA[M12]*matB[M20]
        val v11 = matA[M10]*matB[M01] + matA[M11]*matB[M11] + matA[M12]*matB[M21]
        val v12 = matA[M10]*matB[M02] + matA[M11]*matB[M12] + matA[M12]*matB[M22]
        val v20 = matA[M20]*matB[M00] + matA[M21]*matB[M10] + matA[M22]*matB[M20]
        val v21 = matA[M20]*matB[M01] + matA[M21]*matB[M11] + matA[M22]*matB[M21]
        val v22 = matA[M20]*matB[M02] + matA[M21]*matB[M12] + matA[M22]*matB[M22]
        matA[M00]=v00; matA[M01]=v01; matA[M02]=v02
        matA[M10]=v10; matA[M11]=v11; matA[M12]=v12
        matA[M20]=v20; matA[M21]=v21; matA[M22]=v22
        return this
    }

    fun setToTranslation(x: Float, y: Float): Matrix3 {
        idt()
        `val`[M02] = x
        `val`[M12] = y
        return this
    }

    fun setToScaling(scaleX: Float, scaleY: Float): Matrix3 {
        idt()
        `val`[M00] = scaleX
        `val`[M11] = scaleY
        return this
    }

    fun setToRotation(degrees: Float): Matrix3 {
        val rad = degrees * MathUtils.degreesToRadians
        val cos = kotlin.math.cos(rad)
        val sin = kotlin.math.sin(rad)
        idt()
        `val`[M00] = cos
        `val`[M01] = -sin
        `val`[M10] = sin
        `val`[M11] = cos
        return this
    }
}
