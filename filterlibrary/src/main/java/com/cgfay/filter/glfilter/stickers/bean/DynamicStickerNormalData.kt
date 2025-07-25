package com.cgfay.filter.glfilter.stickers.bean

import java.util.Arrays

/**
 * 默认动态贴纸类型
 */
class DynamicStickerNormalData : DynamicStickerData() {
    var centerIndexList: IntArray? = null // 中心坐标索引列表，有可能是多个关键点计算中心点
    var offsetX = 0f           // 相对于贴纸中心坐标的x轴偏移像素
    var offsetY = 0f           // 相对于贴纸中心坐标的y轴偏移像素
    var baseScale = 0f         // 贴纸基准缩放倍数
    var startIndex = 0         // 人脸起始索引，用于计算人脸的宽度
    var endIndex = 0           // 人脸结束索引，用于计算人脸的宽度

    override fun toString(): String {
        return "DynamicStickerNormalData{" +
                "centerIndexList=" + Arrays.toString(centerIndexList) +
                ", offsetX=" + offsetX +
                ", offsetY=" + offsetY +
                ", baseScale=" + baseScale +
                ", startIndex=" + startIndex +
                ", endIndex=" + endIndex +
                ", width=" + width +
                ", height=" + height +
                ", frames=" + frames +
                ", action=" + action +
                ", stickerName='" + stickerName + '\'' +
                ", duration=" + duration +
                ", stickerLooping=" + stickerLooping +
                ", audioPath='" + audioPath + '\'' +
                ", audioLooping=" + audioLooping +
                ", maxCount=" + maxCount +
                '}'
    }
}
