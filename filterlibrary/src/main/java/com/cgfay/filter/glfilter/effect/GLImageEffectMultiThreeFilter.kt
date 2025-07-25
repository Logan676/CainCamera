package com.cgfay.filter.glfilter.effect

import android.content.Context
import com.cgfay.filter.glfilter.utils.OpenGLUtils

/** Three panel effect similar to TikTok */
class GLImageEffectMultiThreeFilter : GLImageEffectFilter {
    constructor(context: Context) : this(
        context,
        VERTEX_SHADER,
        OpenGLUtils.getShaderFromAssets(context, "shader/effect/fragment_effect_multi_three.glsl")
    )

    constructor(context: Context, vertexShader: String, fragmentShader: String) :
        super(context, vertexShader, fragmentShader)
}
