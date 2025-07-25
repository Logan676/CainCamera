package com.cgfay.filter.glfilter.face

import android.content.Context
import android.opengl.GLES30
import android.text.TextUtils
import android.util.SparseArray
import com.cgfay.filter.glfilter.base.GLImageFilter
import com.cgfay.filter.glfilter.utils.OpenGLUtils
import com.cgfay.landmark.LandmarkEngine
import com.cgfay.landmark.OneFace
import java.nio.FloatBuffer

class GLImageFacePointsFilter : GLImageFilter {

    private val color = floatArrayOf(1.0f, 0.0f, 0.0f, 1.0f)

    private var mColorHandle = 0
    private val mPointCount = 114
    private val mPoints = FloatArray(mPointCount * 2)
    private val mPointVertexBuffer: FloatBuffer = OpenGLUtils.createFloatBuffer(mPoints)

    constructor(context: Context) : this(context, VertexShader, FragmentShader)

    constructor(context: Context, vertexShader: String, fragmentShader: String) : super(context, vertexShader, fragmentShader)

    override fun initProgramHandle() {
        if (!TextUtils.isEmpty(mVertexShader) && !TextUtils.isEmpty(mFragmentShader)) {
            mProgramHandle = OpenGLUtils.createProgram(mVertexShader, mFragmentShader)
            mPositionHandle = GLES30.glGetAttribLocation(mProgramHandle, "aPosition")
            mColorHandle = GLES30.glGetUniformLocation(mProgramHandle, "color")
            mIsInitialized = true
        } else {
            mPositionHandle = OpenGLUtils.GL_NOT_INIT
            mColorHandle = OpenGLUtils.GL_NOT_INIT
            mIsInitialized = false
        }
        mTextureCoordinateHandle = OpenGLUtils.GL_NOT_INIT
        mInputTextureHandle = OpenGLUtils.GL_NOT_TEXTURE
    }

    override fun drawFrame(textureId: Int, vertexBuffer: FloatBuffer?, textureBuffer: FloatBuffer?): Boolean {
        if (!mIsInitialized || !mFilterEnable) {
            return false
        }
        GLES30.glViewport(0, 0, mDisplayWidth, mDisplayHeight)
        GLES30.glUseProgram(mProgramHandle)
        runPendingOnDrawTasks()
        GLES30.glEnableVertexAttribArray(mPositionHandle)
        GLES30.glUniform4fv(mColorHandle, 1, color, 0)
        onDrawFrameBegin()
        synchronized(this) {
            if (LandmarkEngine.getInstance().faceSize > 0) {
                val faceArrays: SparseArray<OneFace> = LandmarkEngine.getInstance().faceArrays
                for (i in 0 until faceArrays.size()) {
                    if (faceArrays[i].vertexPoints != null) {
                        LandmarkEngine.getInstance().calculateExtraFacePoints(mPoints, i)
                        mPointVertexBuffer.clear()
                        mPointVertexBuffer.put(mPoints, 0, mPoints.size)
                        mPointVertexBuffer.position(0)
                        GLES30.glVertexAttribPointer(
                            mPositionHandle,
                            2,
                            GLES30.GL_FLOAT,
                            false,
                            8,
                            mPointVertexBuffer
                        )
                        GLES30.glDrawArrays(GLES30.GL_POINTS, 0, mPointCount)
                    }
                }
            }
        }
        onDrawFrameAfter()
        GLES30.glDisableVertexAttribArray(mPositionHandle)
        return true
    }

    override fun drawFrameBuffer(textureId: Int, vertexBuffer: FloatBuffer?, textureBuffer: FloatBuffer?): Int {
        if (textureId == OpenGLUtils.GL_NOT_TEXTURE || mFrameBuffers == null || !mIsInitialized || !mFilterEnable) {
            return textureId
        }
        drawFrame(textureId, vertexBuffer, textureBuffer)
        return textureId
    }

    override fun initFrameBuffer(width: Int, height: Int) {
        // do nothing
    }

    override fun destroyFrameBuffer() {
        // do nothing
    }

    companion object {
        private const val VertexShader = "" +
            "attribute vec4 aPosition;\n" +
            "void main() {\n" +
            "    gl_Position = aPosition;\n" +
            "    gl_PointSize = 8.0;\n" +
            "}"

        private const val FragmentShader = "" +
            "precision mediump float;\n" +
            "uniform vec4 color;\n" +
            "void main() {\n" +
            "    gl_FragColor = color;\n" +
            "}"
    }
}

