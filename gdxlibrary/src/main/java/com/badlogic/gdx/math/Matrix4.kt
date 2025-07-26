package com.badlogic.gdx.math

import androidx.compose.runtime.Stable
import android.opengl.Matrix
import java.io.Serializable

@Stable
class Matrix4() : Serializable {
    companion object {
        const val M00 = 0
        const val M01 = 4
        const val M02 = 8
        const val M03 = 12
        const val M10 = 1
        const val M11 = 5
        const val M12 = 9
        const val M13 = 13
        const val M20 = 2
        const val M21 = 6
        const val M22 = 10
        const val M23 = 14
        const val M30 = 3
        const val M31 = 7
        const val M32 = 11
        const val M33 = 15

        @JvmStatic external fun mul(mata: FloatArray, matb: FloatArray)
        @JvmStatic external fun mulVec(mat: FloatArray, vec: FloatArray)
        @JvmStatic external fun mulVec(mat: FloatArray, vecs: FloatArray, offset: Int, numVecs: Int, stride: Int)
        @JvmStatic external fun prj(mat: FloatArray, vec: FloatArray)
        @JvmStatic external fun prj(mat: FloatArray, vecs: FloatArray, offset: Int, numVecs: Int, stride: Int)
        @JvmStatic external fun rot(mat: FloatArray, vec: FloatArray)
        @JvmStatic external fun rot(mat: FloatArray, vecs: FloatArray, offset: Int, numVecs: Int, stride: Int)
        @JvmStatic external fun inv(values: FloatArray): Boolean
        @JvmStatic external fun det(values: FloatArray): Float
    }

    val `val`: FloatArray = FloatArray(16)
    private val tmp = FloatArray(16)

    init { idt() }

    fun idt(): Matrix4 { Matrix.setIdentityM(`val`,0); return this }

    fun set(matrix: Matrix4): Matrix4 { System.arraycopy(matrix.`val`,0,`val`,0,16); return this }
    fun set(values: FloatArray): Matrix4 { System.arraycopy(values,0,`val`,0,16); return this }

    fun mul(matrix: Matrix4): Matrix4 {
        Matrix.multiplyMM(tmp,0,`val`,0,matrix.`val`,0)
        System.arraycopy(tmp,0,`val`,0,16)
        return this
    }

    fun translate(x: Float, y: Float, z: Float): Matrix4 { Matrix.translateM(`val`,0,x,y,z); return this }
    fun scale(x: Float, y: Float, z: Float): Matrix4 { Matrix.scaleM(`val`,0,x,y,z); return this }
    fun rotate(axis: Vector3, degrees: Float): Matrix4 { Matrix.rotateM(`val`,0,degrees,axis.x,axis.y,axis.z); return this }

    fun setToTranslation(x: Float, y: Float, z: Float): Matrix4 { idt(); translate(x,y,z); return this }

    fun setToOrtho2D(x: Float, y: Float, width: Float, height: Float): Matrix4 =
        setToOrtho(x, x + width, y, y + height, 0f, 1f)

    fun setToOrtho(left: Float, right: Float, bottom: Float, top: Float, near: Float, far: Float): Matrix4 {
        Matrix.orthoM(`val`,0,left,right,bottom,top,near,far)
        return this
    }

    fun setToLookAt(position: Vector3, target: Vector3, up: Vector3): Matrix4 {
        Matrix.setLookAtM(`val`,0,position.x,position.y,position.z,target.x,target.y,target.z,up.x,up.y,up.z)
        return this
    }

    fun inv(): Matrix4 { Matrix.invertM(tmp,0,`val`,0); System.arraycopy(tmp,0,`val`,0,16); return this }
}
