package com.cgfay.filter.glfilter.stickers

import android.content.Context
import com.cgfay.filter.glfilter.base.GLImageGroupFilter
import com.cgfay.filter.glfilter.stickers.bean.DynamicSticker
import com.cgfay.filter.glfilter.stickers.bean.DynamicStickerFrameData
import com.cgfay.filter.glfilter.stickers.bean.DynamicStickerNormalData
import com.cgfay.filter.glfilter.stickers.bean.StaticStickerNormalData

/**
 * 动态贴纸滤镜
 */
class GLImageDynamicStickerFilter(context: Context, sticker: DynamicSticker?) : GLImageGroupFilter(context) {
    init {
        if (sticker == null || sticker.dataList == null) {
            return
        }
        for (data in sticker.dataList) {
            if (data is DynamicStickerNormalData) {
                mFilters.add(DynamicStickerNormalFilter(context, sticker))
                break
            }
        }
        for (data in sticker.dataList) {
            if (data is DynamicStickerFrameData) {
                mFilters.add(DynamicStickerFrameFilter(context, sticker))
                break
            }
        }
        for (data in sticker.dataList) {
            if (data is StaticStickerNormalData) {
                mFilters.add(StaticStickerNormalFilter(context, sticker))
                break
            }
        }
    }
}
