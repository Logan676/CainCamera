package com.cgfay.filter.glfilter.effect.bean

/**
 * Information describing one dynamic effect.
 * Effects are time-based filters.
 */
class DynamicEffectData {
    var name: String? = null
    var vertexShader: String? = null
    var fragmentShader: String? = null
    var uniformDataList: MutableList<UniformData> = ArrayList()
    var uniformSamplerList: MutableList<UniformSampler> = ArrayList()
    var texelSize: Boolean = false
    var duration: Int = 0

    /**
     * Uniform float values bound to a shader variable.
     */
    class UniformData(var uniform: String, var value: FloatArray)

    /**
     * Uniform sampler bound to a texture path.
     */
    class UniformSampler(var uniform: String, var value: String)
}
