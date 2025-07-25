package com.cgfay.filter.glfilter.multiframe

import android.content.Context
import android.opengl.GLES30
import com.cgfay.filter.glfilter.base.GLImageFilter
import com.cgfay.filter.glfilter.base.GLImageGaussianBlurFilter
import com.cgfay.filter.glfilter.utils.OpenGLUtils
import java.nio.FloatBuffer

/**
 * Edge blur filter matching the effect used by FaceU.
 */
class GLImageFrameEdgeBlurFilter @JvmOverloads constructor(
    context: Context,
    vertexShader: String = VERTEX_SHADER,
    fragmentShader: String = OpenGLUtils.getShaderFromAssets(
        context,
        "shader/multiframe/fragment_frame_blur.glsl"
    )
) : GLImageFilter(context, vertexShader, fragmentShader) {

    private var mBlurTextureHandle = 0
    private var mBlurOffsetXHandle = 0
    private var mBlurOffsetYHandle = 0
    private var blurOffsetX = 0f
    private var blurOffsetY = 0f

    private var mGaussianBlurFilter: GLImageGaussianBlurFilter? = GLImageGaussianBlurFilter(mContext)
    private var mBlurScale = 0.5f
    private var mBlurTexture = OpenGLUtils.GL_NOT_TEXTURE

    override fun initProgramHandle() {
        super.initProgramHandle()
        if (mProgramHandle != OpenGLUtils.GL_NOT_INIT) {
            mBlurTextureHandle = GLES30.glGetUniformLocation(mProgramHandle, "blurTexture")
            mBlurOffsetXHandle = GLES30.glGetUniformLocation(mProgramHandle, "blurOffsetX")
            mBlurOffsetYHandle = GLES30.glGetUniformLocation(mProgramHandle, "blurOffsetY")
            setBlurOffset(0.15f, 0.15f)
        }
    }

    override fun onDrawFrameBegin() {
        super.onDrawFrameBegin()
        if (mBlurTexture != OpenGLUtils.GL_NOT_TEXTURE) {
            OpenGLUtils.bindTexture(mBlurTextureHandle, mBlurTexture, 1)
        }
    }

    override fun onInputSizeChanged(width: Int, height: Int) {
        super.onInputSizeChanged(width, height)
        mGaussianBlurFilter?.let {
            val w = (width * mBlurScale).toInt()
            val h = (height * mBlurScale).toInt()
            it.onInputSizeChanged(w, h)
            it.initFrameBuffer(w, h)
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
     * Set blur offsets.
     * @param offsetX Offset value 0.0 ~ 1.0
     * @param offsetY Offset value 0.0 ~ 1.0
     */
    fun setBlurOffset(offsetX: Float, offsetY: Float) {
        blurOffsetX = offsetX.coerceIn(0.0f, 1.0f)
        blurOffsetY = offsetY.coerceIn(0.0f, 1.0f)
        setFloat(mBlurOffsetXHandle, blurOffsetX)
        setFloat(mBlurOffsetYHandle, blurOffsetY)
    }
}
