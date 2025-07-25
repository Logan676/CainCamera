package com.cgfay.filter.glfilter.stickers

import android.content.Context
import com.cgfay.filter.glfilter.base.GLImageAudioFilter
import com.cgfay.filter.glfilter.stickers.bean.DynamicSticker

/**
 * 贴纸滤镜基类
 */
open class DynamicStickerBaseFilter(
    context: Context,
    sticker: DynamicSticker?,
    vertexShader: String,
    fragmentShader: String
) : GLImageAudioFilter(context, vertexShader, fragmentShader) {

    // 贴纸数据
    protected var mDynamicSticker: DynamicSticker? = sticker

    // 贴纸加载器列表
    protected val mStickerLoaderList: MutableList<DynamicStickerLoader> = mutableListOf()
}
