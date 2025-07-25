package com.cgfay.filter.glfilter.beauty

import android.content.Context
import android.opengl.GLES30
import com.cgfay.filter.glfilter.base.GLImageFilter
import com.cgfay.filter.glfilter.utils.OpenGLUtils

/**
 * Filter responsible for skin complexion adjustment.
 */
class GLImageBeautyComplexionFilter(
    context: Context,
    vertexShader: String = VERTEX_SHADER,
    fragmentShader: String = OpenGLUtils.getShaderFromAssets(
        context,
        "shader/beauty/fragment_beauty_complexion.glsl"
    )
) : GLImageFilter(context, vertexShader, fragmentShader) {

    private var grayTextureLoc = 0
    private var lookupTextureLoc = 0
    private var levelRangeInvLoc = 0
    private var levelBlackLoc = 0
    private var alphaLoc = 0

    private var grayTexture = 0
    private var lookupTexture = 0

    private var levelRangeInv = 0f
    private var levelBlack = 0f
    private var alpha = 1.0f

    override fun initProgramHandle() {
        super.initProgramHandle()
        grayTextureLoc = GLES30.glGetUniformLocation(mProgramHandle, "grayTexture")
        lookupTextureLoc = GLES30.glGetUniformLocation(mProgramHandle, "lookupTexture")
        levelRangeInvLoc = GLES30.glGetUniformLocation(mProgramHandle, "levelRangeInv")
        levelBlackLoc = GLES30.glGetUniformLocation(mProgramHandle, "levelBlack")
        alphaLoc = GLES30.glGetUniformLocation(mProgramHandle, "alpha")
        createTexture()
        levelRangeInv = 1.040816f
        levelBlack = 0.01960784f
        alpha = 1.0f
    }

    private fun createTexture() {
        grayTexture = OpenGLUtils.createTextureFromAssets(mContext, "texture/skin_gray.png")
        lookupTexture = OpenGLUtils.createTextureFromAssets(mContext, "texture/skin_lookup.png")
    }

    override fun onDrawFrameBegin() {
        super.onDrawFrameBegin()
        OpenGLUtils.bindTexture(grayTextureLoc, grayTexture, 1)
        OpenGLUtils.bindTexture(lookupTextureLoc, lookupTexture, 2)
        GLES30.glUniform1f(levelRangeInvLoc, levelRangeInv)
        GLES30.glUniform1f(levelBlackLoc, levelBlack)
        GLES30.glUniform1f(alphaLoc, alpha)
    }

    override fun release() {
        super.release()
        GLES30.glDeleteTextures(2, intArrayOf(grayTexture, lookupTexture), 0)
    }

    /**
     * Set complexion level.
     * @param level 0 ~ 1.0f
     */
    fun setComplexionLevel(level: Float) {
        alpha = level
    }
}
