package com.cgfay.filter.glfilter.stickers.bean

/**
 * 默认动态贴纸类型
 */
class StaticStickerNormalData : DynamicStickerData() {
    var alignMode = 0 // 对齐方式，0表示centerCrop, 1表示fitXY，2表示居中center

    override fun toString(): String {
        return "DynamicStickerFrameData{" +
                "alignMode=" + alignMode +
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
