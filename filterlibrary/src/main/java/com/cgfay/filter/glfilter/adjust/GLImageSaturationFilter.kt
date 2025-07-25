package com.cgfay.filter.glfilter.adjust

import android.content.Context
import android.opengl.GLES30
import com.cgfay.filter.glfilter.base.GLImageFilter
import com.cgfay.filter.glfilter.utils.OpenGLUtils

/**
 * 饱和度滤镜
 * 饱和度可以解决为彩色光所呈现的彩色的深浅程度，取决于彩色光中混入的白光的数量，
 * 饱和度是某种色光纯度的反映，饱和度越高，则深色越深
 */
class GLImageSaturationFilter(context: Context) : GLImageFilter(
    context,
    VERTEX_SHADER,
    OpenGLUtils.getShaderFromAssets(context, "shader/adjust/fragment_saturation.glsl")
) {

    private var rangeMinHandle = 0
    private var rangeMaxHandle = 0
    private var inputLevelHandle = 0
    private var saturation = 1f

    override fun initProgramHandle() {
        super.initProgramHandle()
        rangeMinHandle = GLES30.glGetUniformLocation(mProgramHandle, "rangeMin")
        rangeMaxHandle = GLES30.glGetUniformLocation(mProgramHandle, "rangeMax")
        inputLevelHandle = GLES30.glGetUniformLocation(mProgramHandle, "inputLevel")
        setSaturationMin(floatArrayOf(0f, 0f, 0f))
        setSaturationMax(floatArrayOf(1f, 1f, 1f))
        setSaturation(1f)
    }

    /**
     * 设置饱和度值 0.0 ~ 2.0
     */
    fun setSaturation(saturation: Float) {
        var value = saturation
        if (value < 0.0f) {
            value = 0.0f
        } else if (value > 2.0f) {
            value = 2.0f
        }
        this.saturation = value
        setFloat(inputLevelHandle, this.saturation)
    }

    /** 设置饱和度最小值 */
    fun setSaturationMin(matrix: FloatArray) {
        setFloatVec3(rangeMinHandle, matrix)
    }

    /** 设置饱和度最大值 */
    fun setSaturationMax(matrix: FloatArray) {
        setFloatVec3(rangeMaxHandle, matrix)
    }
}
