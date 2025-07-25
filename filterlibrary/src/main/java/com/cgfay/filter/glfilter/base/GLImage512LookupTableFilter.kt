package com.cgfay.filter.glfilter.base

import android.content.Context
import android.opengl.GLES30
import com.cgfay.filter.glfilter.utils.OpenGLUtils

/**
 * 3D LUT filter (512x512)
 */
open class GLImage512LookupTableFilter : GLImageFilter {
    private var mStrength = 0f
    private var mStrengthHandle = 0
    private var mLookupTableTextureHandle = 0
    private var mCurveTexture = OpenGLUtils.GL_NOT_INIT

    constructor(context: Context) : this(
        context,
        VERTEX_SHADER,
        OpenGLUtils.getShaderFromAssets(context, "shader/base/fragment_lookup_table_512.glsl")
    )

    constructor(context: Context, vertexShader: String, fragmentShader: String) :
        super(context, vertexShader, fragmentShader)

    override fun initProgramHandle() {
        super.initProgramHandle()
        mStrengthHandle = GLES30.glGetUniformLocation(mProgramHandle, "strength")
        mLookupTableTextureHandle = GLES30.glGetUniformLocation(mProgramHandle, "lookupTableTexture")
        setStrength(1.0f)
    }

    override fun onDrawFrameBegin() {
        super.onDrawFrameBegin()
        OpenGLUtils.bindTexture(mLookupTableTextureHandle, mCurveTexture, 1)
        GLES30.glUniform1f(mStrengthHandle, mStrength)
    }

    override fun release() {
        GLES30.glDeleteTextures(1, intArrayOf(mCurveTexture), 0)
        super.release()
    }

    fun setStrength(value: Float) {
        val opacity = when {
            value <= 0f -> 0f
            value > 1f -> 1f
            else -> value
        }
        mStrength = opacity
        setFloat(mStrengthHandle, mStrength)
    }
}
