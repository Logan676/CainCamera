package com.cgfay.filter.glfilter.base

import android.content.Context
import android.opengl.GLES20
import com.cgfay.filter.glfilter.utils.OpenGLUtils

open class GLImage3X3ConvolutionFilter : GLImage3x3TextureSamplingFilter {
    private var mConvolutionKernel = floatArrayOf(
        0.0f, 0.0f, 0.0f,
        0.0f, 1.0f, 0.0f,
        0.0f, 0.0f, 0.0f
    )
    private var mUniformConvolutionMatrix = 0

    constructor(context: Context) : this(
        context,
        OpenGLUtils.getShaderFromAssets(context, "shader/base/vertex_3x3_texture_sampling.glsl"),
        OpenGLUtils.getShaderFromAssets(context, "shader/base/fragment_3x3_convolution.glsl")
    )

    constructor(context: Context, vertexShader: String, fragmentShader: String) :
        super(context, vertexShader, fragmentShader)

    override fun initProgramHandle() {
        super.initProgramHandle()
        mUniformConvolutionMatrix = GLES20.glGetUniformLocation(mProgramHandle, "convolutionMatrix")
        setConvolutionKernel(floatArrayOf(-1.0f, 0.0f, 1.0f, -2.0f, 0.0f, 2.0f, -1.0f, 0.0f, 1.0f))
    }

    fun setConvolutionKernel(convolutionKernel: FloatArray) {
        mConvolutionKernel = convolutionKernel
        setUniformMatrix3f(mUniformConvolutionMatrix, mConvolutionKernel)
    }
}
