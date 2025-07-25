package com.cgfay.filter.glfilter.base

import android.content.Context
import android.graphics.PointF
import android.opengl.GLES30
import com.cgfay.filter.glfilter.utils.OpenGLUtils
import java.nio.FloatBuffer

/**
 * Depth blur filter using a Gaussian blur pass.
 */
open class GLImageDepthBlurFilter(context: Context) : GLImageFilter(
    context,
    VERTEX_SHADER,
    OpenGLUtils.getShaderFromAssets(context, "shader/base/fragment_depth_blur.glsl")
) {
    private var mBlurImageHandle = 0
    private var mInnerHandle = 0
    private var mOuterHandle = 0
    private var mWidthHandle = 0
    private var mHeightHandle = 0
    private var mCenterHandle = 0
    private var mLine1Handle = 0
    private var mLine2Handle = 0
    private var mIntensityHandle = 0

    private var mGaussianBlurFilter = GLImageGaussianBlurFilter(context)
    private var mBlurScale = 0.5f
    private var mBlurTexture = OpenGLUtils.GL_NOT_TEXTURE

    constructor(context: Context, vertexShader: String, fragmentShader: String) :
        this(context) {
        // delegate to primary constructor then reinitialize filter
    }

    override fun initProgramHandle() {
        super.initProgramHandle()
        if (mProgramHandle != OpenGLUtils.GL_NOT_INIT) {
            mBlurImageHandle = GLES30.glGetUniformLocation(mProgramHandle, "blurImageTexture")
            mInnerHandle = GLES30.glGetUniformLocation(mProgramHandle, "inner")
            mOuterHandle = GLES30.glGetUniformLocation(mProgramHandle, "outer")
            mWidthHandle = GLES30.glGetUniformLocation(mProgramHandle, "width")
            mHeightHandle = GLES30.glGetUniformLocation(mProgramHandle, "height")
            mCenterHandle = GLES30.glGetUniformLocation(mProgramHandle, "center")
            mLine1Handle = GLES30.glGetUniformLocation(mProgramHandle, "line1")
            mLine2Handle = GLES30.glGetUniformLocation(mProgramHandle, "line2")
            mIntensityHandle = GLES30.glGetUniformLocation(mProgramHandle, "intensity")
            initUniformData()
        }
    }

    private fun initUniformData() {
        setFloat(mInnerHandle, 0.35f)
        setFloat(mOuterHandle, 0.12f)
        setPoint(mCenterHandle, PointF(0.5f, 0.5f))
        setFloatVec3(mLine1Handle, floatArrayOf(0f, 0f, -0.15f))
        setFloatVec3(mLine2Handle, floatArrayOf(0f, 0f, -0.15f))
        setFloat(mIntensityHandle, 1.0f)
    }

    override fun onDrawFrameBegin() {
        super.onDrawFrameBegin()
        if (mBlurTexture != OpenGLUtils.GL_NOT_TEXTURE) {
            OpenGLUtils.bindTexture(mBlurImageHandle, mBlurTexture, 1)
        }
    }

    override fun onInputSizeChanged(width: Int, height: Int) {
        super.onInputSizeChanged(width, height)
        setFloat(mWidthHandle, width.toFloat())
        setFloat(mHeightHandle, height.toFloat())
        mGaussianBlurFilter.onInputSizeChanged((width * mBlurScale).toInt(), (height * mBlurScale).toInt())
    }

    override fun onDisplaySizeChanged(width: Int, height: Int) {
        super.onDisplaySizeChanged(width, height)
        mGaussianBlurFilter.onDisplaySizeChanged(width, height)
    }

    override fun drawFrame(textureId: Int, vertexBuffer: FloatBuffer, textureBuffer: FloatBuffer): Boolean {
        mBlurTexture = mGaussianBlurFilter.drawFrameBuffer(textureId, vertexBuffer, textureBuffer)
        return super.drawFrame(textureId, vertexBuffer, textureBuffer)
    }

    override fun drawFrameBuffer(textureId: Int, vertexBuffer: FloatBuffer, textureBuffer: FloatBuffer): Int {
        mBlurTexture = mGaussianBlurFilter.drawFrameBuffer(textureId, vertexBuffer, textureBuffer)
        return super.drawFrameBuffer(textureId, vertexBuffer, textureBuffer)
    }

    override fun initFrameBuffer(width: Int, height: Int) {
        super.initFrameBuffer(width, height)
        mGaussianBlurFilter.initFrameBuffer((width * mBlurScale).toInt(), (height * mBlurScale).toInt())
    }

    override fun destroyFrameBuffer() {
        super.destroyFrameBuffer()
        mGaussianBlurFilter.destroyFrameBuffer()
    }

    override fun release() {
        super.release()
        mGaussianBlurFilter.release()
        if (mBlurTexture != OpenGLUtils.GL_NOT_TEXTURE) {
            GLES30.glDeleteTextures(1, intArrayOf(mBlurTexture), 0)
        }
    }
}
