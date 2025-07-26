package com.cgfay.video.bean

import android.graphics.Bitmap
import androidx.compose.runtime.Stable

/**
 * Node for storing a single video frame in a doubly linked list.
 */
@Stable
class VideoFrameNode(
    var bitmap: Bitmap? = null,
    var frameTime: Long = 0L
) {
    var prev: VideoFrameNode? = null
    var next: VideoFrameNode? = null
}
