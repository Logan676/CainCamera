package com.cgfay.filter.glfilter.base

import android.content.Context
import com.cgfay.filter.glfilter.utils.OpenGLUtils
import java.nio.FloatBuffer

/**
 * Gaussian blur filter using two pass blur.
 */
open class GLImageGaussianBlurFilter : GLImageFilter {
    protected var mVerticalPassFilter: GLImageGaussPassFilter? = null
    protected var mHorizontalPassFilter: GLImageGaussPassFilter? = null
    private var mCurrentTexture = OpenGLUtils.GL_NOT_TEXTURE

    constructor(context: Context) : super(context, null, null) {
        initFilters()
    }

    constructor(context: Context, vertexShader: String?, fragmentShader: String?) : super(context, vertexShader, fragmentShader) {
        initFilters(vertexShader, fragmentShader)
    }

    private fun initFilters() {
        mVerticalPassFilter = GLImageGaussPassFilter(mContext!!)
        mHorizontalPassFilter = GLImageGaussPassFilter(mContext!!)
    }

    private fun initFilters(vertexShader: String?, fragmentShader: String?) {
        mVerticalPassFilter = GLImageGaussPassFilter(mContext!!, vertexShader, fragmentShader)
        mHorizontalPassFilter = GLImageGaussPassFilter(mContext!!, vertexShader, fragmentShader)
    }

    override fun initProgramHandle() {
        super.initProgramHandle()
        mVerticalPassFilter?.initProgramHandle()
        mHorizontalPassFilter?.initProgramHandle()
    }

    override fun onInputSizeChanged(width: Int, height: Int) {
        super.onInputSizeChanged(width, height)
        mVerticalPassFilter?.let {
            it.onInputSizeChanged(width, height)
            it.setTexelOffsetSize(0f, height.toFloat())
        }
        mHorizontalPassFilter?.let {
            it.onInputSizeChanged(width, height)
            it.setTexelOffsetSize(width.toFloat(), 0f)
        }
    }

    override fun onDisplaySizeChanged(width: Int, height: Int) {
        super.onDisplaySizeChanged(width, height)
        mVerticalPassFilter?.onDisplaySizeChanged(width, height)
        mHorizontalPassFilter?.onDisplaySizeChanged(width, height)
    }

    override fun drawFrame(textureId: Int, vertexBuffer: FloatBuffer, textureBuffer: FloatBuffer): Boolean {
        if (textureId == OpenGLUtils.GL_NOT_TEXTURE) return false
        mCurrentTexture = textureId
        mVerticalPassFilter?.let {
            mCurrentTexture = it.drawFrameBuffer(mCurrentTexture, vertexBuffer, textureBuffer)
        }
        return mHorizontalPassFilter?.drawFrame(mCurrentTexture, vertexBuffer, textureBuffer) ?: false
    }

    override fun drawFrameBuffer(textureId: Int, vertexBuffer: FloatBuffer, textureBuffer: FloatBuffer): Int {
        mCurrentTexture = textureId
        if (mCurrentTexture == OpenGLUtils.GL_NOT_TEXTURE) return mCurrentTexture
        mVerticalPassFilter?.let {
            mCurrentTexture = it.drawFrameBuffer(mCurrentTexture, vertexBuffer, textureBuffer)
        }
        mHorizontalPassFilter?.let {
            mCurrentTexture = it.drawFrameBuffer(mCurrentTexture, vertexBuffer, textureBuffer)
        }
        return mCurrentTexture
    }

    override fun initFrameBuffer(width: Int, height: Int) {
        super.initFrameBuffer(width, height)
        mVerticalPassFilter?.initFrameBuffer(width, height)
        mHorizontalPassFilter?.initFrameBuffer(width, height)
    }

    override fun destroyFrameBuffer() {
        super.destroyFrameBuffer()
        mVerticalPassFilter?.destroyFrameBuffer()
        mHorizontalPassFilter?.destroyFrameBuffer()
    }

    override fun release() {
        super.release()
        mVerticalPassFilter?.release()
        mHorizontalPassFilter?.release()
        mVerticalPassFilter = null
        mHorizontalPassFilter = null
    }

    fun setBlurSize(blurSize: Float) {
        mVerticalPassFilter?.setBlurSize(blurSize)
        mHorizontalPassFilter?.setBlurSize(blurSize)
    }
}

