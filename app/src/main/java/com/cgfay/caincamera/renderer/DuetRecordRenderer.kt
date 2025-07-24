package com.cgfay.caincamera.renderer

import android.graphics.SurfaceTexture
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.opengl.EGL14
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.util.Log
import android.view.Surface
import androidx.annotation.NonNull
import com.cgfay.caincamera.presenter.RecordPresenter
import com.cgfay.filter.glfilter.adjust.GLImageMirrorFilter
import com.cgfay.filter.glfilter.base.GLImageFilter
import com.cgfay.filter.glfilter.base.GLImageOESInputFilter
import com.cgfay.filter.glfilter.utils.OpenGLUtils
import com.cgfay.filter.glfilter.utils.TextureRotationUtils
import com.cgfay.picker.model.MediaData
import com.cgfay.picker.utils.MediaMetadataUtils
import java.io.IOException
import java.lang.ref.WeakReference
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * 同框录制渲染器
 */
class DuetRecordRenderer(presenter: RecordPresenter) : GLSurfaceView.Renderer {

    companion object {
        private const val TAG = "DuetRecordRenderer"
    }

    private var mInputFilter: GLImageOESInputFilter? = null // OES输入滤镜
    private var mDuetFilter: GLImageDuetFilter? = null // 同框滤镜
    private var mImageFilter: GLImageFilter? = null // 输出滤镜
    private lateinit var mVertexBuffer: FloatBuffer
    private lateinit var mTextureBuffer: FloatBuffer
    private lateinit var mDisplayVertexBuffer: FloatBuffer
    private lateinit var mDisplayTextureBuffer: FloatBuffer
    protected var mTextureWidth = 0
    protected var mTextureHeight = 0
    protected var mViewWidth = 0
    protected var mViewHeight = 0
    private var mInputTexture = OpenGLUtils.GL_NOT_TEXTURE
    @Volatile private var mNeedToAttach = false
    private var mWeakSurfaceTexture: WeakReference<SurfaceTexture>? = null
    private val mMatrix = FloatArray(16)
    private val mWeakPresenter = WeakReference(presenter)

    private var mDuetType = DuetType.DUET_TYPE_NONE
    private var mMVPMatrix = FloatArray(16)
    private var mFlip = false
    private var mDuetVideo: MediaData? = null
    private var mVideoWidth = 0
    private var mVideoHeight = 0
    private var mVideoInputTexture = OpenGLUtils.GL_NOT_TEXTURE
    private var mVideoInputFilter: GLImageOESInputFilter? = null
    private lateinit var mDuetVertexBuffer: FloatBuffer
    private lateinit var mDuetTextureBuffer: FloatBuffer
    private var mVideoSurface: Surface? = null
    private var mVideoSurfaceTexture: SurfaceTexture? = null
    private var mMediaPlayer: MediaPlayer? = null

    init {
        Matrix.setIdentityM(mMVPMatrix, 0)
    }

    /** 设置同框视频 */
    fun setDuetVideo(@NonNull mediaData: MediaData) {
        mDuetVideo = mediaData
        mDuetType = DuetType.DUET_TYPE_LEFT_RIGHT
        if (mediaData.orientation == 90 || mediaData.orientation == 270) {
            mVideoWidth = mediaData.height
            mVideoHeight = mediaData.width
        } else {
            mVideoWidth = mediaData.width
            mVideoHeight = mediaData.height
        }
        Log.d(TAG, "setDuetVideo - video width: $mVideoWidth, video height: $mVideoHeight")
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        mVertexBuffer = OpenGLUtils.createFloatBuffer(TextureRotationUtils.CubeVertices)
        mTextureBuffer = OpenGLUtils.createFloatBuffer(TextureRotationUtils.TextureVertices)
        mDuetVertexBuffer = OpenGLUtils.createFloatBuffer(TextureRotationUtils.CubeVertices)
        mDuetTextureBuffer = OpenGLUtils.createFloatBuffer(TextureRotationUtils.TextureVertices)
        mDisplayVertexBuffer = OpenGLUtils.createFloatBuffer(TextureRotationUtils.CubeVertices)
        mDisplayTextureBuffer = OpenGLUtils.createFloatBuffer(TextureRotationUtils.TextureVertices)

        GLES30.glDisable(GL10.GL_DITHER)
        GLES30.glClearColor(0f, 0f, 0f, 0f)
        GLES30.glEnable(GL10.GL_CULL_FACE)
        GLES30.glEnable(GL10.GL_DEPTH_TEST)
        initFilters()
        mWeakPresenter.get()?.onBindSharedContext(EGL14.eglGetCurrentContext())
        initMediaPlayer()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        mViewWidth = width
        mViewHeight = height
        onFilterSizeChanged()
        adjustDisplayCoordinateSize()
    }

    override fun onDrawFrame(gl: GL10?) {
        val surfaceTexture = mWeakSurfaceTexture?.get() ?: return

        // 更新纹理
        val timeStamp: Long
        synchronized(this) {
            updateSurfaceTexture(surfaceTexture)
            timeStamp = surfaceTexture.timestamp
        }

        GLES30.glClearColor(0f, 0f, 0f, 1.0f)
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT or GLES30.GL_DEPTH_BUFFER_BIT)
        val inputFilter = mInputFilter ?: return
        val imageFilter = mImageFilter ?: return
        inputFilter.setTextureTransformMatrix(mMatrix)

        // 将OES纹理绘制到FBO中
        var currentTexture = mInputTexture
        currentTexture = inputFilter.drawFrameBuffer(currentTexture, mVertexBuffer, mTextureBuffer)
        // 绘制同框
        currentTexture = drawDuetTexture(currentTexture)
        // 将最终的结果会是预览
        imageFilter.drawFrame(currentTexture, mDisplayVertexBuffer, mDisplayTextureBuffer)
        // 录制视频
        mWeakPresenter.get()?.onRecordFrameAvailable(currentTexture, timeStamp)
    }

    private fun resetInputCoordinateSize() {
        val vertexCoord = TextureRotationUtils.CubeVertices
        val textureVertices = TextureRotationUtils.TextureVertices
        mDuetVertexBuffer.clear()
        mDuetVertexBuffer.put(vertexCoord).position(0)
        mDuetTextureBuffer.clear()
        mDuetTextureBuffer.put(textureVertices).position(0)
    }

    /** 将同框视频绘制到FBO中 */
    private fun drawVideoToFrameBuffer(): Int {
        mVideoSurfaceTexture?.let {
            it.updateTexImage()
            it.getTransformMatrix(mMatrix)
        }
        var videoTexture = mVideoInputTexture
        mVideoInputFilter?.let {
            it.setTextureTransformMatrix(mMatrix)
            val videoRatio = mVideoWidth.toDouble() / mVideoHeight
            resetInputCoordinateSize()
            if (videoRatio < 9f / 16f && mDuetType == DuetType.DUET_TYPE_LEFT_RIGHT) {
                adjustVideoCoordinate()
            }
            videoTexture = it.drawFrameBuffer(mVideoInputTexture, mDuetVertexBuffer, mDuetTextureBuffer)
        }
        return videoTexture
    }

    /** 绘制同框纹理 */
    private fun drawDuetTexture(currentTexture: Int): Int {
        val videoTexture = drawVideoToFrameBuffer()
        resetInputCoordinateSize()
        var texture = currentTexture
        mDuetFilter?.let { filter ->
            filter.bindFrameBuffer()
            GLES30.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
            GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)
            when (mDuetType) {
                DuetType.DUET_TYPE_LEFT_RIGHT -> {
                    drawPreviewLeftRight(texture, mFlip)
                    drawVideoLeftRight(videoTexture, !mFlip)
                }
                DuetType.DUET_TYPE_UP_DOWN -> {
                    drawPreviewUpDown(texture, mFlip)
                    drawVideoUpDown(videoTexture, !mFlip)
                }
                DuetType.DUET_TYPE_BIG_SMALL -> {
                    if (!mFlip) {
                        drawPreviewBigSmall(texture, false)
                        drawVideoBigSmall(videoTexture, true)
                    } else {
                        drawVideoBigSmall(videoTexture, false)
                        drawPreviewBigSmall(texture, true)
                    }
                }
                else -> {
                    Matrix.setIdentityM(mMVPMatrix, 0)
                    filter.setMVPMatrix(mMVPMatrix)
                    filter.onDrawTexture(texture, mVertexBuffer, mTextureBuffer)
                }
            }
            texture = filter.unBindFrameBuffer()
        }
        return texture
    }

    private fun drawPreviewLeftRight(currentTexture: Int, drawRight: Boolean) {
        Matrix.setIdentityM(mMVPMatrix, 0)
        Matrix.scaleM(mMVPMatrix, 0, 0.5f, 0.5f, 0f)
        Matrix.translateM(mMVPMatrix, 0, if (drawRight) 1f else -1f, 0f, 0f)
        mDuetFilter?.apply {
            setDuetType(0f)
            setOffsetX(0f)
            setOffsetY(0f)
            setMVPMatrix(mMVPMatrix)
            onDrawTexture(currentTexture, mDuetVertexBuffer, mDuetTextureBuffer)
        }
    }

    private fun drawVideoLeftRight(videoTexture: Int, drawRight: Boolean) {
        Matrix.setIdentityM(mMVPMatrix, 0)
        val videoRatio = mVideoWidth.toDouble() / mVideoHeight
        if (videoRatio < 9f / 16f) {
            val scaleX = mTextureWidth * 0.5f / mVideoWidth
            val scaleY = mTextureHeight * 0.5f / mVideoHeight
            val scale = maxOf(scaleX, scaleY)
            Matrix.scaleM(mMVPMatrix, 0, scale, scale, 0f)
        } else {
            val scaleX = mTextureWidth * 0.5 / mVideoWidth
            val scaleY = mTextureHeight * 0.5 / mVideoHeight
            val scale = minOf(scaleX, scaleY)
            val width = scale * mVideoWidth
            val height = scale * mVideoHeight
            Matrix.scaleM(mMVPMatrix, 0, (width / mTextureWidth).toFloat(), (height / mTextureHeight).toFloat(), 0f)
        }
        Matrix.translateM(mMVPMatrix, 0, if (drawRight) 1f else -1f, 0f, 0f)
        mDuetFilter?.apply {
            setDuetType(0f)
            setOffsetX(0f)
            setOffsetY(0f)
            setMVPMatrix(mMVPMatrix)
            onDrawTexture(videoTexture, mDuetVertexBuffer, mDuetTextureBuffer)
        }
    }

    private fun drawPreviewUpDown(currentTexture: Int, drawUp: Boolean) {
        Matrix.setIdentityM(mMVPMatrix, 0)
        mDuetFilter?.apply {
            setDuetType(1f)
            setOffsetX(0f)
            setOffsetY(if (drawUp) -0.25f else 0.25f)
            setMVPMatrix(mMVPMatrix)
            onDrawTexture(currentTexture, mDuetVertexBuffer, mDuetTextureBuffer)
        }
    }

    private fun drawVideoUpDown(videoTexture: Int, drawUp: Boolean) {
        Matrix.setIdentityM(mMVPMatrix, 0)
        val videoRatio = mVideoWidth.toDouble() / mVideoHeight
        if (videoRatio <= 9f / 16f) {
            val scaleX = mTextureWidth.toDouble() / mVideoWidth
            val scaleY = mTextureHeight.toDouble() / mVideoHeight
            val scale = maxOf(scaleX, scaleY)
            val width = scale * mVideoWidth
            val height = scale * mVideoHeight
            val maxOffsetY = kotlin.math.abs(height - mTextureHeight * 0.5) / mTextureHeight
            var offset = 0.25f
            offset = when {
                offset >= maxOffsetY -> maxOffsetY.toFloat()
                offset <= -maxOffsetY -> (-maxOffsetY).toFloat()
                else -> offset
            }
            Matrix.scaleM(mMVPMatrix, 0, (width / mTextureWidth).toFloat(), (height / mTextureHeight).toFloat(), 0f)
            Matrix.translateM(mMVPMatrix, 0, 0f, if (drawUp) 1f else -1f, 0f)
            mDuetFilter?.setDuetType(1f)
            mDuetFilter?.setOffsetX(0f)
            mDuetFilter?.setOffsetY(if (drawUp) 0 + offset else -maxOffsetY.toFloat() + offset)
        } else {
            val scaleX = mTextureWidth * 0.5f / mVideoWidth
            val scaleY = mTextureHeight * 0.5f / mVideoHeight
            val scale = maxOf(scaleX, scaleY)
            val width = scale * mVideoWidth
            val height = scale * mVideoHeight
            val maxOffsetX = kotlin.math.abs(width - mTextureWidth.toFloat()) / mTextureWidth
            var offset = 0f
            offset = when {
                offset > maxOffsetX -> maxOffsetX
                offset <= -maxOffsetX -> -maxOffsetX
                else -> offset
            }
            Matrix.translateM(mMVPMatrix, 0, offset, 0f, 0f)
            Matrix.scaleM(mMVPMatrix, 0, (width / mTextureWidth).toFloat(), (height / mTextureHeight).toFloat(), 0f)
            Matrix.translateM(mMVPMatrix, 0, 0f, if (drawUp) 1f else -1f, 0f)
            mDuetFilter?.setDuetType(1f)
            mDuetFilter?.setOffsetX(0f)
            mDuetFilter?.setOffsetY(0f)
        }
        mDuetFilter?.setMVPMatrix(mMVPMatrix)
        mDuetFilter?.onDrawTexture(videoTexture, mDuetVertexBuffer, mDuetTextureBuffer)
    }

    private fun drawPreviewBigSmall(currentTexture: Int, drawSmall: Boolean) {
        Matrix.setIdentityM(mMVPMatrix, 0)
        if (drawSmall) {
            val scaleX = 1f / 3f
            val scaleY = 1f / 3f
            val left = 30f
            val top = mTextureHeight - scaleX * mTextureHeight - 30f
            val ratioX = (left - 0.5f * (1 - scaleX) * mTextureWidth) / (mTextureWidth * scaleX)
            val ratioY = (top - 0.5f * (1 - scaleY) * mTextureHeight) / (mTextureHeight * scaleY)
            Matrix.scaleM(mMVPMatrix, 0, scaleX, scaleY, 0f)
            Matrix.translateM(mMVPMatrix, 0, ratioX * 2f, ratioY * 2f, 0f)
        }
        mDuetFilter?.apply {
            setDuetType(0f)
            setOffsetX(0f)
            setOffsetY(0f)
            setMVPMatrix(mMVPMatrix)
            onDrawTexture(currentTexture, mDuetVertexBuffer, mDuetTextureBuffer)
        }
    }

    private fun drawVideoBigSmall(videoTexture: Int, drawSmall: Boolean) {
        Matrix.setIdentityM(mMVPMatrix, 0)
        val videoRatio = mVideoWidth.toDouble() / mVideoHeight
        if (drawSmall) {
            val scaleX = 1f / 3f
            val scaleY = 1f / 3f
            val left = 0f
            val top = mVideoHeight * 2f / 3f
            val ratioX = (left - 0.5f * (1 - scaleX) * mTextureWidth) / (mTextureWidth * scaleX)
            val ratioY = (top - 0.5f * (1 - scaleY) * mTextureHeight) / (mTextureHeight * scaleY)
            Matrix.scaleM(mMVPMatrix, 0, 0.3f, 0.3f, 0f)
            Matrix.translateM(mMVPMatrix, 0, ratioX * 2f, ratioY * 2f, 0f)
        } else {
            if (videoRatio < 9f / 16f) {
                val scaleX = mTextureWidth.toDouble() / mVideoWidth
                val scaleY = mTextureHeight.toDouble() / mVideoHeight
                val scale = maxOf(scaleX, scaleY)
                Matrix.scaleM(mMVPMatrix, 0, scale.toFloat(), scale.toFloat(), 0f)
                adjustVideoCoordinate()
            } else {
                val scaleX = mTextureWidth.toDouble() / mVideoWidth
                val scaleY = mTextureHeight.toDouble() / mVideoHeight
                val scale = minOf(scaleX, scaleY)
                val width = scale * mVideoWidth
                val height = scale * mVideoHeight
                Matrix.scaleM(mMVPMatrix, 0, (width / mTextureWidth).toFloat(), (height / mTextureHeight).toFloat(), 0f)
            }
        }
        mDuetFilter?.apply {
            setDuetType(0f)
            setOffsetX(0f)
            setOffsetY(0f)
            setMVPMatrix(mMVPMatrix)
            onDrawTexture(videoTexture, mDuetVertexBuffer, mDuetTextureBuffer)
        }
    }

    /** 裁剪视频纹理，让视频纹理绘制到9/16区间内 */
    private fun adjustVideoCoordinate() {
        val vertexCoord = TextureRotationUtils.CubeVertices
        val textureVertices = TextureRotationUtils.TextureVertices
        val ratioMax = maxOf(
            mTextureWidth.toFloat() / mVideoWidth,
            mTextureHeight.toFloat() / mVideoHeight
        )
        val imageWidth = mVideoWidth * ratioMax
        val imageHeight = mVideoHeight * ratioMax
        val ratioWidth = imageWidth / mTextureWidth.toFloat()
        val ratioHeight = imageHeight / mTextureHeight.toFloat()
        val distHorizontal = (1 - 1 / ratioWidth) / 2
        val distVertical = (1 - 1 / ratioHeight) / 2
        val textureCoord = floatArrayOf(
            addDistance(textureVertices[0], distHorizontal), addDistance(textureVertices[1], distVertical),
            addDistance(textureVertices[2], distHorizontal), addDistance(textureVertices[3], distVertical),
            addDistance(textureVertices[4], distHorizontal), addDistance(textureVertices[5], distVertical),
            addDistance(textureVertices[6], distHorizontal), addDistance(textureVertices[7], distVertical)
        )
        mDuetVertexBuffer.clear()
        mDuetVertexBuffer.put(vertexCoord).position(0)
        mDuetTextureBuffer.clear()
        mDuetTextureBuffer.put(textureCoord).position(0)
    }

    /** 更新输入纹理 */
    private fun updateSurfaceTexture(@NonNull surfaceTexture: SurfaceTexture) {
        if (mNeedToAttach) {
            if (mInputTexture != OpenGLUtils.GL_NOT_TEXTURE) {
                OpenGLUtils.deleteTexture(mInputTexture)
            }
            mInputTexture = OpenGLUtils.createOESTexture()
            surfaceTexture.attachToGLContext(mInputTexture)
            mNeedToAttach = false
        }
        surfaceTexture.updateTexImage()
        surfaceTexture.getTransformMatrix(mMatrix)
    }

    /** 绑定纹理 */
    fun bindSurfaceTexture(surfaceTexture: SurfaceTexture) {
        synchronized(this) {
            mWeakSurfaceTexture = WeakReference(surfaceTexture)
            mNeedToAttach = true
        }
    }

    /** 设置纹理大小 */
    fun setTextureSize(width: Int, height: Int) {
        mTextureWidth = width
        mTextureHeight = height
        if (mViewWidth != 0 && mViewHeight != 0) {
            onFilterSizeChanged()
            adjustDisplayCoordinateSize()
        }
    }

    /** 初始化滤镜 */
    private fun initFilters() {
        val activity = mWeakPresenter.get()?.activity ?: return
        mInputFilter = GLImageOESInputFilter(activity)
        mVideoInputFilter = GLImageOESInputFilter(activity)
        mDuetFilter = GLImageDuetFilter(activity)
        mImageFilter = GLImageMirrorFilter(activity)
    }

    /** 更新滤镜纹理大小和显示大小 */
    private fun onFilterSizeChanged() {
        mInputFilter?.let {
            it.onInputSizeChanged(mTextureWidth, mTextureHeight)
            it.initFrameBuffer(mTextureWidth, mTextureHeight)
            it.onDisplaySizeChanged(mViewWidth, mViewHeight)
        }
        mVideoInputFilter?.let {
            it.onInputSizeChanged(mVideoWidth, mVideoHeight)
            it.initFrameBuffer(mVideoWidth, mVideoHeight)
            it.onDisplaySizeChanged(mTextureWidth, mTextureHeight)
        }
        mDuetFilter?.let {
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
    private fun adjustDisplayCoordinateSize() {
        val vertexCoord = TextureRotationUtils.CubeVertices
        val textureVertices = TextureRotationUtils.TextureVertices
        val ratioMax = maxOf(
            mViewWidth.toFloat() / mTextureWidth,
            mViewHeight.toFloat() / mTextureHeight
        )
        val imageWidth = mTextureWidth * ratioMax
        val imageHeight = mTextureHeight * ratioMax
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
        mDisplayVertexBuffer.clear()
        mDisplayVertexBuffer.put(vertexCoord).position(0)
        mDisplayTextureBuffer.clear()
        mDisplayTextureBuffer.put(textureCoord).position(0)
    }

    private fun addDistance(coordinate: Float, distance: Float): Float {
        return if (coordinate == 0.0f) distance else 1 - distance
    }

    /** 清理一些缓存数据 */
    fun clear() {
        mWeakSurfaceTexture?.clear()
    }

    /** 设置同框类型 */
    fun setDuetType(type: DuetType) {
        mDuetType = type
        mFlip = false
    }

    /** 翻转 */
    fun flip() {
        mFlip = !mFlip
    }

    /** 初始化播放器 */
    private fun initMediaPlayer() {
        val duetVideo = mDuetVideo ?: return
        mVideoInputTexture = OpenGLUtils.createOESTexture()
        mVideoSurfaceTexture = SurfaceTexture(mVideoInputTexture)
        mVideoSurface = Surface(mVideoSurfaceTexture)
        mMediaPlayer = MediaPlayer()
        try {
            val context = mWeakPresenter.get()?.activity
            val path = MediaMetadataUtils.getPath(context, duetVideo.contentUri)
            mMediaPlayer?.setDataSource(path)
            mMediaPlayer?.setSurface(mVideoSurface)
            mMediaPlayer?.isLooping = true
            mMediaPlayer?.prepare()
            mMediaPlayer?.seekTo(0)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    /** 开始播放 */
    fun playVideo() {
        mMediaPlayer?.start()
    }

    /** 暂停播放 */
    fun stopVideo() {
        mMediaPlayer?.pause()
    }
}
