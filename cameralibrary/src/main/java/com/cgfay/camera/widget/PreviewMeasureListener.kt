package com.cgfay.camera.widget

import android.view.View
import android.view.ViewGroup
import com.cgfay.cameralibrary.R
import java.lang.ref.WeakReference

/**
 * Kotlin conversion of PreviewMeasureListener.
 */
class PreviewMeasureListener(view: CameraMeasureFrameLayout) :
    CameraMeasureFrameLayout.OnMeasureListener {

    private val weakLayout = WeakReference(view)

    override fun onMeasure(width: Int, height: Int) {
        weakLayout.get()?.let { layout ->
            calculatePreviewLayout(layout, width, height)
            layout.setOnMeasureListener(null)
        }
    }

    private fun calculatePreviewLayout(preview: View, widthPixel: Int, heightPixel: Int) {
        val tabHeight = preview.resources.getDimension(R.dimen.camera_tab_height)
        var w = widthPixel
        var h = heightPixel
        if (w.toFloat() / h > 9f / 16f) {
            w = (h * (9f / 16f)).toInt()
            preview.layoutParams = ViewGroup.LayoutParams(w, h)
        } else if (w.toFloat() / (h - tabHeight) < 9f / 16f) {
            preview.layoutParams = ViewGroup.LayoutParams(w, (h - tabHeight).toInt())
        }
        preview.requestLayout()
    }
}
