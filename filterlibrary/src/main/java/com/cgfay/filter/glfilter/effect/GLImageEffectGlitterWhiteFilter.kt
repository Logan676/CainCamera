package com.cgfay.filter.glfilter.effect

import android.content.Context
import android.opengl.GLES30
import com.cgfay.filter.glfilter.utils.OpenGLUtils

/** Glitter white effect similar to TikTok. */
class GLImageEffectGlitterWhiteFilter : GLImageEffectFilter {

    private var mColorHandle = 0
    private var color = 0f

    constructor(context: Context) : this(
        context,
        VERTEX_SHADER,
        OpenGLUtils.getShaderFromAssets(context, "shader/effect/fragment_effect_glitter_white.glsl")
    )

    constructor(context: Context, vertexShader: String, fragmentShader: String) :
        super(context, vertexShader, fragmentShader)

    override fun initProgramHandle() {
        super.initProgramHandle()
        if (mProgramHandle != OpenGLUtils.GL_NOT_INIT) {
            mColorHandle = GLES30.glGetUniformLocation(mProgramHandle, "color")
        }
    }

    override fun onDrawFrameBegin() {
        super.onDrawFrameBegin()
        GLES30.glUniform1f(mColorHandle, color)
    }

    override fun calculateInterval() {
        val interval = mCurrentPosition % 40f
        color += interval * 0.018f
        if (color > 1.0f) {
            color = 0f
        }
    }
}
