package com.cgfay.filter.glfilter.beauty

import android.content.Context
import android.opengl.GLES30
import com.cgfay.filter.glfilter.base.GLImageFilter
import com.cgfay.filter.glfilter.beauty.bean.BeautyParam
import com.cgfay.filter.glfilter.beauty.bean.IBeautify
import com.cgfay.filter.glfilter.utils.OpenGLUtils
import java.nio.FloatBuffer

/**
 * Legacy beauty filter implementation.
 */
class GLImageOldBeautyFilter(
    context: Context,
    vertexShader: String = VERTEX_SHADER,
    fragmentShader: String = OpenGLUtils.getShaderFromAssets(
        context,
        "shader/beauty/fragment_old_beauty.glsl"
    )
) : GLImageFilter(context, vertexShader, fragmentShader), IBeautify {

    private var widthLoc = 0
    private var heightLoc = 0
    private var opacityLoc = 0

    private val blurScale = 0.5f

    private var complexionBeautyFilter: GLImageBeautyComplexionFilter? = GLImageBeautyComplexionFilter(context)

    override fun initProgramHandle() {
        super.initProgramHandle()
        widthLoc = GLES30.glGetUniformLocation(mProgramHandle, "width")
        heightLoc = GLES30.glGetUniformLocation(mProgramHandle, "height")
        opacityLoc = GLES30.glGetUniformLocation(mProgramHandle, "opacity")
        setSkinBeautyLevel(1.0f)
    }

    override fun onInputSizeChanged(width: Int, height: Int) {
        super.onInputSizeChanged((width * blurScale).toInt(), (height * blurScale).toInt())
        setInteger(widthLoc, (width * blurScale).toInt())
        setInteger(heightLoc, (height * blurScale).toInt())
        complexionBeautyFilter?.onInputSizeChanged(width, height)
    }

    override fun onDisplaySizeChanged(width: Int, height: Int) {
        super.onDisplaySizeChanged(width, height)
        complexionBeautyFilter?.onDisplaySizeChanged(width, height)
    }

    override fun drawFrame(textureId: Int, vertexBuffer: FloatBuffer, textureBuffer: FloatBuffer): Boolean {
        var currentTexture = textureId
        complexionBeautyFilter?.let {
            currentTexture = it.drawFrameBuffer(currentTexture, vertexBuffer, textureBuffer)
        }
        return super.drawFrame(currentTexture, vertexBuffer, textureBuffer)
    }

    override fun drawFrameBuffer(textureId: Int, vertexBuffer: FloatBuffer, textureBuffer: FloatBuffer): Int {
        var currentTexture = textureId
        complexionBeautyFilter?.let {
            currentTexture = it.drawFrameBuffer(currentTexture, vertexBuffer, textureBuffer)
        }
        return super.drawFrameBuffer(currentTexture, vertexBuffer, textureBuffer)
    }

    override fun initFrameBuffer(width: Int, height: Int) {
        super.initFrameBuffer((width * blurScale).toInt(), (height * blurScale).toInt())
        complexionBeautyFilter?.initFrameBuffer(width, height)
    }

    override fun destroyFrameBuffer() {
        super.destroyFrameBuffer()
        complexionBeautyFilter?.destroyFrameBuffer()
    }

    override fun release() {
        super.release()
        complexionBeautyFilter?.release()
        complexionBeautyFilter = null
    }

    override fun onBeauty(beauty: BeautyParam) {
        complexionBeautyFilter?.setComplexionLevel(beauty.complexionIntensity)
        setSkinBeautyLevel(beauty.beautyIntensity)
    }

    fun setSkinBeautyLevel(percent: Float) {
        val opacity = if (percent <= 0) 0.0f else calculateOpacity(percent)
        setFloat(opacityLoc, opacity)
    }

    private fun calculateOpacity(percent: Float): Float {
        var p = percent
        if (p > 1.0f) {
            p = 1.0f
        }
        return (1.0f - (1.0f - p + 0.02f) / 2.0f)
    }
}
