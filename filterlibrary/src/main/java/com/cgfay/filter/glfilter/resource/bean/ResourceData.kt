package com.cgfay.filter.glfilter.resource.bean

/**
 * 资源数据
 */
data class ResourceData(
    var name: String,
    var zipPath: String,
    var type: ResourceType,
    var unzipFolder: String,
    var thumbPath: String
)
