package com.cgfay.filter.widget

import android.content.Context
import android.graphics.Bitmap
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.Toast
import com.cgfay.filter.glfilter.base.GLImageFilter
import com.cgfay.filter.glfilter.base.GLImageInputFilter
import com.cgfay.filter.glfilter.color.GLImageDynamicColorFilter
import com.cgfay.filter.glfilter.color.bean.DynamicColor
import com.cgfay.filter.glfilter.resource.FilterHelper
import com.cgfay.filter.glfilter.resource.ResourceJsonCodec
import com.cgfay.filter.glfilter.resource.bean.ResourceData
import com.cgfay.filter.glfilter.utils.OpenGLUtils
import com.cgfay.filter.glfilter.utils.TextureRotationUtils
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * GL view for rendering a bitmap with filters.
 */
class GLImageSurfaceView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : GLSurfaceView(context, attrs), GLSurfaceView.Renderer {

    /** Input texture id */
    protected var mInputTexture = OpenGLUtils.GL_NOT_TEXTURE

    /** Filter for image input */
    protected var mInputFilter: GLImageInputFilter? = null

    /** Color filter */
    protected var mColorFilter: GLImageFilter? = null

    /** Display output filter */
    protected var mDisplayFilter: GLImageFilter? = null

    private var mVertexBuffer: FloatBuffer =
        OpenGLUtils.createFloatBuffer(TextureRotationUtils.CubeVertices)
    private var mTextureBuffer: FloatBuffer =
        OpenGLUtils.createFloatBuffer(TextureRotationUtils.TextureVertices)

    /** Texture size */
    protected var mTextureWidth = 0
    protected var mTextureHeight = 0

    /** View size */
    protected var mViewWidth = 0
    protected var mViewHeight = 0

    /** Input bitmap */
    private var mBitmap: Bitmap? = null

    /** Current filter resource */
    private var mResourceData: ResourceData? = null

    /** Handler on main thread */
    protected val mMainHandler = Handler(Looper.getMainLooper())

    private var takePicture = false

    private var mCaptureCallback: CaptureCallback? = null

    init {
        setEGLContextClientVersion(3)
        setRenderer(this)
        renderMode = RENDERMODE_WHEN_DIRTY
    }

    @Throws(Throwable::class)
    protected fun finalize() {
        mCaptureCallback = null
    }

    override fun onPause() {
        super.onPause()
        mInputTexture = OpenGLUtils.GL_NOT_TEXTURE
        mColorFilter = null
        mDisplayFilter = null
        mInputFilter = null
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES30.glDisable(GL10.GL_DITHER)
        GLES30.glClearColor(0f, 0f, 0f, 0f)
        GLES30.glEnable(GL10.GL_CULL_FACE)
        GLES30.glEnable(GL10.GL_DEPTH_TEST)
        initFilters()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        mViewWidth = width
        mViewHeight = height
        GLES30.glViewport(0, 0, width, height)
        if (mInputTexture == OpenGLUtils.GL_NOT_TEXTURE) {
            mBitmap?.let { mInputTexture = OpenGLUtils.createTexture(it, mInputTexture) }
        }
        if (mDisplayFilter == null) {
            initFilters()
        }
        onFilterSizeChanged()
    }

    private fun initFilters() {
        if (mInputFilter == null) {
            mInputFilter = GLImageInputFilter(context)
        } else {
            mInputFilter?.initProgramHandle()
        }

        if (mColorFilter == null && mResourceData != null) {
            mResourceData?.let { createColorFilter(it) }
        } else {
            mColorFilter?.initProgramHandle()
        }

        if (mDisplayFilter == null) {
            mDisplayFilter = GLImageFilter(context)
        } else {
            mDisplayFilter?.initProgramHandle()
        }

        if (mBitmap != null) {
            mMainHandler.post { calculateViewSize() }
        }
    }

    private fun onFilterSizeChanged() {
        mInputFilter?.let {
            it.onInputSizeChanged(mTextureWidth, mTextureHeight)
            it.initFrameBuffer(mTextureWidth, mTextureHeight)
            it.onDisplaySizeChanged(mViewWidth, mViewHeight)
        }
        mColorFilter?.let {
            it.onInputSizeChanged(mTextureWidth, mTextureHeight)
            it.initFrameBuffer(mTextureWidth, mTextureHeight)
            it.onDisplaySizeChanged(mViewWidth, mViewHeight)
        }
        mDisplayFilter?.let {
            it.onInputSizeChanged(mTextureWidth, mTextureHeight)
            it.onDisplaySizeChanged(mViewWidth, mViewHeight)
        }
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES30.glClearColor(0f, 0f, 0f, 0f)
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT or GLES30.GL_DEPTH_BUFFER_BIT)
        val display = mDisplayFilter ?: return

        var currentTexture = mInputTexture
        mInputFilter?.let { currentTexture = it.drawFrameBuffer(currentTexture, mVertexBuffer, mTextureBuffer) }
        mColorFilter?.let { currentTexture = it.drawFrameBuffer(currentTexture, mVertexBuffer, mTextureBuffer) }
        display.drawFrame(currentTexture, mVertexBuffer, mTextureBuffer)

        if (takePicture) {
            val width = width
            val height = height
            val buf = ByteBuffer.allocateDirect(width * height * 4)
            buf.order(ByteOrder.LITTLE_ENDIAN)
            GLES30.glReadPixels(0, 0, width, height, GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE, buf)
            OpenGLUtils.checkGlError("glReadPixels")
            buf.rewind()
            takePicture = false
            mCaptureCallback?.onCapture(buf, width, height)
        }
    }

    fun setFilter(resourceData: ResourceData) {
        mResourceData = resourceData
        queueEvent {
            mColorFilter?.release()
            mColorFilter = null
            createColorFilter(resourceData)
            onFilterSizeChanged()
            requestRender()
        }
    }

    fun setCaptureCallback(captureCallback: CaptureCallback?) {
        mCaptureCallback = captureCallback
    }

    @Synchronized
    fun getCaptureFrame() {
        if (takePicture) {
            Toast.makeText(context, "正在保存图片", Toast.LENGTH_SHORT).show()
            return
        }
        takePicture = true
        requestRender()
    }

    private fun createColorFilter(resourceData: ResourceData) {
        val folderPath = FilterHelper.getFilterDirectory(context) + File.separator + resourceData.unzipFolder
        try {
            val dynamicColor: DynamicColor = ResourceJsonCodec.decodeFilterData(folderPath)
            mColorFilter = GLImageDynamicColorFilter(context, dynamicColor)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setBitmap(bitmap: Bitmap) {
        mBitmap = bitmap
        mTextureWidth = bitmap.width
        mTextureHeight = bitmap.height
        requestRender()
    }

    private fun calculateViewSize() {
        if (mTextureWidth == 0 || mTextureHeight == 0) return
        if (mViewWidth == 0 || mViewHeight == 0) {
            mViewWidth = width
            mViewHeight = height
        }
        val ratio = mTextureWidth.toFloat() / mTextureHeight.toFloat()
        val viewAspectRatio = mViewWidth.toDouble() / mViewHeight.toDouble()
        if (ratio < viewAspectRatio) {
            mViewWidth = (mViewHeight * ratio).toInt()
        } else {
            mViewHeight = (mViewWidth / ratio).toInt()
        }
        val layoutParams: ViewGroup.LayoutParams = layoutParams
        layoutParams.width = mViewWidth
        layoutParams.height = mViewHeight
        this.layoutParams = layoutParams
    }

    interface CaptureCallback {
        fun onCapture(buffer: ByteBuffer?, width: Int, height: Int)
    }
}
