package com.cgfay.filter.glfilter.adjust

import android.content.Context
import android.opengl.GLES30
import com.cgfay.filter.glfilter.base.GLImageFilter
import com.cgfay.filter.glfilter.utils.OpenGLUtils

/**
 * 色调
 * 色调是色彩的类别，比如红绿蓝，取决于彩色光的光谱成分。
 */
class GLImageHueFilter(context: Context) : GLImageFilter(
    context,
    VERTEX_SHADER,
    OpenGLUtils.getShaderFromAssets(context, "shader/adjust/fragment_hue.glsl")
) {

    private var hueAdjustHandle = 0
    private var hue = 0f

    override fun initProgramHandle() {
        super.initProgramHandle()
        hueAdjustHandle = GLES30.glGetUniformLocation(mProgramHandle, "hueAdjust")
        setHue(0f)
    }

    /**
     * 设置色调 0 ~ 360
     */
    fun setHue(hue: Float) {
        this.hue = hue
        val hueAdjust = (this.hue % 360f) * Math.PI.toFloat() / 180.0f
        setFloat(hueAdjustHandle, hueAdjust)
    }
}
