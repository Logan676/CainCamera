package com.cgfay.filter.glfilter.effect

import android.content.Context
import android.opengl.GLES20
import com.cgfay.filter.glfilter.utils.OpenGLUtils

/** Soul stuff effect similar to TikTok */
class GLImageEffectSoulStuffFilter : GLImageEffectFilter {
    private var mScaleHandle = 0
    private var mScale = 1.0f
    private var mOffset = 0.0f

    constructor(context: Context) : this(
        context,
        VERTEX_SHADER,
        OpenGLUtils.getShaderFromAssets(context, "shader/effect/fragment_effect_soul_stuff.glsl")
    )

    constructor(context: Context, vertexShader: String, fragmentShader: String) :
        super(context, vertexShader, fragmentShader)

    override fun initProgramHandle() {
        super.initProgramHandle()
        if (mProgramHandle != OpenGLUtils.GL_NOT_INIT) {
            mScaleHandle = GLES20.glGetUniformLocation(mProgramHandle, "scale")
        }
    }

    override fun onDrawFrameBegin() {
        super.onDrawFrameBegin()
        GLES20.glUniform1f(mScaleHandle, mScale)
    }

    override fun calculateInterval() {
        val interval = mCurrentPosition % 40f
        mOffset += interval * 0.0025f
        if (mOffset > 1.0f) {
            mOffset = 0.0f
        }
        mScale = 1.0f + 0.3f * getInterpolation(mOffset)
    }

    private fun getInterpolation(input: Float): Float {
        return (Math.cos((input + 1) * Math.PI) / 2.0f).toFloat() + 0.5f
    }
}
