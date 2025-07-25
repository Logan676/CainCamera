package com.cgfay.filter.glfilter.base

import android.content.Context
import com.cgfay.filter.glfilter.utils.OpenGLUtils

/**
 * Load an image and flip vertically.
 */
open class GLImageInputFilter : GLImageFilter {
    constructor(context: Context) : this(
        context,
        VERTEX_SHADER,
        OpenGLUtils.getShaderFromAssets(context, "shader/base/fragment_image_input.glsl")
    )

    constructor(context: Context, vertexShader: String, fragmentShader: String) :
        super(context, vertexShader, fragmentShader)
}
