package com.cgfay.filter.glfilter.effect

import android.content.Context
import android.opengl.GLES30
import com.cgfay.filter.glfilter.utils.OpenGLUtils

/** Black and white three-panel effect similar to TikTok */
class GLImageEffectBlackWhiteThreeFilter : GLImageEffectFilter {

    private var mScaleHandle = 0
    private var mScale = 1.2f

    constructor(context: Context) : this(
        context,
        VERTEX_SHADER,
        OpenGLUtils.getShaderFromAssets(context, "shader/effect/fragment_effect_multi_bw_three.glsl")
    )

    constructor(context: Context, vertexShader: String, fragmentShader: String) :
        super(context, vertexShader, fragmentShader)

    override fun initProgramHandle() {
        super.initProgramHandle()
        if (mProgramHandle != OpenGLUtils.GL_NOT_INIT) {
            mScaleHandle = GLES30.glGetUniformLocation(mProgramHandle, "scale")
        }
    }

    override fun onDrawFrameBegin() {
        super.onDrawFrameBegin()
        GLES30.glUniform1f(mScaleHandle, mScale)
    }
}
