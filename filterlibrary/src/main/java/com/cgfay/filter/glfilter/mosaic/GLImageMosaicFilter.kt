package com.cgfay.filter.glfilter.mosaic

import android.content.Context
import android.opengl.GLES30
import com.cgfay.filter.glfilter.base.GLImageFilter
import com.cgfay.filter.glfilter.utils.OpenGLUtils

/**
 * 方形马赛克滤镜
 */
class GLImageMosaicFilter(
    context: Context,
    vertexShader: String = VERTEX_SHADER,
    fragmentShader: String = OpenGLUtils.getShaderFromAssets(
        context,
        "shader/mosaic/fragment_mosaic.glsl"
    )
) : GLImageFilter(context, vertexShader, fragmentShader) {

    private val imageWidthFactorLoc: Int
    private val imageHeightFactorLoc: Int
    private val mosaicSizeLoc: Int

    private var imageWidthFactor = 0f
    private var imageHeightFactor = 0f
    private var mosaicSize = 1.0f // 马赛克大小，1 ~ imagewidth/imageHeight

    init {
        imageWidthFactorLoc = GLES30.glGetUniformLocation(mProgramHandle, "imageWidthFactor")
        imageHeightFactorLoc = GLES30.glGetUniformLocation(mProgramHandle, "imageHeightFactor")
        mosaicSizeLoc = GLES30.glGetUniformLocation(mProgramHandle, "mosaicSize")
    }

    override fun onInputSizeChanged(width: Int, height: Int) {
        super.onInputSizeChanged(width, height)
        imageWidthFactor = 1.0f / width
        imageHeightFactor = 1.0f / height
    }

    override fun onDrawFrameBegin() {
        super.onDrawFrameBegin()
        GLES30.glUniform1f(mosaicSizeLoc, mosaicSize)
        GLES30.glUniform1f(imageWidthFactorLoc, imageWidthFactor)
        GLES30.glUniform1f(imageHeightFactorLoc, imageHeightFactor)
    }

    /** 设置马赛克大小 */
    fun setMosaicSize(size: Float) {
        mosaicSize = size
    }
}
