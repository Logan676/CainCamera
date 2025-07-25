package com.cgfay.filter.glfilter.effect

import android.content.Context
import android.opengl.GLES30
import com.cgfay.filter.glfilter.base.GLImageGaussianBlurFilter
import com.cgfay.filter.glfilter.utils.OpenGLUtils
import java.nio.FloatBuffer

/** Blur multi-panel effect similar to TikTok. */
class GLImageEffectMultiBlurFilter : GLImageEffectFilter {

    private var mBlurTextureHandle = 0
    private var mBlurOffsetYHandle = 0
    private var mScaleHandle = 0
    private var blurOffsetY = 0f

    private var mGaussianBlurFilter: GLImageGaussianBlurFilter? = null
    private var mBlurScale = 0.5f
    private var mBlurTexture = OpenGLUtils.GL_NOT_TEXTURE
    private var mScale = 1.2f

    constructor(context: Context) : this(
        context,
        VERTEX_SHADER,
        OpenGLUtils.getShaderFromAssets(context, "shader/effect/fragment_effect_multi_blur.glsl")
    )

    constructor(context: Context, vertexShader: String, fragmentShader: String) :
        super(context, vertexShader, fragmentShader) {
        mGaussianBlurFilter = GLImageGaussianBlurFilter(mContext)
        mGaussianBlurFilter?.setBlurSize(1.0f)
    }

    override fun initProgramHandle() {
        super.initProgramHandle()
        if (mProgramHandle != OpenGLUtils.GL_NOT_INIT) {
            mBlurTextureHandle = GLES30.glGetUniformLocation(mProgramHandle, "blurTexture")
            mBlurOffsetYHandle = GLES30.glGetUniformLocation(mProgramHandle, "blurOffsetY")
            mScaleHandle = GLES30.glGetUniformLocation(mProgramHandle, "scale")
            setBlurOffset(0.33f)
        }
    }

    override fun onDrawFrameBegin() {
        super.onDrawFrameBegin()
        if (mBlurTexture != OpenGLUtils.GL_NOT_TEXTURE) {
            OpenGLUtils.bindTexture(mBlurTextureHandle, mBlurTexture, 1)
        }
        GLES30.glUniform1f(mScaleHandle, mScale)
    }

    override fun onInputSizeChanged(width: Int, height: Int) {
        super.onInputSizeChanged(width, height)
        mGaussianBlurFilter?.let {
            it.onInputSizeChanged((width * mBlurScale).toInt(), (height * mBlurScale).toInt())
            it.initFrameBuffer((width * mBlurScale).toInt(), (height * mBlurScale).toInt())
        }
    }

    override fun onDisplaySizeChanged(width: Int, height: Int) {
        super.onDisplaySizeChanged(width, height)
        mGaussianBlurFilter?.onDisplaySizeChanged(width, height)
    }

    override fun drawFrame(textureId: Int, vertexBuffer: FloatBuffer, textureBuffer: FloatBuffer): Boolean {
        mGaussianBlurFilter?.let {
            mBlurTexture = it.drawFrameBuffer(textureId, vertexBuffer, textureBuffer)
        }
        return super.drawFrame(textureId, vertexBuffer, textureBuffer)
    }

    override fun drawFrameBuffer(textureId: Int, vertexBuffer: FloatBuffer, textureBuffer: FloatBuffer): Int {
        mGaussianBlurFilter?.let {
            mBlurTexture = it.drawFrameBuffer(textureId, vertexBuffer, textureBuffer)
        }
        return super.drawFrameBuffer(textureId, vertexBuffer, textureBuffer)
    }

    override fun initFrameBuffer(width: Int, height: Int) {
        super.initFrameBuffer(width, height)
        mGaussianBlurFilter?.initFrameBuffer((width * mBlurScale).toInt(), (height * mBlurScale).toInt())
    }

    override fun destroyFrameBuffer() {
        super.destroyFrameBuffer()
        mGaussianBlurFilter?.destroyFrameBuffer()
    }

    override fun release() {
        super.release()
        mGaussianBlurFilter?.release()
        mGaussianBlurFilter = null
        if (mBlurTexture != OpenGLUtils.GL_NOT_TEXTURE) {
            GLES30.glDeleteTextures(1, intArrayOf(mBlurTexture), 0)
        }
    }

    /**
     * Set blur offset Y between 0 and 1.
     */
    fun setBlurOffset(offsetY: Float) {
        var value = offsetY
        if (value < 0f) value = 0f else if (value > 1f) value = 1f
        blurOffsetY = value
        setFloat(mBlurOffsetYHandle, blurOffsetY)
    }
}
