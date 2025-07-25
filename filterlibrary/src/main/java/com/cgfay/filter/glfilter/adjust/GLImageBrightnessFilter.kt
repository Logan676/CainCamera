package com.cgfay.filter.glfilter.adjust

import android.content.Context
import android.opengl.GLES30
import com.cgfay.filter.glfilter.base.GLImageFilter
import com.cgfay.filter.glfilter.utils.OpenGLUtils

/**
 * 光照亮度
 * 亮度是人眼的所感受到的光的明暗程度
 * 亮度是彩色光在量上的特征，如果没有色彩，则只有亮度的一维变量。
 */
class GLImageBrightnessFilter(context: Context) : GLImageFilter(
    context,
    VERTEX_SHADER,
    OpenGLUtils.getShaderFromAssets(context, "shader/adjust/fragment_brightness.glsl")
) {

    private var brightnessHandle = 0
    private var brightness = 0f

    override fun initProgramHandle() {
        super.initProgramHandle()
        brightnessHandle = GLES30.glGetUniformLocation(mProgramHandle, "brightness")
        setBrightness(0f)
    }

    /**
     * 设置亮度 -1.0 ~ 1.0, 默认为0
     */
    fun setBrightness(brightness: Float) {
        var value = brightness
        if (value < -1.0f) {
            value = -1.0f
        } else if (value > 1.0f) {
            value = 1.0f
        }
        this.brightness = value
        setFloat(brightnessHandle, this.brightness)
    }
}
