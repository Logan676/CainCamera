package com.cgfay.filter.glfilter.base

import android.content.Context
import android.opengl.GLES20
import com.cgfay.filter.glfilter.utils.OpenGLUtils

open class GLImage3x3TextureSamplingFilter : GLImageFilter {
    private var mUniformTexelWidthLocation = 0
    private var mUniformTexelHeightLocation = 0

    private var mHasOverriddenImageSizeFactor = false
    private var mTexelWidth = 0f
    private var mTexelHeight = 0f
    private var mLineSize = 1.0f

    constructor(context: Context) : this(
        context,
        OpenGLUtils.getShaderFromAssets(context, "shader/base/vertex_3x3_texture_sampling.glsl"),
        FRAGMENT_SHADER
    )

    constructor(context: Context, vertexShader: String, fragmentShader: String) :
        super(context, vertexShader, fragmentShader)

    override fun initProgramHandle() {
        super.initProgramHandle()
        mUniformTexelWidthLocation = GLES20.glGetUniformLocation(mProgramHandle, "texelWidth")
        mUniformTexelHeightLocation = GLES20.glGetUniformLocation(mProgramHandle, "texelHeight")
        if (mTexelWidth != 0f) {
            updateTexelValues()
        }
    }

    override fun onInputSizeChanged(width: Int, height: Int) {
        super.onInputSizeChanged(width, height)
        if (!mHasOverriddenImageSizeFactor) {
            setLineSize(mLineSize)
        }
    }

    fun setTexelWidth(texelWidth: Float) {
        mHasOverriddenImageSizeFactor = true
        mTexelWidth = texelWidth
        setFloat(mUniformTexelWidthLocation, texelWidth)
    }

    fun setTexelHeight(texelHeight: Float) {
        mHasOverriddenImageSizeFactor = true
        mTexelHeight = texelHeight
        setFloat(mUniformTexelHeightLocation, texelHeight)
    }

    fun setLineSize(size: Float) {
        mLineSize = size
        mTexelWidth = size / mImageWidth
        mTexelHeight = size / mImageHeight
        updateTexelValues()
    }

    private fun updateTexelValues() {
        setFloat(mUniformTexelWidthLocation, mTexelWidth)
        setFloat(mUniformTexelHeightLocation, mTexelHeight)
    }
}
