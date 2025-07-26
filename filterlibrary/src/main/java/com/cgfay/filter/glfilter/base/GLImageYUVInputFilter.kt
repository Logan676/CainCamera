package com.cgfay.filter.glfilter.base

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLES30
import com.cgfay.filter.glfilter.utils.OpenGLUtils
import java.nio.Buffer
import java.nio.ByteBuffer
import java.nio.FloatBuffer

/**
 * Render YUV/BGRA input frames.
 */
class GLImageYUVInputFilter : GLImageFilter {
    private var mRenderYUVHandle = 0
    private var mInputTexture2Handle = 0
    private var mInputTexture3Handle = 0

    private val mInputTexture = IntArray(3)
    private var mRenderYUV = 1
    private var mVertexBuffer: FloatBuffer
    private var mTextureBuffer: FloatBuffer

    private var yBuffer: Buffer? = null
    private var uBuffer: Buffer? = null
    private var vBuffer: Buffer? = null
    private var yLinesize = 0
    private var uLinesize = 0
    private var vLinesize = 0

    constructor(context: Context) : this(
        context,
        VERTEX_SHADER,
        OpenGLUtils.getShaderFromAssets(context, "shader/base/fragment_yuv_input.glsl")
    )

    constructor(context: Context, vertexShader: String?, fragmentShader: String?) : super(context, vertexShader, fragmentShader) {
        mRenderYUV = 1
        mVertexBuffer = OpenGLUtils.createFloatBuffer(floatArrayOf(
            -1.0f, -1.0f,
            1.0f, -1.0f,
            -1.0f, 1.0f,
            1.0f, 1.0f
        ))
        mTextureBuffer = OpenGLUtils.createFloatBuffer(floatArrayOf(
            0.0f, 1.0f,
            1.0f, 1.0f,
            0.0f, 0.0f,
            1.0f, 0.0f
        ))
    }

    override fun initProgramHandle() {
        super.initProgramHandle()
        if (mProgramHandle != OpenGLUtils.GL_NOT_INIT) {
            mRenderYUVHandle = GLES30.glGetUniformLocation(mProgramHandle, "renderYUV")
            mInputTexture2Handle = GLES30.glGetUniformLocation(mProgramHandle, "inputTexture2")
            mInputTexture3Handle = GLES30.glGetUniformLocation(mProgramHandle, "inputTexture3")
        }
        GLES30.glGenTextures(3, mInputTexture, 0)
        for (i in 0..2) {
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, mInputTexture[i])
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR)
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR)
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE)
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE)
        }
    }

    fun setYUVData(ydata: ByteArray, udata: ByteArray, vdata: ByteArray, yLinesize: Int, uLinesize: Int, vLinesize: Int) {
        mRenderYUV = 1
        yBuffer = ByteBuffer.wrap(ydata)
        uBuffer = ByteBuffer.wrap(udata)
        vBuffer = ByteBuffer.wrap(vdata)
        this.yLinesize = yLinesize
        this.uLinesize = uLinesize
        this.vLinesize = vLinesize
        cropTexVertices(yLinesize)
    }

    fun setBGRAData(data: ByteArray, linesize: Int) {
        mRenderYUV = 0
        yBuffer = ByteBuffer.wrap(data)
        this.yLinesize = linesize / 4
        cropTexVertices(this.yLinesize)
    }

    private fun cropTexVertices(linesize: Int) {
        if (mImageWidth != 0 && mImageWidth != linesize) {
            val normalized = (Math.abs(mImageWidth - linesize) + 0.5f) / linesize
            mTextureBuffer.clear()
            mTextureBuffer.put(
                floatArrayOf(
                    0.0f, 1.0f,
                    1.0f - normalized, 1.0f,
                    0.0f, 0.0f,
                    1.0f - normalized, 0.0f
                )
            )
        }
    }

    private fun updateYUV() {
        mRenderYUV = 1
        if (yBuffer != null && uBuffer != null && vBuffer != null) {
            GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, mInputTexture[0])
            GLES30.glTexImage2D(
                GLES30.GL_TEXTURE_2D, 0, GLES30.GL_LUMINANCE,
                yLinesize, mImageHeight, 0, GLES30.GL_LUMINANCE, GLES20.GL_UNSIGNED_BYTE, yBuffer
            )
            GLES30.glUniform1i(mInputTextureHandle, 0)

            GLES30.glActiveTexture(GLES30.GL_TEXTURE1)
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, mInputTexture[1])
            GLES30.glTexImage2D(
                GLES30.GL_TEXTURE_2D, 0, GLES30.GL_LUMINANCE,
                uLinesize, mImageHeight, 0, GLES30.GL_LUMINANCE, GLES20.GL_UNSIGNED_BYTE, uBuffer
            )
            GLES30.glUniform1i(mInputTexture2Handle, 1)

            GLES30.glActiveTexture(GLES30.GL_TEXTURE2)
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, mInputTexture[2])
            GLES30.glTexImage2D(
                GLES30.GL_TEXTURE_2D, 0, GLES30.GL_LUMINANCE,
                vLinesize, mImageHeight, 0, GLES30.GL_LUMINANCE, GLES20.GL_UNSIGNED_BYTE, vBuffer
            )
            GLES30.glUniform1i(mInputTexture3Handle, 2)
        }
        yBuffer = null
        uBuffer = null
        vBuffer = null
    }

    private fun updateBGRA() {
        mRenderYUV = 0
        if (yBuffer != null) {
            GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, mInputTexture[0])
            GLES30.glTexImage2D(
                GLES30.GL_TEXTURE_2D, 0, GLES30.GL_RGBA,
                yLinesize, mImageHeight, 0, GLES30.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, yBuffer
            )
            GLES30.glUniform1i(mInputTextureHandle, 0)
        }
        yBuffer = null
        uBuffer = null
        vBuffer = null
    }

    override fun onDrawTexture(textureId: Int, vertexBuffer: FloatBuffer, textureBuffer: FloatBuffer) {
        GLES30.glPixelStorei(GLES30.GL_UNPACK_ALIGNMENT, 1)
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)
        vertexBuffer.position(0)
        GLES30.glVertexAttribPointer(mPositionHandle, mCoordsPerVertex, GLES30.GL_FLOAT, false, 0, vertexBuffer)
        GLES30.glEnableVertexAttribArray(mPositionHandle)
        textureBuffer.position(0)
        GLES30.glVertexAttribPointer(mTextureCoordinateHandle, 2, GLES30.GL_FLOAT, false, 0, textureBuffer)
        GLES30.glEnableVertexAttribArray(mTextureCoordinateHandle)
        if (mRenderYUV == 1) {
            updateYUV()
        } else {
            updateBGRA()
        }
        GLES30.glUniform1i(mRenderYUVHandle, mRenderYUV)
        onDrawFrameBegin()
        onDrawFrame()
        onDrawFrameAfter()
        GLES30.glDisableVertexAttribArray(mPositionHandle)
        GLES30.glDisableVertexAttribArray(mTextureCoordinateHandle)
        GLES30.glBindTexture(textureType, 0)
        GLES30.glUseProgram(0)
    }

    fun drawFrame(): Boolean {
        return drawFrame(mInputTexture[0], mVertexBuffer, mTextureBuffer)
    }

    fun drawFrameBuffer(): Int {
        return drawFrameBuffer(mInputTexture[0], mVertexBuffer, mTextureBuffer)
    }
}

