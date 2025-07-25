package com.cgfay.filter.glfilter.adjust

import android.content.Context
import android.opengl.GLES30
import com.cgfay.filter.glfilter.base.GLImageFilter
import com.cgfay.filter.glfilter.utils.OpenGLUtils

/**
 * 曝光
 */
class GLImageExposureFilter(context: Context) : GLImageFilter(
    context,
    VERTEX_SHADER,
    OpenGLUtils.getShaderFromAssets(context, "shader/adjust/fragment_exposure.glsl")
) {

    private var exposureHandle = 0
    private var exposure = 0f

    override fun initProgramHandle() {
        super.initProgramHandle()
        exposureHandle = GLES30.glGetUniformLocation(mProgramHandle, "exposure")
        setExposure(0f)
    }

    /**
     * 设置曝光度 -10.0 ~ 10.0, 默认为0
     */
    fun setExposure(exposure: Float) {
        var value = exposure
        if (value < -10.0f) {
            value = -10.0f
        } else if (value > 10.0f) {
            value = 10.0f
        }
        this.exposure = value
        setFloat(exposureHandle, this.exposure)
    }
}
