package com.cgfay.caincamera.renderer

import android.content.Context
import android.opengl.GLES30
import com.cgfay.filter.glfilter.base.GLImageFilter
import com.cgfay.filter.glfilter.utils.OpenGLUtils

/**
 * Filter used for duet recording scenarios.
 */
class GLImageDuetFilter(context: Context) : GLImageFilter(
    context,
    OpenGLUtils.getShaderFromAssets(context, "shader/multiframe/vertex_duet.glsl"),
    OpenGLUtils.getShaderFromAssets(context, "shader/multiframe/fragment_duet.glsl")
) {

    /** Transformation matrix handle */
    private var mMVPMatrixHandle = 0
    private var mMVPMatrix = FloatArray(16)

    private var mOffsetDxHandle = 0
    private var mOffsetDyHandle = 0
    private var mTypeHandle = 0

    override fun initProgramHandle() {
        super.initProgramHandle()
        if (mProgramHandle != OpenGLUtils.GL_NOT_INIT) {
            mMVPMatrixHandle = GLES30.glGetUniformLocation(mProgramHandle, "uMVPMatrix")
            mOffsetDxHandle = GLES30.glGetUniformLocation(mProgramHandle, "offset_dx")
            mOffsetDyHandle = GLES30.glGetUniformLocation(mProgramHandle, "offset_dy")
            mTypeHandle = GLES30.glGetUniformLocation(mProgramHandle, "type")
        } else {
            mMVPMatrixHandle = OpenGLUtils.GL_NOT_INIT
            mOffsetDxHandle = OpenGLUtils.GL_NOT_INIT
            mOffsetDyHandle = OpenGLUtils.GL_NOT_INIT
            mTypeHandle = OpenGLUtils.GL_NOT_INIT
        }
    }

    override fun onDrawFrameBegin() {
        super.onDrawFrameBegin()
        if (mMVPMatrixHandle != OpenGLUtils.GL_NOT_INIT) {
            GLES30.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0)
        }
        // Enable blending when drawing to the FBO
        GLES30.glEnable(GLES30.GL_BLEND)
        GLES30.glBlendEquation(GLES30.GL_FUNC_ADD)
        GLES30.glBlendFuncSeparate(
            GLES30.GL_ONE,
            GLES30.GL_ONE_MINUS_SRC_ALPHA,
            GLES30.GL_ONE,
            GLES30.GL_ONE
        )
    }

    override fun onDrawFrameAfter() {
        super.onDrawFrameAfter()
        GLES30.glDisable(GLES30.GL_BLEND)
    }

    fun setMVPMatrix(MVPMatrix: FloatArray) {
        mMVPMatrix = MVPMatrix
    }

    /**
     * Set duet type flag
     */
    fun setDuetType(type: Float) {
        if (mTypeHandle != OpenGLUtils.GL_NOT_INIT) {
            GLES30.glUniform1f(mTypeHandle, type)
        }
    }

    /**
     * Set X-axis offset
     */
    fun setOffsetX(offset: Float) {
        if (mOffsetDxHandle != OpenGLUtils.GL_NOT_INIT) {
            GLES30.glUniform1f(mOffsetDxHandle, offset)
        }
    }

    /**
     * Set Y-axis offset
     */
    fun setOffsetY(offset: Float) {
        if (mOffsetDyHandle != OpenGLUtils.GL_NOT_INIT) {
            GLES30.glUniform1f(mOffsetDyHandle, offset)
        }
    }
}
