package com.cgfay.filter.glfilter.base

import android.content.Context
import android.graphics.PointF
import android.opengl.GLES30
import android.text.TextUtils
import com.cgfay.filter.glfilter.utils.OpenGLUtils
import com.cgfay.filter.glfilter.utils.TextureRotationUtils
import java.nio.FloatBuffer
import java.util.LinkedList

open class GLImageFilter {
    companion object {
        const val VERTEX_SHADER = "attribute vec4 aPosition;\n" +
                "attribute vec4 aTextureCoord;\n" +
                "varying vec2 textureCoordinate;\n" +
                "void main() {\n" +
                "    gl_Position = aPosition;\n" +
                "    textureCoordinate = aTextureCoord.xy;\n" +
                "}"

        const val FRAGMENT_SHADER = "precision mediump float;\n" +
                "varying vec2 textureCoordinate;\n" +
                "uniform sampler2D inputTexture;\n" +
                "void main() {\n" +
                "    gl_FragColor = texture2D(inputTexture, textureCoordinate);\n" +
                "}"
    }

    protected var TAG: String = javaClass.simpleName
    protected var mContext: Context? = null
    private val mRunOnDraw = LinkedList<() -> Unit>()
    protected var mVertexShader: String? = null
    protected var mFragmentShader: String? = null
    protected var mIsInitialized = false
    protected var mFilterEnable = true
    protected var mCoordsPerVertex = TextureRotationUtils.CoordsPerVertex
    protected var mVertexCount = TextureRotationUtils.CubeVertices.size / mCoordsPerVertex
    protected var mProgramHandle = 0
    protected var mPositionHandle = 0
    protected var mTextureCoordinateHandle = 0
    protected var mInputTextureHandle = 0
    protected var mImageWidth = 0
    protected var mImageHeight = 0
    protected var mDisplayWidth = 0
    protected var mDisplayHeight = 0
    protected var mFrameWidth = -1
    protected var mFrameHeight = -1
    protected var mFrameBuffers: IntArray? = null
    protected var mFrameBufferTextures: IntArray? = null

    constructor(context: Context?) : this(context, VERTEX_SHADER, FRAGMENT_SHADER)

    constructor(context: Context?, vertexShader: String?, fragmentShader: String?) {
        mContext = context
        mVertexShader = vertexShader
        mFragmentShader = fragmentShader
        initProgramHandle()
    }

    open fun initProgramHandle() {
        if (!mVertexShader.isNullOrEmpty() && !mFragmentShader.isNullOrEmpty()) {
            mProgramHandle = OpenGLUtils.createProgram(mVertexShader!!, mFragmentShader!!)
            mPositionHandle = GLES30.glGetAttribLocation(mProgramHandle, "aPosition")
            mTextureCoordinateHandle = GLES30.glGetAttribLocation(mProgramHandle, "aTextureCoord")
            mInputTextureHandle = GLES30.glGetUniformLocation(mProgramHandle, "inputTexture")
            mIsInitialized = true
        } else {
            mPositionHandle = OpenGLUtils.GL_NOT_INIT
            mTextureCoordinateHandle = OpenGLUtils.GL_NOT_INIT
            mInputTextureHandle = OpenGLUtils.GL_NOT_TEXTURE
            mIsInitialized = false
        }
    }

    open fun onInputSizeChanged(width: Int, height: Int) {
        mImageWidth = width
        mImageHeight = height
    }

    open fun onDisplaySizeChanged(width: Int, height: Int) {
        mDisplayWidth = width
        mDisplayHeight = height
    }

    open fun drawFrame(textureId: Int, vertexBuffer: FloatBuffer, textureBuffer: FloatBuffer): Boolean {
        if (!mIsInitialized || textureId == OpenGLUtils.GL_NOT_INIT || !mFilterEnable) {
            return false
        }
        GLES30.glViewport(0, 0, mDisplayWidth, mDisplayHeight)
        GLES30.glClearColor(0f, 0f, 0f, 1f)
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)
        GLES30.glUseProgram(mProgramHandle)
        runPendingOnDrawTasks()
        onDrawTexture(textureId, vertexBuffer, textureBuffer)
        return true
    }

    open fun drawFrameBuffer(textureId: Int, vertexBuffer: FloatBuffer, textureBuffer: FloatBuffer): Int {
        if (textureId == OpenGLUtils.GL_NOT_TEXTURE || mFrameBuffers == null || !mIsInitialized || !mFilterEnable) {
            return textureId
        }
        bindFrameBuffer()
        onDrawTexture(textureId, vertexBuffer, textureBuffer)
        return unBindFrameBuffer()
    }

    open fun bindFrameBuffer() {
        GLES30.glViewport(0, 0, mFrameWidth, mFrameHeight)
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, mFrameBuffers!![0])
        GLES30.glUseProgram(mProgramHandle)
        runPendingOnDrawTasks()
    }

    open fun unBindFrameBuffer(): Int {
        GLES30.glUseProgram(0)
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0)
        return mFrameBufferTextures!![0]
    }

    open fun drawFrameBufferClear(textureId: Int, vertexBuffer: FloatBuffer, textureBuffer: FloatBuffer): Int {
        if (textureId == OpenGLUtils.GL_NOT_TEXTURE || mFrameBuffers == null || !mIsInitialized || !mFilterEnable) {
            return textureId
        }
        GLES30.glViewport(0, 0, mFrameWidth, mFrameHeight)
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, mFrameBuffers!![0])
        GLES30.glClearColor(0f, 0f, 0f, 1f)
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)
        GLES30.glUseProgram(mProgramHandle)
        runPendingOnDrawTasks()
        onDrawTexture(textureId, vertexBuffer, textureBuffer)
        return unBindFrameBuffer()
    }

    open fun onDrawTexture(textureId: Int, vertexBuffer: FloatBuffer, textureBuffer: FloatBuffer) {
        vertexBuffer.position(0)
        GLES30.glVertexAttribPointer(mPositionHandle, mCoordsPerVertex, GLES30.GL_FLOAT, false, 0, vertexBuffer)
        GLES30.glEnableVertexAttribArray(mPositionHandle)
        textureBuffer.position(0)
        GLES30.glVertexAttribPointer(mTextureCoordinateHandle, 2, GLES30.GL_FLOAT, false, 0, textureBuffer)
        GLES30.glEnableVertexAttribArray(mTextureCoordinateHandle)
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        GLES30.glBindTexture(textureType, textureId)
        GLES30.glUniform1i(mInputTextureHandle, 0)
        onDrawFrameBegin()
        onDrawFrame()
        onDrawFrameAfter()
        GLES30.glDisableVertexAttribArray(mPositionHandle)
        GLES30.glDisableVertexAttribArray(mTextureCoordinateHandle)
        GLES30.glBindTexture(textureType, 0)
    }

    open fun onDrawFrameBegin() {}

    protected open fun onDrawFrame() {
        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, mVertexCount)
    }

    open fun onDrawFrameAfter() {}

    protected open fun onUnbindTextureValue() {}

    open val textureType: Int
        get() = GLES30.GL_TEXTURE_2D

    open fun release() {
        if (mIsInitialized) {
            GLES30.glDeleteProgram(mProgramHandle)
            mProgramHandle = OpenGLUtils.GL_NOT_INIT
        }
        destroyFrameBuffer()
    }

    open fun initFrameBuffer(width: Int, height: Int) {
        if (!isInitialized()) return
        if (mFrameBuffers != null && (mFrameWidth != width || mFrameHeight != height)) {
            destroyFrameBuffer()
        }
        if (mFrameBuffers == null) {
            mFrameWidth = width
            mFrameHeight = height
            mFrameBuffers = IntArray(1)
            mFrameBufferTextures = IntArray(1)
            OpenGLUtils.createFrameBuffer(mFrameBuffers, mFrameBufferTextures, width, height)
        }
    }

    open fun destroyFrameBuffer() {
        if (!mIsInitialized) return
        mFrameBufferTextures?.let {
            GLES30.glDeleteTextures(1, it, 0)
            mFrameBufferTextures = null
        }
        mFrameBuffers?.let {
            GLES30.glDeleteFramebuffers(1, it, 0)
            mFrameBuffers = null
        }
        mFrameWidth = -1
        mFrameHeight = -1
    }

    fun isInitialized(): Boolean = mIsInitialized

    fun setFilterEnable(enable: Boolean) {
        mFilterEnable = enable
    }

    fun getDisplayWidth(): Int = mDisplayWidth

    fun getDisplayHeight(): Int = mDisplayHeight

    protected fun setInteger(location: Int, intValue: Int) = runOnDraw {
        GLES30.glUniform1i(location, intValue)
    }

    protected fun setFloat(location: Int, floatValue: Float) = runOnDraw {
        GLES30.glUniform1f(location, floatValue)
    }

    protected fun setFloatVec2(location: Int, arrayValue: FloatArray) = runOnDraw {
        GLES30.glUniform2fv(location, 1, FloatBuffer.wrap(arrayValue))
    }

    protected fun setFloatVec3(location: Int, arrayValue: FloatArray) = runOnDraw {
        GLES30.glUniform3fv(location, 1, FloatBuffer.wrap(arrayValue))
    }

    protected fun setFloatVec4(location: Int, arrayValue: FloatArray) = runOnDraw {
        GLES30.glUniform4fv(location, 1, FloatBuffer.wrap(arrayValue))
    }

    protected fun setFloatArray(location: Int, arrayValue: FloatArray) = runOnDraw {
        GLES30.glUniform1fv(location, arrayValue.size, FloatBuffer.wrap(arrayValue))
    }

    protected fun setPoint(location: Int, point: PointF) = runOnDraw {
        val vec2 = floatArrayOf(point.x, point.y)
        GLES30.glUniform2fv(location, 1, vec2, 0)
    }

    protected fun setUniformMatrix3f(location: Int, matrix: FloatArray) = runOnDraw {
        GLES30.glUniformMatrix3fv(location, 1, false, matrix, 0)
    }

    protected fun setUniformMatrix4f(location: Int, matrix: FloatArray) = runOnDraw {
        GLES30.glUniformMatrix4fv(location, 1, false, matrix, 0)
    }

    protected fun runOnDraw(runnable: () -> Unit) {
        synchronized(mRunOnDraw) { mRunOnDraw.add(runnable) }
    }

    protected fun runPendingOnDrawTasks() {
        while (!mRunOnDraw.isEmpty()) {
            mRunOnDraw.removeFirst().invoke()
        }
    }

    protected fun clamp(value: Float, min: Float, max: Float): Float {
        return when {
            value < min -> min
            value > max -> max
            else -> value
        }
    }
}

