package com.cgfay.filter.glfilter.stickers.bean

/**
 * 某个部位的动态贴纸数据
 */
class DynamicStickerData {
    var width = 0           // 贴纸宽度
    var height = 0          // 贴纸高度
    var frames = 0          // 贴纸帧数
    var action = 0          // 动作，0表示默认显示，这里用来处理贴纸音乐、动作等
    var stickerName: String? = null // 贴纸名称，用于标记贴纸所在文件夹以及png文件的
    var duration = 0        // 贴纸帧显示间隔
    var stickerLooping = false  // 贴纸是否循环渲染
    var audioPath: String = ""    // 音乐路径，不存在时，路径为空字符串
    var audioLooping = false     // 音乐是否循环播放
    var maxCount = 0             // 最大贴纸渲染次数

    override fun toString(): String {
        return "DynamicStickerData{" +
                "width=" + width +
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
