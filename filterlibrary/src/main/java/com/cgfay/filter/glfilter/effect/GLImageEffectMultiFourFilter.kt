package com.cgfay.filter.glfilter.effect

import android.content.Context
import com.cgfay.filter.glfilter.utils.OpenGLUtils

/** Four panel split effect similar to TikTok. */
class GLImageEffectMultiFourFilter : GLImageEffectFilter {
    constructor(context: Context) : this(
        context,
        VERTEX_SHADER,
        OpenGLUtils.getShaderFromAssets(context, "shader/effect/fragment_effect_multi_four.glsl")
    )

    constructor(context: Context, vertexShader: String, fragmentShader: String) :
        super(context, vertexShader, fragmentShader)
}
