package com.cgfay.filter.glfilter.adjust

import android.content.Context
import android.opengl.GLES30
import com.cgfay.filter.glfilter.base.GLImageFilter
import com.cgfay.filter.glfilter.utils.OpenGLUtils

/**
 * 对比度
 */
class GLImageContrastFilter(context: Context) : GLImageFilter(
    context,
    VERTEX_SHADER,
    OpenGLUtils.getShaderFromAssets(context, "shader/adjust/fragment_contrast.glsl")
) {

    private var contrastHandle = 0
    private var contrast = 1f

    override fun initProgramHandle() {
        super.initProgramHandle()
        contrastHandle = GLES30.glGetUniformLocation(mProgramHandle, "contrast")
        setContrast(1.0f)
    }

    /**
     * 设置对比度 0.0 ~ 4.0, 默认1.0
     */
    fun setContrast(contrast: Float) {
        var value = contrast
        if (value < 0.0f) {
            value = 0.0f
        } else if (value > 4.0f) {
            value = 4.0f
        }
        this.contrast = value
        setFloat(contrastHandle, this.contrast)
    }
}
