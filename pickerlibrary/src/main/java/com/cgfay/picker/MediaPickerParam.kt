package com.cgfay.picker

import java.io.Serializable

/**
 * Parameters for media scanning.
 */
class MediaPickerParam : Serializable {
    var showCapture: Boolean = true
    var showImage: Boolean = true
    var showVideo: Boolean = true
    var spanCount: Int = 4
    var spaceSize: Int = 4
    var hasEdge: Boolean = true
    var autoDismiss: Boolean = false

    fun showImageOnly(): Boolean = showImage && !showVideo
    fun showVideoOnly(): Boolean = showVideo && !showImage
}
