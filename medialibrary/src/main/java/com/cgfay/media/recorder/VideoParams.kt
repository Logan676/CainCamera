package com.cgfay.media.recorder

import android.opengl.EGLContext

/**
 * Video recording parameters rewritten in Kotlin.
 */
class VideoParams {

    var videoWidth: Int = 0
    var videoHeight: Int = 0
    var bitRate: Int = BIT_RATE
    var videoPath: String = ""
    var speedMode: SpeedMode = SpeedMode.MODE_NORMAL
    var maxDuration: Long = 0L
    var eglContext: EGLContext? = null

    override fun toString(): String {
        return "VideoParams: ${videoWidth}x$videoHeight@$bitRate to $videoPath"
    }

    fun setVideoPath(path: String): VideoParams {
        videoPath = path
        return this
    }

    fun setVideoSize(width: Int, height: Int): VideoParams {
        videoWidth = width
        videoHeight = height
        if (videoWidth * videoHeight < 1280 * 720) {
            bitRate = BIT_RATE_LOW
        }
        return this
    }

    fun setVideoWidth(width: Int): VideoParams {
        videoWidth = width
        return this
    }

    fun setVideoHeight(height: Int): VideoParams {
        videoHeight = height
        return this
    }

    fun setBitRate(rate: Int): VideoParams {
        bitRate = rate
        return this
    }

    fun setSpeedMode(mode: SpeedMode): VideoParams {
        speedMode = mode
        return this
    }

    fun setMaxDuration(duration: Long): VideoParams {
        maxDuration = duration
        return this
    }

    fun setEglContext(context: EGLContext?): VideoParams {
        eglContext = context
        return this
    }

    companion object {
        const val MIME_TYPE = "video/avc"
        const val FRAME_RATE = 25
        const val I_FRAME_INTERVAL = 1
        const val BIT_RATE = 6693560
        const val BIT_RATE_LOW = 3921332
    }
}
