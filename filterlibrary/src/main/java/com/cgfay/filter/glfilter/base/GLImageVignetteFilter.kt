package com.cgfay.filter.glfilter.base

import android.content.Context
import android.graphics.PointF
import android.opengl.GLES20
import com.cgfay.filter.glfilter.utils.OpenGLUtils

/**
 * Vignette filter.
 */
class GLImageVignetteFilter : GLImageFilter {
    private var mVignetteCenterHandle = 0
    private var mVignetteColorHandle = 0
    private var mVignetteStartHandle = 0
    private var mVignetteEndHandle = 0
    private var mVignetteCenter: PointF? = null
    private var mVignetteColor: FloatArray? = null
    private var mVignetteStart = 0f
    private var mVignetteEnd = 0f

    constructor(context: Context) : this(
        context,
        VERTEX_SHADER,
        OpenGLUtils.getShaderFromAssets(context, "shader/base/fragment_vignette.glsl")
    )

    constructor(context: Context, vertexShader: String?, fragmentShader: String?) : super(context, vertexShader, fragmentShader)

    override fun initProgramHandle() {
        super.initProgramHandle()
        mVignetteCenterHandle = GLES20.glGetUniformLocation(mProgramHandle, "vignetteCenter")
        mVignetteColorHandle = GLES20.glGetUniformLocation(mProgramHandle, "vignetteColor")
        mVignetteStartHandle = GLES20.glGetUniformLocation(mProgramHandle, "vignetteStart")
        mVignetteEndHandle = GLES20.glGetUniformLocation(mProgramHandle, "vignetteEnd")
        setVignetteCenter(PointF(0.5f, 0.5f))
        setVignetteColor(floatArrayOf(0f, 0f, 0f))
        setVignetteStart(0.3f)
        setVignetteEnd(0.75f)
    }

    fun setVignetteCenter(vignetteCenter: PointF) {
        mVignetteCenter = vignetteCenter
        setPoint(mVignetteCenterHandle, mVignetteCenter!!)
    }

    fun setVignetteColor(vignetteColor: FloatArray) {
        mVignetteColor = vignetteColor
        setFloatVec3(mVignetteColorHandle, mVignetteColor!!)
    }

    fun setVignetteStart(vignetteStart: Float) {
        mVignetteStart = vignetteStart
        setFloat(mVignetteStartHandle, mVignetteStart)
    }

    fun setVignetteEnd(vignetteEnd: Float) {
        mVignetteEnd = vignetteEnd
        setFloat(mVignetteEndHandle, mVignetteEnd)
    }
}

