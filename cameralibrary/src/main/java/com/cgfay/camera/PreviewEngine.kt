package com.cgfay.camera

import android.app.Activity
import com.cgfay.camera.model.AspectRatio

/**
 * Entry point for launching camera preview.
 */
class PreviewEngine private constructor(activity: Activity) {

    private val weakActivity = java.lang.ref.WeakReference(activity)

    companion object {
        /**
         * Create a [PreviewEngine] from an [Activity] context.
         */
        @JvmStatic
        fun from(activity: Activity): PreviewEngine = PreviewEngine(activity)
    }

    /**
     * Configure the preview with a given [AspectRatio].
     */
    fun setCameraRatio(ratio: AspectRatio): PreviewBuilder = PreviewBuilder(this, ratio)

    internal fun getActivity(): Activity? = weakActivity.get()
}
