package com.cgfay.filter.glfilter.beauty

import android.content.Context
import android.opengl.GLES30
import com.cgfay.filter.glfilter.base.GLImageFilter
import com.cgfay.filter.glfilter.utils.OpenGLUtils

/**
 * High-pass filter in beauty pipeline.
 */
class GLImageBeautyHighPassFilter(
    context: Context,
    vertexShader: String = VERTEX_SHADER,
    fragmentShader: String = OpenGLUtils.getShaderFromAssets(
        context,
        "shader/beauty/fragment_beauty_highpass.glsl"
    )
) : GLImageFilter(context, vertexShader, fragmentShader) {

    private var blurTextureHandle = 0
    private var blurTexture = 0

    override fun initProgramHandle() {
        super.initProgramHandle()
        blurTextureHandle = GLES30.glGetUniformLocation(mProgramHandle, "blurTexture")
    }

    override fun onDrawFrameBegin() {
        super.onDrawFrameBegin()
        OpenGLUtils.bindTexture(blurTextureHandle, blurTexture, 1)
    }

    /**
     * Set texture after gaussian blur.
     */
    fun setBlurTexture(texture: Int) {
        blurTexture = texture
    }
}
