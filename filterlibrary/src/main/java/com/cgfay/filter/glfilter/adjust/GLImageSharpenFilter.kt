package com.cgfay.filter.glfilter.adjust

import android.content.Context
import android.opengl.GLES30
import com.cgfay.filter.glfilter.base.GLImageFilter
import com.cgfay.filter.glfilter.utils.OpenGLUtils

/**
 * 锐度变换
 */
class GLImageSharpenFilter(context: Context) : GLImageFilter(
    context,
    OpenGLUtils.getShaderFromAssets(context, "shader/adjust/vertex_sharpen.glsl"),
    OpenGLUtils.getShaderFromAssets(context, "shader/adjust/fragment_sharpen.glsl")
) {

    private var sharpnessLoc = 0
    private var sharpness = 0f
    private var imageWidthFactorHandle = 0
    private var imageHeightFactorHandle = 0

    override fun initProgramHandle() {
        super.initProgramHandle()
        imageWidthFactorHandle = GLES30.glGetUniformLocation(mProgramHandle, "imageWidthFactor")
        imageHeightFactorHandle = GLES30.glGetUniformLocation(mProgramHandle, "imageHeightFactor")
        sharpnessLoc = GLES30.glGetUniformLocation(mProgramHandle, "sharpness")
        setSharpness(0f)
    }

    override fun onInputSizeChanged(width: Int, height: Int) {
        super.onInputSizeChanged(width, height)
        setFloat(imageWidthFactorHandle, 1.0f / width)
        setFloat(imageHeightFactorHandle, 1.0f / height)
    }

    /**
     * 设置锐度 -4.0 ~ 4.0, 默认为0
     */
    fun setSharpness(sharpness: Float) {
        var value = sharpness
        if (value < -4.0f) {
            value = -4.0f
        } else if (value > 4.0f) {
            value = 4.0f
        }
        this.sharpness = value
        setFloat(sharpnessLoc, this.sharpness)
    }
}
