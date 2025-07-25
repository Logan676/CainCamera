package com.cgfay.filter.glfilter.stickers.bean

/**
 * 动态贴纸
 */
class DynamicSticker {
    var unzipPath: String? = null
    var dataList: MutableList<DynamicStickerData> = ArrayList()

    override fun toString(): String {
        return "DynamicSticker{" +
                "unzipPath='" + unzipPath + '\'' +
                ", dataList=" + dataList +
                '}'
    }
}
