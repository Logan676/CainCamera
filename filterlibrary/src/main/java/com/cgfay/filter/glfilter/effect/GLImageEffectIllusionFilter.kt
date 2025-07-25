package com.cgfay.filter.glfilter.effect

import android.content.Context
import com.cgfay.filter.glfilter.utils.OpenGLUtils

/** Illusion effect similar to TikTok. */
class GLImageEffectIllusionFilter : GLImageEffectFilter {
    constructor(context: Context) : this(
        context,
        VERTEX_SHADER,
        OpenGLUtils.getShaderFromAssets(context, "shader/effect/fragment_effect_illusion.glsl")
    )

    constructor(context: Context, vertexShader: String, fragmentShader: String) :
        super(context, vertexShader, fragmentShader)
}
