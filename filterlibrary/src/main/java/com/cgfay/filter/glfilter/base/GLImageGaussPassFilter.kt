package com.cgfay.filter.glfilter.base

import android.content.Context
import android.opengl.GLES20
import com.cgfay.filter.glfilter.utils.OpenGLUtils

/**
 * Single pass of Gaussian blur.
 */
class GLImageGaussPassFilter : GLImageFilter {
    private var mBlurSize = 1f
    private var mTexelWidthOffsetHandle = 0
    private var mTexelHeightOffsetHandle = 0
    private var mTexelWidth = 0f
    private var mTexelHeight = 0f

    constructor(context: Context) : this(
        context,
        OpenGLUtils.getShaderFromAssets(context, "shader/base/vertex_gaussian_pass.glsl"),
        OpenGLUtils.getShaderFromAssets(context, "shader/base/fragment_gaussian_pass.glsl")
    )

    constructor(context: Context, vertexShader: String?, fragmentShader: String?) : super(context, vertexShader, fragmentShader)

    override fun initProgramHandle() {
        super.initProgramHandle()
        mTexelWidthOffsetHandle = GLES20.glGetUniformLocation(mProgramHandle, "texelWidthOffset")
        mTexelHeightOffsetHandle = GLES20.glGetUniformLocation(mProgramHandle, "texelHeightOffset")
    }

    fun setBlurSize(blurSize: Float) {
        mBlurSize = blurSize
    }

    fun setTexelOffsetSize(width: Float, height: Float) {
        mTexelWidth = width
        mTexelHeight = height
        if (mTexelWidth != 0f) {
            setFloat(mTexelWidthOffsetHandle, mBlurSize / mTexelWidth)
        } else {
            setFloat(mTexelWidthOffsetHandle, 0.0f)
        }
        if (mTexelHeight != 0f) {
            setFloat(mTexelHeightOffsetHandle, mBlurSize / mTexelHeight)
        } else {
            setFloat(mTexelHeightOffsetHandle, 0.0f)
        }
    }
}

