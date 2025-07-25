package com.cgfay.filter.glfilter.makeup.bean

/**
 * Container for a set of makeup effects.
 */
class DynamicMakeup {
    var unzipPath: String? = null
    var makeupList: MutableList<MakeupBaseData> = ArrayList()
    override fun toString(): String {
        return "DynamicMakeup{" +
                "unzipPath='" + unzipPath + '\'' +
                ", makeupList=" + makeupList +
                '}'
    }
}
