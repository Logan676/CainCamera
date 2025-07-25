package com.cgfay.filter.glfilter.resource.bean

/**
 * 资源枚举类型
 */
enum class ResourceType(val typeName: String, val index: Int) {
    NONE("none", -1),
    STICKER("sticker", 0),
    FILTER("filter", 1),
    EFFECT("effect", 2),
    MAKEUP("makeup", 3),
    MULTI("multi", 4)
}
