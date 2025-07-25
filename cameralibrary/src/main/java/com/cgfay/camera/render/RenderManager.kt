package com.cgfay.camera.render

import android.content.Context
import android.util.Log
import android.util.SparseArray
import android.view.MotionEvent
import com.badlogic.gdx.math.Vector3
import com.cgfay.camera.camera.CameraParam
import com.cgfay.filter.glfilter.base.GLImageDepthBlurFilter
import com.cgfay.filter.glfilter.base.GLImageFilter
import com.cgfay.filter.glfilter.base.GLImageOESInputFilter
import com.cgfay.filter.glfilter.base.GLImageVignetteFilter
import com.cgfay.filter.glfilter.beauty.GLImageBeautyFilter
import com.cgfay.filter.glfilter.beauty.bean.IBeautify
import com.cgfay.filter.glfilter.color.GLImageDynamicColorFilter
import com.cgfay.filter.glfilter.color.bean.DynamicColor
import com.cgfay.filter.glfilter.face.GLImageFacePointsFilter
import com.cgfay.filter.glfilter.face.GLImageFaceReshapeFilter
import com.cgfay.filter.glfilter.makeup.GLImageMakeupFilter
import com.cgfay.filter.glfilter.makeup.bean.DynamicMakeup
import com.cgfay.filter.glfilter.multiframe.GLImageFrameEdgeBlurFilter
import com.cgfay.filter.glfilter.stickers.GLImageDynamicStickerFilter
import com.cgfay.filter.glfilter.stickers.GestureHelp
import com.cgfay.filter.glfilter.stickers.StaticStickerNormalFilter
import com.cgfay.filter.glfilter.stickers.bean.DynamicSticker
import com.cgfay.filter.glfilter.utils.OpenGLUtils
import com.cgfay.filter.glfilter.utils.TextureRotationUtils
import com.cgfay.landmark.LandmarkEngine
import java.nio.FloatBuffer

/**
 * Manager handling rendering filters and buffers.
 */
class RenderManager {

    private val filterArrays = SparseArray<GLImageFilter>()
    private var scaleType = ScaleType.CENTER_CROP
    private var vertexBuffer: FloatBuffer? = null
    private var textureBuffer: FloatBuffer? = null
    private var displayVertexBuffer: FloatBuffer? = null
    private var displayTextureBuffer: FloatBuffer? = null
    private var viewWidth = 0
    private var viewHeight = 0
    private var textureWidth = 0
    private var textureHeight = 0
    private val cameraParam = CameraParam.getInstance()
    private var context: Context? = null

    fun init(context: Context) {
        initBuffers()
        initFilters(context)
        this.context = context
    }

    fun release() {
        releaseBuffers()
        releaseFilters()
        context = null
    }

    private fun releaseFilters() {
        for (i in 0 until RenderIndex.NumberIndex) {
            filterArrays[i]?.release()
        }
        filterArrays.clear()
    }

    private fun releaseBuffers() {
        vertexBuffer?.clear(); vertexBuffer = null
        textureBuffer?.clear(); textureBuffer = null
        displayVertexBuffer?.clear(); displayVertexBuffer = null
        displayTextureBuffer?.clear(); displayTextureBuffer = null
    }

    private fun initBuffers() {
        releaseBuffers()
        displayVertexBuffer = OpenGLUtils.createFloatBuffer(TextureRotationUtils.CubeVertices)
        displayTextureBuffer = OpenGLUtils.createFloatBuffer(TextureRotationUtils.TextureVertices)
        vertexBuffer = OpenGLUtils.createFloatBuffer(TextureRotationUtils.CubeVertices)
        textureBuffer = OpenGLUtils.createFloatBuffer(TextureRotationUtils.TextureVertices)
    }

    private fun initFilters(context: Context) {
        releaseFilters()
        filterArrays.put(RenderIndex.CameraIndex, GLImageOESInputFilter(context))
        filterArrays.put(RenderIndex.BeautyIndex, GLImageBeautyFilter(context))
        filterArrays.put(RenderIndex.MakeupIndex, GLImageMakeupFilter(context, null))
        filterArrays.put(RenderIndex.FaceAdjustIndex, GLImageFaceReshapeFilter(context))
        filterArrays.put(RenderIndex.FilterIndex, null)
        filterArrays.put(RenderIndex.ResourceIndex, null)
        filterArrays.put(RenderIndex.DepthBlurIndex, GLImageDepthBlurFilter(context))
        filterArrays.put(RenderIndex.VignetteIndex, GLImageVignetteFilter(context))
        filterArrays.put(RenderIndex.DisplayIndex, GLImageFilter(context))
        filterArrays.put(RenderIndex.FacePointIndex, GLImageFacePointsFilter(context))
    }

    @Synchronized
    fun changeEdgeBlurFilter(enableEdgeBlur: Boolean) {
        filterArrays[RenderIndex.DisplayIndex]?.release()
        val filter: GLImageFilter = if (enableEdgeBlur) {
            GLImageFrameEdgeBlurFilter(context)
        } else {
            GLImageFilter(context)
        }
        filter.onInputSizeChanged(textureWidth, textureHeight)
        filter.onDisplaySizeChanged(viewWidth, viewHeight)
        filterArrays.put(RenderIndex.DisplayIndex, filter)
    }

    @Synchronized
    fun changeDynamicFilter(color: DynamicColor?) {
        filterArrays[RenderIndex.FilterIndex]?.release()
        filterArrays.put(RenderIndex.FilterIndex, null)
        if (color == null) return
        val filter = GLImageDynamicColorFilter(context, color)
        filter.onInputSizeChanged(textureWidth, textureHeight)
        filter.initFrameBuffer(textureWidth, textureHeight)
        filter.onDisplaySizeChanged(viewWidth, viewHeight)
        filterArrays.put(RenderIndex.FilterIndex, filter)
    }

    @Synchronized
    fun changeDynamicMakeup(dynamicMakeup: DynamicMakeup?) {
        val exist = filterArrays[RenderIndex.MakeupIndex]
        if (exist != null) {
            (exist as GLImageMakeupFilter).changeMakeupData(dynamicMakeup)
        } else {
            val filter = GLImageMakeupFilter(context, dynamicMakeup)
            filter.onInputSizeChanged(textureWidth, textureHeight)
            filter.initFrameBuffer(textureWidth, textureHeight)
            filter.onDisplaySizeChanged(viewWidth, viewHeight)
            filterArrays.put(RenderIndex.MakeupIndex, filter)
        }
    }

    @Synchronized
    fun changeDynamicResource(color: DynamicColor?) {
        filterArrays[RenderIndex.ResourceIndex]?.release()
        filterArrays.put(RenderIndex.ResourceIndex, null)
        if (color == null) return
        val filter = GLImageDynamicColorFilter(context, color)
        filter.onInputSizeChanged(textureWidth, textureHeight)
        filter.initFrameBuffer(textureWidth, textureHeight)
        filter.onDisplaySizeChanged(viewWidth, viewHeight)
        filterArrays.put(RenderIndex.ResourceIndex, filter)
    }

    @Synchronized
    fun changeDynamicResource(sticker: DynamicSticker?) {
        filterArrays[RenderIndex.ResourceIndex]?.release()
        filterArrays.put(RenderIndex.ResourceIndex, null)
        if (sticker == null) return
        val filter = GLImageDynamicStickerFilter(context, sticker)
        filter.onInputSizeChanged(textureWidth, textureHeight)
        filter.initFrameBuffer(textureWidth, textureHeight)
        filter.onDisplaySizeChanged(viewWidth, viewHeight)
        filterArrays.put(RenderIndex.ResourceIndex, filter)
    }

    fun drawFrame(inputTexture: Int, matrix: FloatArray?): Int {
        var currentTexture = inputTexture
        val cameraFilter = filterArrays[RenderIndex.CameraIndex]
        val displayFilter = filterArrays[RenderIndex.DisplayIndex]
        if (cameraFilter == null || displayFilter == null) return currentTexture
        if (cameraFilter is GLImageOESInputFilter && matrix != null) {
            cameraFilter.setTextureTransformMatrix(matrix)
        }
        currentTexture = cameraFilter.drawFrameBuffer(currentTexture, vertexBuffer, textureBuffer)
        if (!cameraParam.showCompare) {
            filterArrays[RenderIndex.BeautyIndex]?.let {
                if (it is IBeautify && cameraParam.beauty != null) {
                    it.onBeauty(cameraParam.beauty)
                }
                currentTexture = it.drawFrameBuffer(currentTexture, vertexBuffer, textureBuffer)
            }
            filterArrays[RenderIndex.MakeupIndex]?.let {
                currentTexture = it.drawFrameBuffer(currentTexture, vertexBuffer, textureBuffer)
            }
            filterArrays[RenderIndex.FaceAdjustIndex]?.let {
                if (it is IBeautify) {
                    it.onBeauty(cameraParam.beauty)
                }
                currentTexture = it.drawFrameBuffer(currentTexture, vertexBuffer, textureBuffer)
            }
            filterArrays[RenderIndex.FilterIndex]?.let {
                currentTexture = it.drawFrameBuffer(currentTexture, vertexBuffer, textureBuffer)
            }
            filterArrays[RenderIndex.ResourceIndex]?.let {
                currentTexture = it.drawFrameBuffer(currentTexture, vertexBuffer, textureBuffer)
            }
            filterArrays[RenderIndex.DepthBlurIndex]?.let {
                it.setFilterEnable(cameraParam.enableDepthBlur)
                currentTexture = it.drawFrameBuffer(currentTexture, vertexBuffer, textureBuffer)
            }
            filterArrays[RenderIndex.VignetteIndex]?.let {
                it.setFilterEnable(cameraParam.enableVignette)
                currentTexture = it.drawFrameBuffer(currentTexture, vertexBuffer, textureBuffer)
            }
        }
        displayFilter.drawFrame(currentTexture, displayVertexBuffer, displayTextureBuffer)
        return currentTexture
    }

    fun drawFacePoint(currentTexture: Int) {
        filterArrays[RenderIndex.FacePointIndex]?.let {
            if (cameraParam.drawFacePoints && LandmarkEngine.getInstance().hasFace()) {
                it.drawFrame(currentTexture, displayVertexBuffer, displayTextureBuffer)
            }
        }
    }

    fun setTextureSize(width: Int, height: Int) {
        textureWidth = width
        textureHeight = height
        if (viewWidth != 0 && viewHeight != 0) {
            adjustCoordinateSize()
            onFilterChanged()
        }
    }

    fun getTextureWidth() = textureWidth

    fun getTextureHeight() = textureHeight

    fun setDisplaySize(width: Int, height: Int) {
        viewWidth = width
        viewHeight = height
        if (textureWidth != 0 && textureHeight != 0) {
            adjustCoordinateSize()
            onFilterChanged()
        }
    }

    private fun onFilterChanged() {
        for (i in 0 until RenderIndex.NumberIndex) {
            filterArrays[i]?.let { filter ->
                filter.onInputSizeChanged(textureWidth, textureHeight)
                if (i < RenderIndex.DisplayIndex) {
                    filter.initFrameBuffer(textureWidth, textureHeight)
                }
                filter.onDisplaySizeChanged(viewWidth, viewHeight)
            }
        }
    }

    private fun adjustCoordinateSize() {
        var textureCoord: FloatArray? = null
        var vertexCoord: FloatArray? = null
        val textureVertices = TextureRotationUtils.TextureVertices
        val vertexVertices = TextureRotationUtils.CubeVertices
        val ratioMax = maxOf(viewWidth.toFloat() / textureWidth, viewHeight.toFloat() / textureHeight)
        val imageWidth = (textureWidth * ratioMax).toInt()
        val imageHeight = (textureHeight * ratioMax).toInt()
        val ratioWidth = imageWidth.toFloat() / viewWidth
        val ratioHeight = imageHeight.toFloat() / viewHeight
        if (scaleType == ScaleType.CENTER_INSIDE) {
            vertexCoord = floatArrayOf(
                vertexVertices[0] / ratioHeight, vertexVertices[1] / ratioWidth,
                vertexVertices[2] / ratioHeight, vertexVertices[3] / ratioWidth,
                vertexVertices[4] / ratioHeight, vertexVertices[5] / ratioWidth,
                vertexVertices[6] / ratioHeight, vertexVertices[7] / ratioWidth
            )
        } else if (scaleType == ScaleType.CENTER_CROP) {
            val distHorizontal = (1 - 1 / ratioWidth) / 2
            val distVertical = (1 - 1 / ratioHeight) / 2
            textureCoord = floatArrayOf(
                addDistance(textureVertices[0], distHorizontal), addDistance(textureVertices[1], distVertical),
                addDistance(textureVertices[2], distHorizontal), addDistance(textureVertices[3], distVertical),
                addDistance(textureVertices[4], distHorizontal), addDistance(textureVertices[5], distVertical),
                addDistance(textureVertices[6], distHorizontal), addDistance(textureVertices[7], distVertical)
            )
        }
        if (vertexCoord == null) {
            vertexCoord = vertexVertices
        }
        if (textureCoord == null) {
            textureCoord = textureVertices
        }
        if (displayVertexBuffer == null || displayTextureBuffer == null) {
            initBuffers()
        }
        displayVertexBuffer!!.clear()
        displayVertexBuffer!!.put(vertexCoord).position(0)
        displayTextureBuffer!!.clear()
        displayTextureBuffer!!.put(textureCoord).position(0)
    }

    private fun addDistance(coordinate: Float, distance: Float): Float = if (coordinate == 0.0f) distance else 1 - distance

    fun touchDown(e: MotionEvent): StaticStickerNormalFilter? {
        filterArrays[RenderIndex.ResourceIndex]?.let { filter ->
            if (filter is GLImageDynamicStickerFilter) {
                tempVec.set(e.x, e.y, 0f)
                val sticker = GestureHelp.hit(tempVec, filter.filters)
                if (sticker != null) {
                    Log.d("touchSticker", "找到贴纸")
                } else {
                    Log.d("touchSticker", "没有贴纸")
                }
                return sticker
            }
        }
        return null
    }

    companion object {
        val tempVec = Vector3()
    }
}
