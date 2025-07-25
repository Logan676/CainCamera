package com.cgfay.filter.glfilter.effect.bean

/**
 * Dynamic effect container with associated data list.
 */
class DynamicEffect {
    /** Folder path containing the unzipped effect resources. */
    var unzipPath: String? = null

    /** List of effect data objects. */
    val effectList: MutableList<DynamicEffectData> = ArrayList()

    override fun toString(): String {
        val builder = StringBuilder()
        builder.append("unzipPath: ").append(unzipPath).append('\n')
        builder.append("data: [")
        for (i in effectList.indices) {
            builder.append(effectList[i].toString())
            if (i < effectList.size - 1) {
                builder.append(',')
            }
        }
        builder.append(']')
        return builder.toString()
    }
}
