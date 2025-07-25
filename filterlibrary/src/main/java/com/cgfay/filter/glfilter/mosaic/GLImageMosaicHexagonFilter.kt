package com.cgfay.filter.glfilter.mosaic

import android.content.Context
import android.opengl.GLES30
import com.cgfay.filter.glfilter.base.GLImageFilter
import com.cgfay.filter.glfilter.utils.OpenGLUtils
import kotlin.math.min

/**
 * 六边形马赛克滤镜
 */
class GLImageMosaicHexagonFilter(
    context: Context,
    vertexShader: String = VERTEX_SHADER,
    fragmentShader: String = OpenGLUtils.getShaderFromAssets(
        context,
        "shader/mosaic/fragment_mosaic_hexagon.glsl"
    )
) : GLImageFilter(context, vertexShader, fragmentShader) {

    private var mosaicSizeHandle = 0
    private var mosaicSize = 0f

    override fun initProgramHandle() {
        super.initProgramHandle()
        if (mProgramHandle != OpenGLUtils.GL_NOT_INIT) {
            mosaicSizeHandle = GLES30.glGetUniformLocation(mProgramHandle, "mosaicSize")
            setMosaicSize(30.0f)
        }
    }

    override fun onDrawFrameBegin() {
        super.onDrawFrameBegin()
        val minSize = min(mImageWidth, mImageHeight).toFloat()
        GLES30.glUniform1f(mosaicSizeHandle, mosaicSize * (1.0f / minSize))
    }

    fun setMosaicSize(size: Float) {
        mosaicSize = size
    }
}
