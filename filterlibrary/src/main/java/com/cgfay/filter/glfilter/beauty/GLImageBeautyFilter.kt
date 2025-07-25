package com.cgfay.filter.glfilter.beauty

import android.content.Context
import com.cgfay.filter.glfilter.base.GLImageGaussianBlurFilter
import com.cgfay.filter.glfilter.base.GLImageFilter
import com.cgfay.filter.glfilter.beauty.bean.BeautyParam
import com.cgfay.filter.glfilter.beauty.bean.IBeautify
import com.cgfay.filter.glfilter.utils.OpenGLUtils
import java.nio.FloatBuffer

/**
 * Real-time beauty filter using high pass skin smoothing.
 */
class GLImageBeautyFilter(
    context: Context,
    vertexShader: String? = null,
    fragmentShader: String? = null
) : GLImageFilter(context, vertexShader, fragmentShader), IBeautify {

    private var complexionFilter: GLImageBeautyComplexionFilter? = null
    private var beautyBlurFilter: GLImageBeautyBlurFilter? = null
    private var highPassFilter: GLImageBeautyHighPassFilter? = null
    private var highPassBlurFilter: GLImageGaussianBlurFilter? = null
    private var beautyAdjustFilter: GLImageBeautyAdjustFilter? = null
    private var beautyFaceFilter: GLImageBeautyFaceFilter? = null

    private val blurScale = 0.5f

    init {
        initFilters()
    }

    private fun initFilters() {
        complexionFilter = GLImageBeautyComplexionFilter(mContext)
        beautyBlurFilter = GLImageBeautyBlurFilter(mContext)
        highPassFilter = GLImageBeautyHighPassFilter(mContext)
        highPassBlurFilter = GLImageGaussianBlurFilter(mContext)
        beautyAdjustFilter = GLImageBeautyAdjustFilter(mContext)
        beautyFaceFilter = GLImageBeautyFaceFilter(mContext)
    }

    override fun onInputSizeChanged(width: Int, height: Int) {
        super.onInputSizeChanged(width, height)
        complexionFilter?.onInputSizeChanged(width, height)
        beautyBlurFilter?.onInputSizeChanged((width * blurScale).toInt(), (height * blurScale).toInt())
        highPassFilter?.onInputSizeChanged((width * blurScale).toInt(), (height * blurScale).toInt())
        highPassBlurFilter?.onInputSizeChanged((width * blurScale).toInt(), (height * blurScale).toInt())
        beautyAdjustFilter?.onInputSizeChanged(width, height)
        beautyFaceFilter?.onInputSizeChanged(width, height)
    }

    override fun onDisplaySizeChanged(width: Int, height: Int) {
        super.onDisplaySizeChanged(width, height)
        complexionFilter?.onDisplaySizeChanged(width, height)
        beautyBlurFilter?.onDisplaySizeChanged(width, height)
        highPassFilter?.onDisplaySizeChanged(width, height)
        highPassBlurFilter?.onDisplaySizeChanged(width, height)
        beautyAdjustFilter?.onDisplaySizeChanged(width, height)
        beautyFaceFilter?.onDisplaySizeChanged(width, height)
    }

    override fun drawFrame(textureId: Int, vertexBuffer: FloatBuffer, textureBuffer: FloatBuffer): Boolean {
        if (textureId == OpenGLUtils.GL_NOT_TEXTURE) return false
        var currentTexture = textureId
        val sourceTexture = complexionFilter!!.drawFrameBuffer(currentTexture, vertexBuffer, textureBuffer)
        currentTexture = sourceTexture

        var blurTexture = currentTexture
        var highPassBlurTexture = currentTexture
        beautyBlurFilter?.let {
            blurTexture = it.drawFrameBuffer(currentTexture, vertexBuffer, textureBuffer)
            currentTexture = blurTexture
        }
        highPassFilter?.let {
            it.setBlurTexture(currentTexture)
            currentTexture = it.drawFrameBuffer(sourceTexture, vertexBuffer, textureBuffer)
        }
        highPassBlurFilter?.let {
            highPassBlurTexture = it.drawFrameBuffer(currentTexture, vertexBuffer, textureBuffer)
        }
        beautyAdjustFilter?.let {
            it.setBlurTexture(blurTexture, highPassBlurTexture)
            currentTexture = it.drawFrameBuffer(sourceTexture, vertexBuffer, textureBuffer)
        }
        beautyFaceFilter?.let {
            return it.drawFrame(currentTexture, vertexBuffer, textureBuffer)
        }
        return false
    }

    override fun drawFrameBuffer(textureId: Int, vertexBuffer: FloatBuffer, textureBuffer: FloatBuffer): Int {
        if (textureId == OpenGLUtils.GL_NOT_TEXTURE) return textureId
        var currentTexture = textureId
        val sourceTexture = complexionFilter!!.drawFrameBuffer(currentTexture, vertexBuffer, textureBuffer)
        currentTexture = sourceTexture

        var blurTexture = currentTexture
        var highPassBlurTexture = currentTexture
        beautyBlurFilter?.let {
            blurTexture = it.drawFrameBuffer(currentTexture, vertexBuffer, textureBuffer)
            currentTexture = blurTexture
        }
        highPassFilter?.let {
            it.setBlurTexture(currentTexture)
            currentTexture = it.drawFrameBuffer(sourceTexture, vertexBuffer, textureBuffer)
        }
        highPassBlurFilter?.let {
            highPassBlurTexture = it.drawFrameBuffer(currentTexture, vertexBuffer, textureBuffer)
            currentTexture = highPassBlurTexture
        }
        beautyAdjustFilter?.let {
            currentTexture = sourceTexture
            it.setBlurTexture(blurTexture, highPassBlurTexture)
            currentTexture = it.drawFrameBuffer(currentTexture, vertexBuffer, textureBuffer)
        }
        beautyFaceFilter?.let {
            currentTexture = it.drawFrameBuffer(currentTexture, vertexBuffer, textureBuffer)
        }
        return currentTexture
    }

    override fun initFrameBuffer(width: Int, height: Int) {
        super.initFrameBuffer(width, height)
        complexionFilter?.initFrameBuffer(width, height)
        beautyBlurFilter?.initFrameBuffer((width * blurScale).toInt(), (height * blurScale).toInt())
        highPassFilter?.initFrameBuffer((width * blurScale).toInt(), (height * blurScale).toInt())
        highPassBlurFilter?.initFrameBuffer((width * blurScale).toInt(), (height * blurScale).toInt())
        beautyAdjustFilter?.initFrameBuffer(width, height)
        beautyFaceFilter?.initFrameBuffer(width, height)
    }

    override fun destroyFrameBuffer() {
        super.destroyFrameBuffer()
        complexionFilter?.destroyFrameBuffer()
        beautyBlurFilter?.destroyFrameBuffer()
        highPassFilter?.destroyFrameBuffer()
        highPassBlurFilter?.destroyFrameBuffer()
        beautyAdjustFilter?.destroyFrameBuffer()
        beautyFaceFilter?.destroyFrameBuffer()
    }

    override fun release() {
        super.release()
        complexionFilter?.release(); complexionFilter = null
        beautyBlurFilter?.release(); beautyBlurFilter = null
        highPassFilter?.release(); highPassFilter = null
        highPassBlurFilter?.release(); highPassBlurFilter = null
        beautyAdjustFilter?.release(); beautyAdjustFilter = null
        beautyFaceFilter?.release(); beautyFaceFilter = null
    }

    override fun onBeauty(beauty: BeautyParam) {
        complexionFilter?.setComplexionLevel(beauty.complexionIntensity)
        beautyAdjustFilter?.setSkinBeautyIntensity(beauty.beautyIntensity)
        beautyFaceFilter?.onBeauty(beauty)
    }
}
