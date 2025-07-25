package com.cgfay.filter.glfilter.makeup.bean

/**
 * Base information of a makeup item.
 */
open class MakeupBaseData {
    var makeupType: MakeupType = MakeupType.NONE
    var name: String? = null
    var id: String? = null
    var strength: Float = 1.0f
}
