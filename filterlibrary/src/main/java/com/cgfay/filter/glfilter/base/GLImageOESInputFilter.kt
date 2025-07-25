package com.cgfay.filter.glfilter.base

import android.content.Context
import android.opengl.GLES11Ext
import android.opengl.GLES30
import com.cgfay.filter.glfilter.utils.OpenGLUtils

/**
 * External texture(OES) input filter
 */
open class GLImageOESInputFilter : GLImageFilter {
    private var mTransformMatrixHandle = 0
    private var mTransformMatrix: FloatArray? = null

    constructor(context: Context) : this(
        context,
        OpenGLUtils.getShaderFromAssets(context, "shader/base/vertex_oes_input.glsl"),
        OpenGLUtils.getShaderFromAssets(context, "shader/base/fragment_oes_input.glsl")
    )

    constructor(context: Context, vertexShader: String, fragmentShader: String) :
        super(context, vertexShader, fragmentShader)

    override fun initProgramHandle() {
        super.initProgramHandle()
        mTransformMatrixHandle = GLES30.glGetUniformLocation(mProgramHandle, "transformMatrix")
    }

    override fun getTextureType(): Int = GLES11Ext.GL_TEXTURE_EXTERNAL_OES

    override fun onDrawFrameBegin() {
        super.onDrawFrameBegin()
        mTransformMatrix?.let {
            GLES30.glUniformMatrix4fv(mTransformMatrixHandle, 1, false, it, 0)
        }
    }

    fun setTextureTransformMatrix(transformMatrix: FloatArray?) {
        mTransformMatrix = transformMatrix
    }
}
