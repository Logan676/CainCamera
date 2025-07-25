package com.cgfay.filter.glfilter.beauty

import android.content.Context
import com.cgfay.filter.glfilter.base.GLImageGaussianBlurFilter
import com.cgfay.filter.glfilter.utils.OpenGLUtils

/**
 * Gaussian blur filter used for beauty processing.
 */
class GLImageBeautyBlurFilter(
    context: Context,
    vertexShader: String = OpenGLUtils.getShaderFromAssets(context, "shader/beauty/vertex_beauty_blur.glsl"),
    fragmentShader: String = OpenGLUtils.getShaderFromAssets(context, "shader/beauty/fragment_beauty_blur.glsl")
) : GLImageGaussianBlurFilter(context, vertexShader, fragmentShader)
