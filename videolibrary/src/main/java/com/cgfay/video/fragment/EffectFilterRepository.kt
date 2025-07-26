package com.cgfay.video.fragment

import com.cgfay.video.bean.EffectMimeType
import com.cgfay.video.bean.EffectType

/**
 * Provides predefined video effect configurations.
 */
object EffectFilterRepository {
    private val effectFilterList = listOf(
        EffectType(EffectMimeType.FILTER, "灵魂出窍", 0x000, "assets://thumbs/effect/icon_effect_soul_stuff.png"),
        EffectType(EffectMimeType.FILTER, "抖动", 0x001, "assets://thumbs/effect/icon_effect_shake.png"),
        EffectType(EffectMimeType.FILTER, "幻觉", 0x002, "assets://thumbs/effect/icon_effect_illusion.png"),
        EffectType(EffectMimeType.FILTER, "缩放", 0x003, "assets://thumbs/effect/icon_effect_scale.png"),
        EffectType(EffectMimeType.FILTER, "闪白", 0x004, "assets://thumbs/effect/icon_effect_glitter_white.png"),
    )

    private val effectTransitionList = emptyList<EffectType>()

    private val effectMultiList = listOf(
        EffectType(EffectMimeType.MULTIFRAME, "模糊分屏", 0x200, "assets://thumbs/effect/icon_frame_blur.png"),
        EffectType(EffectMimeType.MULTIFRAME, "黑白三屏", 0x201, "assets://thumbs/effect/icon_frame_bw_three.png"),
        EffectType(EffectMimeType.MULTIFRAME, "两屏", 0x202, "assets://thumbs/effect/icon_frame_two.png"),
        EffectType(EffectMimeType.MULTIFRAME, "三屏", 0x203, "assets://thumbs/effect/icon_frame_three.png"),
        EffectType(EffectMimeType.MULTIFRAME, "四屏", 0x204, "assets://thumbs/effect/icon_frame_four.png"),
        EffectType(EffectMimeType.MULTIFRAME, "六屏", 0x205, "assets://thumbs/effect/icon_frame_six.png"),
        EffectType(EffectMimeType.MULTIFRAME, "九屏", 0x206, "assets://thumbs/effect/icon_frame_nine.png"),
    )

    fun getEffectFilterData(): List<EffectType> = effectFilterList

    fun getEffectTransitionData(): List<EffectType> = effectTransitionList

    fun getEffectMultiData(): List<EffectType> = effectMultiList
}

