package com.cgfay.filter.glfilter.beauty

import android.content.Context
import android.opengl.GLES20
import com.cgfay.filter.glfilter.base.GLImageFilter
import com.cgfay.filter.glfilter.utils.OpenGLUtils

/**
 * Adjust filter used in beauty pipeline.
 */
class GLImageBeautyAdjustFilter(
    context: Context,
    vertexShader: String = VERTEX_SHADER,
    fragmentShader: String = OpenGLUtils.getShaderFromAssets(
        context,
        "shader/beauty/fragment_beauty_adjust.glsl"
    )
) : GLImageFilter(context, vertexShader, fragmentShader) {

    private var blurTextureHandle = 0
    private var blurTexture2Handle = 0
    private var intensityHandle = 0
    private var intensity = 1.0f
    private var blurTexture = 0
    private var highPassBlurTexture = 0

    override fun initProgramHandle() {
        super.initProgramHandle()
        blurTextureHandle = GLES20.glGetUniformLocation(mProgramHandle, "blurTexture")
        blurTexture2Handle = GLES20.glGetUniformLocation(mProgramHandle, "highPassBlurTexture")
        intensityHandle = GLES20.glGetUniformLocation(mProgramHandle, "intensity")
        intensity = 1.0f
    }

    override fun onDrawFrameBegin() {
        super.onDrawFrameBegin()
        OpenGLUtils.bindTexture(blurTextureHandle, blurTexture, 1)
        OpenGLUtils.bindTexture(blurTexture2Handle, highPassBlurTexture, 2)
        GLES20.glUniform1f(intensityHandle, intensity)
    }

    fun setSkinBeautyIntensity(value: Float) {
        intensity = value
    }

    fun setBlurTexture(blurTexture: Int, highPassBlurTexture: Int) {
        this.blurTexture = blurTexture
        this.highPassBlurTexture = highPassBlurTexture
    }
}
