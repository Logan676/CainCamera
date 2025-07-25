package com.cgfay.filter.glfilter.mosaic

import android.content.Context
import android.opengl.GLES30
import com.cgfay.filter.glfilter.base.GLImageFilter
import com.cgfay.filter.glfilter.utils.OpenGLUtils

/**
 * 圆形马赛克
 */
class GLImageMosaicCircleFilter(
    context: Context,
    vertexShader: String = VERTEX_SHADER,
    fragmentShader: String = OpenGLUtils.getShaderFromAssets(
        context,
        "shader/mosaic/fragment_mosaic_circle.glsl"
    )
) : GLImageFilter(context, vertexShader, fragmentShader) {

    private var imageWidthHandle = 0
    private var imageHeightHandle = 0
    private var mosaicSizeLoc = 0

    private var mosaicSize = 0f

    override fun initProgramHandle() {
        super.initProgramHandle()
        if (mProgramHandle != OpenGLUtils.GL_NOT_INIT) {
            imageWidthHandle = GLES30.glGetUniformLocation(mProgramHandle, "imageWidth")
            imageHeightHandle = GLES30.glGetUniformLocation(mProgramHandle, "imageHeight")
            mosaicSizeLoc = GLES30.glGetUniformLocation(mProgramHandle, "mosaicSize")
            setMosaicSize(30.0f)
        }
    }

    override fun onDrawFrameBegin() {
        super.onDrawFrameBegin()
        GLES30.glUniform1f(mosaicSizeLoc, mosaicSize)
        GLES30.glUniform1f(imageWidthHandle, mImageWidth.toFloat())
        GLES30.glUniform1f(imageHeightHandle, mImageHeight.toFloat())
    }

    fun setMosaicSize(size: Float) {
        mosaicSize = size
    }
}
