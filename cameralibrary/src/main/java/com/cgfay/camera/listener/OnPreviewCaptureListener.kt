package com.cgfay.camera.listener

import androidx.annotation.IntDef
import androidx.annotation.RestrictTo

/**
 * \u5a92\u4f53\u62cd\u6444\u56de\u8c03
 */
interface OnPreviewCaptureListener {

    companion object {
        const val MediaTypePicture = 0
        const val MediaTypeVideo = 1
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    @IntDef(value = [MediaTypePicture, MediaTypeVideo])
    @Retention(AnnotationRetention.SOURCE)
    annotation class MediaType

    // \u5a92\u4f53\u9009\u62e9
    fun onMediaSelectedListener(path: String, @MediaType type: Int)
}
