package com.cgfay.caincamera.renderer

import android.graphics.SurfaceTexture
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.util.Log
import androidx.annotation.NonNull
import com.cgfay.caincamera.presenter.FFMediaRecordPresenter
import com.cgfay.filter.glfilter.base.GLImageFilter
import com.cgfay.filter.glfilter.base.GLImageOESInputFilter
import com.cgfay.filter.glfilter.utils.OpenGLUtils
import com.cgfay.filter.glfilter.utils.TextureRotationUtils
import java.lang.ref.WeakReference
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * FFmpeg录制渲染器
 */
class FFRecordRenderer(presenter: FFMediaRecordPresenter) : GLSurfaceView.Renderer {

    companion object {
        private const val TAG = "FFRecordRenderer"
        private const val VERBOSE = false
    }

    private var mInputFilter: GLImageOESInputFilter? = null // 相机输入滤镜
    private var mImageFilter: GLImageFilter? = null // 输出滤镜
    // 顶点坐标缓冲
    private lateinit var mVertexBuffer: FloatBuffer
    // 纹理坐标缓冲
    private lateinit var mTextureBuffer: FloatBuffer
    // 预览顶点坐标缓冲
    private lateinit var mDisplayVertexBuffer: FloatBuffer
    // 预览纹理坐标缓冲
    private lateinit var mDisplayTextureBuffer: FloatBuffer
    // 输入纹理大小
    protected var mTextureWidth = 0
    protected var mTextureHeight = 0
    // 控件视图大小
    protected var mViewWidth = 0
    protected var mViewHeight = 0
    // 输入纹理
    private var mInputTexture = OpenGLUtils.GL_NOT_TEXTURE
    private val mMatrix = FloatArray(16)
    @Volatile private var mNeedToAttach = false
    private var mWeakSurfaceTexture: WeakReference<SurfaceTexture>? = null

    private val mWeakPresenter = WeakReference(presenter)

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        if (VERBOSE) {
            Log.d(TAG, "onSurfaceCreated: ")
        }
        mVertexBuffer = OpenGLUtils.createFloatBuffer(TextureRotationUtils.CubeVertices)
        mTextureBuffer = OpenGLUtils.createFloatBuffer(TextureRotationUtils.TextureVertices)
        mDisplayVertexBuffer = OpenGLUtils.createFloatBuffer(TextureRotationUtils.CubeVertices)
        mDisplayTextureBuffer = OpenGLUtils.createFloatBuffer(TextureRotationUtils.TextureVertices)

        GLES30.glDisable(GL10.GL_DITHER)
        GLES30.glClearColor(0f, 0f, 0f, 0f)
        GLES30.glEnable(GL10.GL_CULL_FACE)
        GLES30.glEnable(GL10.GL_DEPTH_TEST)
        initFilters()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        if (VERBOSE) {
            Log.d(TAG, "onSurfaceChanged: ")
        }
        mViewWidth = width
        mViewHeight = height
        onFilterSizeChanged()
        adjustCoordinateSize()
    }

    override fun onDrawFrame(gl: GL10?) {
        val surfaceTexture = mWeakSurfaceTexture?.get() ?: return
        synchronized(this) {
            updateSurfaceTexture(surfaceTexture)
        }
        GLES30.glClearColor(0f, 0f, 0f, 0f)
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT or GLES30.GL_DEPTH_BUFFER_BIT)
        val inputFilter = mInputFilter ?: return
        val imageFilter = mImageFilter ?: return
        inputFilter.setTextureTransformMatrix(mMatrix)
        // 将OES纹理绘制到FBO中
        var currentTexture = mInputTexture
        currentTexture = inputFilter.drawFrameBuffer(currentTexture, mVertexBuffer, mTextureBuffer)
        // 将最终的结果会是预览
        imageFilter.drawFrame(currentTexture, mDisplayVertexBuffer, mDisplayTextureBuffer)
    }

    /** 更新输出纹理数据 */
    private fun updateSurfaceTexture(@NonNull surfaceTexture: SurfaceTexture) {
        synchronized(this) {
            if (mNeedToAttach) {
                if (mInputTexture != OpenGLUtils.GL_NOT_TEXTURE) {
                    OpenGLUtils.deleteTexture(mInputTexture)
                }
                mInputTexture = OpenGLUtils.createOESTexture()
                try {
                    surfaceTexture.attachToGLContext(mInputTexture)
                    mNeedToAttach = false
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        try {
            surfaceTexture.updateTexImage()
            surfaceTexture.getTransformMatrix(mMatrix)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (VERBOSE) {
            Log.d(TAG, "updateSurfaceTexture: ")
        }
    }

    /** 绑定纹理 */
    fun bindSurfaceTexture(surfaceTexture: SurfaceTexture) {
        synchronized(this) {
            if (mWeakSurfaceTexture == null || mWeakSurfaceTexture?.get() != surfaceTexture) {
                mWeakSurfaceTexture = WeakReference(surfaceTexture)
                mNeedToAttach = true
            }
        }
    }

    /** 设置输入纹理大小 */
    fun setTextureSize(width: Int, height: Int) {
        mTextureWidth = width
        mTextureHeight = height
        if (mViewWidth != 0 && mViewHeight != 0) {
            onFilterSizeChanged()
            adjustCoordinateSize()
        }
        if (VERBOSE) {
            Log.d(TAG, "setTextureSize: width = $width, height = $height")
        }
    }

    /** 初始化滤镜 */
    private fun initFilters() {
        val activity = mWeakPresenter.get()?.activity ?: return
        mInputFilter = GLImageOESInputFilter(activity)
        mImageFilter = GLImageFilter(activity)
    }

    /** 初始化FBO等 */
    private fun onFilterSizeChanged() {
        mInputFilter?.let {
            it.onInputSizeChanged(mTextureWidth, mTextureHeight)
            it.initFrameBuffer(mTextureWidth, mTextureHeight)
            it.onDisplaySizeChanged(mViewWidth, mViewHeight)
        }
        mImageFilter?.let {
            it.onInputSizeChanged(mTextureWidth, mTextureHeight)
            it.onDisplaySizeChanged(mViewWidth, mViewHeight)
        }
    }

    /** 调整由于surface的大小与SurfaceView大小不一致带来的显示问题 */
    private fun adjustCoordinateSize() {
        val vertexCoord = TextureRotationUtils.CubeVertices
        val textureVertices = TextureRotationUtils.TextureVertices
        val ratioMax = maxOf(
            mViewWidth.toFloat() / mTextureWidth,
            mViewHeight.toFloat() / mTextureHeight
        )
        // 新的宽高
        val imageWidth = mTextureWidth * ratioMax
        val imageHeight = mTextureHeight * ratioMax
        // 获取视图跟texture的宽高比
        val ratioWidth = imageWidth / mViewWidth.toFloat()
        val ratioHeight = imageHeight / mViewHeight.toFloat()
        val distHorizontal = (1 - 1 / ratioWidth) / 2
        val distVertical = (1 - 1 / ratioHeight) / 2
        val textureCoord = floatArrayOf(
            addDistance(textureVertices[0], distHorizontal), addDistance(textureVertices[1], distVertical),
            addDistance(textureVertices[2], distHorizontal), addDistance(textureVertices[3], distVertical),
            addDistance(textureVertices[4], distHorizontal), addDistance(textureVertices[5], distVertical),
            addDistance(textureVertices[6], distHorizontal), addDistance(textureVertices[7], distVertical)
        )
        // 更新VertexBuffer 和 TextureBuffer
        mDisplayVertexBuffer.clear()
        mDisplayVertexBuffer.put(vertexCoord).position(0)
        mDisplayTextureBuffer.clear()
        mDisplayTextureBuffer.put(textureCoord).position(0)
    }

    private fun addDistance(coordinate: Float, distance: Float): Float {
        return if (coordinate == 0.0f) distance else 1 - distance
    }

    /** 清理一下任务数据 */
    fun clear() {
        mWeakSurfaceTexture?.clear()
    }
}
