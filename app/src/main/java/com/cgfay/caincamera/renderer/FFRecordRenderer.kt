package com.cgfay.caincamera.renderer

import android.graphics.SurfaceTexture
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.util.Log
import com.cgfay.caincamera.viewmodel.FFMediaRecordViewModel
import com.cgfay.filter.glfilter.base.GLImageFilter
import com.cgfay.filter.glfilter.base.GLImageOESInputFilter
import com.cgfay.filter.glfilter.utils.OpenGLUtils
import com.cgfay.filter.glfilter.utils.TextureRotationUtils
import java.lang.ref.WeakReference
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * Renderer used for FFmpeg based recording.
 */
class FFRecordRenderer(viewModel: FFMediaRecordViewModel) : GLSurfaceView.Renderer {
    private val weakViewModel = WeakReference(viewModel)

    private var inputFilter: GLImageOESInputFilter? = null
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
    private val matrix = FloatArray(16)
    @Volatile private var needToAttach = false
    private var weakSurfaceTexture: WeakReference<SurfaceTexture>? = null

    override fun onSurfaceCreated(gl: GL10, config: EGLConfig) {
        if (VERBOSE) Log.d(TAG, "onSurfaceCreated: ")
        vertexBuffer = OpenGLUtils.createFloatBuffer(TextureRotationUtils.CubeVertices)
        textureBuffer = OpenGLUtils.createFloatBuffer(TextureRotationUtils.TextureVertices)
        displayVertexBuffer = OpenGLUtils.createFloatBuffer(TextureRotationUtils.CubeVertices)
        displayTextureBuffer = OpenGLUtils.createFloatBuffer(TextureRotationUtils.TextureVertices)

        GLES30.glDisable(GL10.GL_DITHER)
        GLES30.glClearColor(0f, 0f, 0f, 0f)
        GLES30.glEnable(GL10.GL_CULL_FACE)
        GLES30.glEnable(GL10.GL_DEPTH_TEST)
        initFilters()
    }

    override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {
        if (VERBOSE) Log.d(TAG, "onSurfaceChanged: ")
        viewWidth = width
        viewHeight = height
        onFilterSizeChanged()
        adjustCoordinateSize()
    }

    override fun onDrawFrame(gl: GL10) {
        val texture = weakSurfaceTexture?.get() ?: return
        synchronized(this) { updateSurfaceTexture(texture) }
        GLES30.glClearColor(0f, 0f, 0f, 0f)
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT or GLES30.GL_DEPTH_BUFFER_BIT)
        val input = inputFilter ?: return
        val image = imageFilter ?: return
        input.setTextureTransformMatrix(matrix)
        var currentTexture = inputTexture
        currentTexture = input.drawFrameBuffer(currentTexture, vertexBuffer, textureBuffer)
        image.drawFrame(currentTexture, displayVertexBuffer, displayTextureBuffer)
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
                    needToAttach = false
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        try {
            surfaceTexture.updateTexImage()
            surfaceTexture.getTransformMatrix(matrix)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (VERBOSE) Log.d(TAG, "updateSurfaceTexture: ")
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
            adjustCoordinateSize()
        }
        if (VERBOSE) Log.d(TAG, "setTextureSize: width = $width, height = $height")
    }

    private fun initFilters() {
        val context = weakViewModel.get()?.activity ?: return
        inputFilter = GLImageOESInputFilter(context)
        imageFilter = GLImageFilter(context)
    }

    private fun onFilterSizeChanged() {
        inputFilter?.let {
            it.onInputSizeChanged(textureWidth, textureHeight)
            it.initFrameBuffer(textureWidth, textureHeight)
            it.onDisplaySizeChanged(viewWidth, viewHeight)
        }
        imageFilter?.let {
            it.onInputSizeChanged(textureWidth, textureHeight)
            it.onDisplaySizeChanged(viewWidth, viewHeight)
        }
    }

    private fun adjustCoordinateSize() {
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

    companion object {
        private const val TAG = "FFRecordRenderer"
        private const val VERBOSE = false
    }
}
