package com.cgfay.camera.model

/**
 * Created by cain.huang on 2017/9/28.
 * Available gallery types when capturing media.
 */
enum class GalleryType {
    /** Still image capture. */
    PICTURE,
    /** Record up to 60 seconds of video. */
    VIDEO_60S,
    /** Record up to 15 seconds of video. */
    VIDEO_15S
}
