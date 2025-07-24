package com.cgfay.caincamera.renderer

import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.opengl.EGL14
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.util.Log
import android.view.Surface
import com.cgfay.caincamera.viewmodel.RecordViewModel
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
 * Renderer supporting duet recording.
 */
class DuetRecordRenderer(viewModel: RecordViewModel) : GLSurfaceView.Renderer {

    private val weakViewModel = WeakReference(viewModel)

    private var inputFilter: GLImageOESInputFilter? = null
    private var duetFilter: GLImageDuetFilter? = null
    private var imageFilter: GLImageFilter? = null

    private lateinit var vertexBuffer: FloatBuffer
    private lateinit var textureBuffer: FloatBuffer
    private lateinit var displayVertexBuffer: FloatBuffer
    private lateinit var displayTextureBuffer: FloatBuffer

    protected var textureWidth = 0
    protected var textureHeight = 0
    protected var viewWidth = 0
    protected var viewHeight = 0

    private var inputTexture = OpenGLUtils.GL_NOT_TEXTURE
    @Volatile private var needToAttach = false
    private var weakSurfaceTexture: WeakReference<SurfaceTexture>? = null
    private val matrix = FloatArray(16)

    private var duetType = DuetType.DUET_TYPE_NONE
    private var mvpMatrix = FloatArray(16)
    private var flip = false
    private var duetVideo: MediaData? = null
    private var videoWidth = 0
    private var videoHeight = 0
    private var videoInputTexture = OpenGLUtils.GL_NOT_TEXTURE
    private var videoInputFilter: GLImageOESInputFilter? = null
    private lateinit var duetVertexBuffer: FloatBuffer
    private lateinit var duetTextureBuffer: FloatBuffer
    private var videoSurface: Surface? = null
    private var videoSurfaceTexture: SurfaceTexture? = null
    private var mediaPlayer: MediaPlayer? = null

    init {
        Matrix.setIdentityM(mvpMatrix, 0)
    }

    fun setDuetVideo(mediaData: MediaData) {
        duetVideo = mediaData
        duetType = DuetType.DUET_TYPE_LEFT_RIGHT
        if (mediaData.orientation == 90 || mediaData.orientation == 270) {
            videoWidth = mediaData.height
            videoHeight = mediaData.width
        } else {
            videoWidth = mediaData.width
            videoHeight = mediaData.height
        }
        Log.d(TAG, "setDuetVideo - video width: $videoWidth, video height: $videoHeight")
    }

    override fun onSurfaceCreated(gl: GL10, config: EGLConfig) {
        vertexBuffer = OpenGLUtils.createFloatBuffer(TextureRotationUtils.CubeVertices)
        textureBuffer = OpenGLUtils.createFloatBuffer(TextureRotationUtils.TextureVertices)
        duetVertexBuffer = OpenGLUtils.createFloatBuffer(TextureRotationUtils.CubeVertices)
        duetTextureBuffer = OpenGLUtils.createFloatBuffer(TextureRotationUtils.TextureVertices)
        displayVertexBuffer = OpenGLUtils.createFloatBuffer(TextureRotationUtils.CubeVertices)
        displayTextureBuffer = OpenGLUtils.createFloatBuffer(TextureRotationUtils.TextureVertices)

        GLES30.glDisable(GL10.GL_DITHER)
        GLES30.glClearColor(0f, 0f, 0f, 0f)
        GLES30.glEnable(GL10.GL_CULL_FACE)
        GLES30.glEnable(GL10.GL_DEPTH_TEST)
        initFilters()
        weakViewModel.get()?.onBindSharedContext(EGL14.eglGetCurrentContext())
        initMediaPlayer()
    }

    override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {
        viewWidth = width
        viewHeight = height
        onFilterSizeChanged()
        adjustDisplayCoordinateSize()
    }

    override fun onDrawFrame(gl: GL10) {
        val texture = weakSurfaceTexture?.get() ?: return

        var timeStamp = 0L
        synchronized(this) {
            updateSurfaceTexture(texture)
            timeStamp = texture.timestamp
        }

        GLES30.glClearColor(0f, 0f, 0f, 1f)
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT or GLES30.GL_DEPTH_BUFFER_BIT)
        val input = inputFilter ?: return
        val image = imageFilter ?: return
        input.setTextureTransformMatrix(matrix)
        var currentTexture = inputTexture
        currentTexture = input.drawFrameBuffer(currentTexture, vertexBuffer, textureBuffer)
        currentTexture = drawDuetTexture(currentTexture)
        image.drawFrame(currentTexture, displayVertexBuffer, displayTextureBuffer)
        weakViewModel.get()?.onRecordFrameAvailable(currentTexture, timeStamp)
    }

    private fun resetInputCoordinateSize() {
        val vertexCoord = TextureRotationUtils.CubeVertices
        val textureVertices = TextureRotationUtils.TextureVertices
        duetVertexBuffer.clear()
        duetVertexBuffer.put(vertexCoord).position(0)
        duetTextureBuffer.clear()
        duetTextureBuffer.put(textureVertices).position(0)
    }

    private fun drawVideoToFrameBuffer(): Int {
        videoSurfaceTexture?.let {
            it.updateTexImage()
            it.getTransformMatrix(matrix)
        }
        var videoTexture = videoInputTexture
        videoInputFilter?.let { filter ->
            filter.setTextureTransformMatrix(matrix)
            val videoRatio = videoWidth.toDouble() / videoHeight
            resetInputCoordinateSize()
            if (videoRatio < 9f / 16f && duetType == DuetType.DUET_TYPE_LEFT_RIGHT) {
                adjustVideoCoordinate()
            }
            videoTexture = filter.drawFrameBuffer(videoInputTexture, duetVertexBuffer, duetTextureBuffer)
        }
        return videoTexture
    }

    private fun drawDuetTexture(currentTexture: Int): Int {
        val videoTexture = drawVideoToFrameBuffer()
        resetInputCoordinateSize()
        duetFilter?.let { filter ->
            filter.bindFrameBuffer()
            GLES30.glClearColor(0f, 0f, 0f, 1f)
            GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)
            when (duetType) {
                DuetType.DUET_TYPE_LEFT_RIGHT -> {
                    drawPreviewLeftRight(currentTexture, flip)
                    drawVideoLeftRight(videoTexture, !flip)
                }
                DuetType.DUET_TYPE_UP_DOWN -> {
                    drawPreviewUpDown(currentTexture, flip)
                    drawVideoUpDown(videoTexture, !flip)
                }
                DuetType.DUET_TYPE_BIG_SMALL -> {
                    if (!flip) {
                        drawPreviewBigSmall(currentTexture, false)
                        drawVideoBigSmall(videoTexture, true)
                    } else {
                        drawVideoBigSmall(videoTexture, false)
                        drawPreviewBigSmall(currentTexture, true)
                    }
                }
                else -> {
                    Matrix.setIdentityM(mvpMatrix, 0)
                    filter.setMVPMatrix(mvpMatrix)
                    filter.onDrawTexture(currentTexture, vertexBuffer, textureBuffer)
                }
            }
            return filter.unBindFrameBuffer()
        }
        return currentTexture
    }

    private fun drawPreviewLeftRight(currentTexture: Int, drawRight: Boolean) {
        Matrix.setIdentityM(mvpMatrix, 0)
        Matrix.scaleM(mvpMatrix, 0, 0.5f, 0.5f, 0f)
        Matrix.translateM(mvpMatrix, 0, if (drawRight) 1f else -1f, 0f, 0f)
        duetFilter?.setDuetType(0f)
        duetFilter?.setOffsetX(0f)
        duetFilter?.setOffsetY(0f)
        duetFilter?.setMVPMatrix(mvpMatrix)
        duetFilter?.onDrawTexture(currentTexture, duetVertexBuffer, duetTextureBuffer)
    }

    private fun drawVideoLeftRight(videoTexture: Int, drawRight: Boolean) {
        Matrix.setIdentityM(mvpMatrix, 0)
        val videoRatio = videoWidth.toDouble() / videoHeight
        if (videoRatio < 9f / 16f) {
            val scaleX = textureWidth * 0.5f / videoWidth
            val scaleY = textureHeight * 0.5f / videoHeight
            val scale = maxOf(scaleX, scaleY)
            Matrix.scaleM(mvpMatrix, 0, scale, scale, 0f)
        } else {
            val scaleX = textureWidth * 0.5f / videoWidth
            val scaleY = textureHeight * 0.5f / videoHeight
            val scale = minOf(scaleX, scaleY)
            val width = scale * videoWidth
            val height = scale * videoHeight
            Matrix.scaleM(mvpMatrix, 0, (width / textureWidth).toFloat(), (height / textureHeight).toFloat(), 0f)
        }
        Matrix.translateM(mvpMatrix, 0, if (drawRight) 1f else -1f, 0f, 0f)
        duetFilter?.setDuetType(0f)
        duetFilter?.setOffsetX(0f)
        duetFilter?.setOffsetY(0f)
        duetFilter?.setMVPMatrix(mvpMatrix)
        duetFilter?.onDrawTexture(videoTexture, duetVertexBuffer, duetTextureBuffer)
    }

    private fun drawPreviewUpDown(currentTexture: Int, drawUp: Boolean) {
        Matrix.setIdentityM(mvpMatrix, 0)
        duetFilter?.setDuetType(1f)
        duetFilter?.setOffsetX(0f)
        duetFilter?.setOffsetY(if (drawUp) -0.25f else 0.25f)
        duetFilter?.setMVPMatrix(mvpMatrix)
        duetFilter?.onDrawTexture(currentTexture, duetVertexBuffer, duetTextureBuffer)
    }

    private fun drawVideoUpDown(videoTexture: Int, drawUp: Boolean) {
        Matrix.setIdentityM(mvpMatrix, 0)
        val videoRatio = videoWidth.toDouble() / videoHeight
        if (videoRatio <= 9f / 16f) {
            val scaleX = textureWidth.toDouble() / videoWidth
            val scaleY = textureHeight.toDouble() / videoHeight
            val scale = maxOf(scaleX, scaleY)
            val width = scale * videoWidth
            val height = scale * videoHeight
            val maxOffsetY = kotlin.math.abs(height - textureHeight * 0.5) / textureHeight
            var offset = 0.25f
            if (offset >= maxOffsetY) offset = maxOffsetY.toFloat()
            else if (offset <= -maxOffsetY) offset = (-maxOffsetY).toFloat()
            Matrix.scaleM(mvpMatrix, 0, (width / textureWidth).toFloat(), (height / textureHeight).toFloat(), 0f)
            Matrix.translateM(mvpMatrix, 0, 0f, if (drawUp) 1f else -1f, 0f)
            duetFilter?.setDuetType(1f)
            duetFilter?.setOffsetX(0f)
            duetFilter?.setOffsetY(if (drawUp) 0 + offset else -maxOffsetY.toFloat() + offset)
        } else {
            val scaleX = textureWidth * 0.5f / videoWidth
            val scaleY = textureHeight * 0.5f / videoHeight
            val scale = maxOf(scaleX, scaleY)
            val width = scale * videoWidth
            val height = scale * videoHeight
            val maxOffsetX = kotlin.math.abs(width - textureWidth) / textureWidth
            var offset = 0f
            if (offset > maxOffsetX) offset = maxOffsetX
            else if (offset <= -maxOffsetX) offset = -maxOffsetX
            Matrix.translateM(mvpMatrix, 0, offset, 0f, 0f)
            Matrix.scaleM(mvpMatrix, 0, (width / textureWidth).toFloat(), (height / textureHeight).toFloat(), 0f)
            Matrix.translateM(mvpMatrix, 0, 0f, if (drawUp) 1f else -1f, 0f)
            duetFilter?.setDuetType(1f)
            duetFilter?.setOffsetX(0f)
            duetFilter?.setOffsetY(0f)
        }
        duetFilter?.setMVPMatrix(mvpMatrix)
        duetFilter?.onDrawTexture(videoTexture, duetVertexBuffer, duetTextureBuffer)
    }

    private fun drawPreviewBigSmall(currentTexture: Int, drawSmall: Boolean) {
        Matrix.setIdentityM(mvpMatrix, 0)
        if (drawSmall) {
            val scaleX = 1f / 3f
            val scaleY = 1f / 3f
            val left = 30f
            val top = textureHeight - scaleX * textureHeight - 30f
            val ratioX = (left - 0.5f * (1 - scaleX) * textureWidth) / (textureWidth * scaleX)
            val ratioY = (top - 0.5f * (1 - scaleY) * textureHeight) / (textureHeight * scaleY)
            Matrix.scaleM(mvpMatrix, 0, scaleX, scaleY, 0f)
            Matrix.translateM(mvpMatrix, 0, ratioX * 2f, ratioY * 2f, 0f)
        }
        duetFilter?.setDuetType(0f)
        duetFilter?.setOffsetX(0f)
        duetFilter?.setOffsetY(0f)
        duetFilter?.setMVPMatrix(mvpMatrix)
        duetFilter?.onDrawTexture(currentTexture, duetVertexBuffer, duetTextureBuffer)
    }

    private fun drawVideoBigSmall(videoTexture: Int, drawSmall: Boolean) {
        Matrix.setIdentityM(mvpMatrix, 0)
        val videoRatio = videoWidth.toDouble() / videoHeight
        if (drawSmall) {
            val scaleX = 1f / 3f
            val scaleY = 1f / 3f
            val left = 0f
            val top = videoHeight * 2f / 3f
            val ratioX = (left - 0.5f * (1 - scaleX) * textureWidth) / (textureWidth * scaleX)
            val ratioY = (top - 0.5f * (1 - scaleY) * textureHeight) / (textureHeight * scaleY)
            Matrix.scaleM(mvpMatrix, 0, 0.3f, 0.3f, 0f)
            Matrix.translateM(mvpMatrix, 0, ratioX * 2f, ratioY * 2f, 0f)
        } else {
            if (videoRatio < 9f / 16f) {
                val scaleX = textureWidth.toDouble() / videoWidth
                val scaleY = textureHeight.toDouble() / videoHeight
                val scale = maxOf(scaleX, scaleY)
                Matrix.scaleM(mvpMatrix, 0, scale.toFloat(), scale.toFloat(), 0f)
                adjustVideoCoordinate()
            } else {
                val scaleX = textureWidth.toDouble() / videoWidth
                val scaleY = textureHeight.toDouble() / videoHeight
                val scale = minOf(scaleX, scaleY)
                Matrix.scaleM(mvpMatrix, 0, scale.toFloat(), scale.toFloat(), 0f)
            }
        }
        duetFilter?.setDuetType(0f)
        duetFilter?.setOffsetX(0f)
        duetFilter?.setOffsetY(0f)
        duetFilter?.setMVPMatrix(mvpMatrix)
        duetFilter?.onDrawTexture(videoTexture, duetVertexBuffer, duetTextureBuffer)
    }

    private fun adjustVideoCoordinate() {
        val textureVertices = TextureRotationUtils.TextureVertices
        val ratio = videoWidth.toFloat() / videoHeight
        val contentWidth = if (ratio < 9f / 16f) videoWidth else (videoHeight * 9f / 16f).toInt()
        val contentHeight = if (ratio < 9f / 16f) (videoWidth * 16f / 9f).toInt() else videoHeight
        val scaleX = contentWidth.toFloat() / videoWidth
        val scaleY = contentHeight.toFloat() / videoHeight
        val distHorizontal = (1 - scaleX) / 2
        val distVertical = (1 - scaleY) / 2
        val textureCoord = floatArrayOf(
            addDistance(textureVertices[0], distHorizontal), addDistance(textureVertices[1], distVertical),
            addDistance(textureVertices[2], distHorizontal), addDistance(textureVertices[3], distVertical),
            addDistance(textureVertices[4], distHorizontal), addDistance(textureVertices[5], distVertical),
            addDistance(textureVertices[6], distHorizontal), addDistance(textureVertices[7], distVertical)
        )
        duetTextureBuffer.clear()
        duetTextureBuffer.put(textureCoord).position(0)
    }

    private fun updateSurfaceTexture(surfaceTexture: SurfaceTexture) {
        synchronized(this) {
            if (needToAttach) {
                if (inputTexture != OpenGLUtils.GL_NOT_TEXTURE) {
                    OpenGLUtils.deleteTexture(inputTexture)
                }
                inputTexture = OpenGLUtils.createOESTexture()
                try {
                    surfaceTexture.attachToGLContext(inputTexture)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                needToAttach = false
            }
        }
        try {
            surfaceTexture.updateTexImage()
            surfaceTexture.getTransformMatrix(matrix)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun bindSurfaceTexture(surfaceTexture: SurfaceTexture) {
        synchronized(this) {
            if (weakSurfaceTexture == null || weakSurfaceTexture?.get() !== surfaceTexture) {
                weakSurfaceTexture = WeakReference(surfaceTexture)
                needToAttach = true
            }
        }
    }

    fun setTextureSize(width: Int, height: Int) {
        textureWidth = width
        textureHeight = height
        if (viewWidth != 0 && viewHeight != 0) {
            onFilterSizeChanged()
            adjustDisplayCoordinateSize()
        }
    }

    private fun initFilters() {
        val context = weakViewModel.get()?.activity ?: return
        inputFilter = GLImageOESInputFilter(context)
        videoInputFilter = GLImageOESInputFilter(context)
        duetFilter = GLImageDuetFilter(context)
        imageFilter = GLImageMirrorFilter(context)
    }

    private fun onFilterSizeChanged() {
        inputFilter?.let {
            it.onInputSizeChanged(textureWidth, textureHeight)
            it.initFrameBuffer(textureWidth, textureHeight)
            it.onDisplaySizeChanged(viewWidth, viewHeight)
        }
        videoInputFilter?.let {
            it.onInputSizeChanged(videoWidth, videoHeight)
            it.initFrameBuffer(videoWidth, videoHeight)
            it.onDisplaySizeChanged(textureWidth, textureHeight)
        }
        duetFilter?.let {
            it.onInputSizeChanged(textureWidth, textureHeight)
            it.initFrameBuffer(textureWidth, textureHeight)
            it.onDisplaySizeChanged(viewWidth, viewHeight)
        }
        imageFilter?.let {
            it.onInputSizeChanged(textureWidth, textureHeight)
            it.onDisplaySizeChanged(viewWidth, viewHeight)
        }
    }

    private fun adjustDisplayCoordinateSize() {
        val vertexCoord = TextureRotationUtils.CubeVertices
        val textureVertices = TextureRotationUtils.TextureVertices
        val ratioMax = maxOf(viewWidth.toFloat() / textureWidth, viewHeight.toFloat() / textureHeight)
        val imageWidth = textureWidth * ratioMax
        val imageHeight = textureHeight * ratioMax
        val ratioWidth = imageWidth / viewWidth
        val ratioHeight = imageHeight / viewHeight
        val distHorizontal = (1 - 1 / ratioWidth) / 2
        val distVertical = (1 - 1 / ratioHeight) / 2
        val textureCoord = floatArrayOf(
            addDistance(textureVertices[0], distHorizontal), addDistance(textureVertices[1], distVertical),
            addDistance(textureVertices[2], distHorizontal), addDistance(textureVertices[3], distVertical),
            addDistance(textureVertices[4], distHorizontal), addDistance(textureVertices[5], distVertical),
            addDistance(textureVertices[6], distHorizontal), addDistance(textureVertices[7], distVertical)
        )
        displayVertexBuffer.clear()
        displayVertexBuffer.put(vertexCoord).position(0)
        displayTextureBuffer.clear()
        displayTextureBuffer.put(textureCoord).position(0)
    }

    private fun addDistance(coordinate: Float, distance: Float): Float =
        if (coordinate == 0f) distance else 1 - distance

    fun clear() {
        weakSurfaceTexture?.clear()
    }

    fun setDuetType(type: DuetType) {
        duetType = type
        flip = false
    }

    fun flip() {
        flip = !flip
    }

    private fun initMediaPlayer() {
        val video = duetVideo ?: return
        videoInputTexture = OpenGLUtils.createOESTexture()
        videoSurfaceTexture = SurfaceTexture(videoInputTexture)
        videoSurface = Surface(videoSurfaceTexture)
        mediaPlayer = MediaPlayer()
        try {
            val path = MediaMetadataUtils.getPath(weakViewModel.get()?.activity, video.contentUri)
            mediaPlayer?.setDataSource(path)
            mediaPlayer?.setSurface(videoSurface)
            mediaPlayer?.isLooping = true
            mediaPlayer?.prepare()
            mediaPlayer?.seekTo(0)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun playVideo() {
        mediaPlayer?.start()
    }

    fun stopVideo() {
        mediaPlayer?.pause()
    }

    companion object {
        private const val TAG = "DuetRecordRenderer"
    }
}
