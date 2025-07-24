package com.cgfay.picker

import android.content.Intent
import androidx.fragment.app.FragmentActivity
import com.cgfay.picker.loader.MediaLoader
import com.cgfay.picker.selector.OnMediaSelector

class MediaPickerBuilder(private val mediaPicker: MediaPicker) {
    private var mediaSelector: OnMediaSelector? = null
    private val pickerParam: MediaPickerParam = MediaPickerParam()

    init {
        MediaPickerManager.getInstance().reset()
    }

    fun showCapture(show: Boolean) = apply { pickerParam.showCapture = show }

    fun showVideo(show: Boolean) = apply { pickerParam.showVideo = show }

    fun showImage(show: Boolean) = apply { pickerParam.showImage = show }

    fun spanCount(spanCount: Int) = apply {
        require(spanCount >= 0) { "spanCount cannot be less than zero" }
        pickerParam.spanCount = spanCount
    }

    fun spaceSize(spaceSize: Int) = apply {
        require(spaceSize >= 0) { "spaceSize cannot be less than zero" }
        pickerParam.spaceSize = spaceSize
    }

    fun setItemHasEdge(hasEdge: Boolean) = apply { pickerParam.hasEdge = hasEdge }

    fun setAutoDismiss(autoDismiss: Boolean) = apply { pickerParam.autoDismiss = autoDismiss }

    // Keep Java API naming for compatibility
    fun ImageLoader(loader: MediaLoader) = apply {
        MediaPickerManager.getInstance().setMediaLoader(loader)
    }

    fun setMediaSelector(selector: OnMediaSelector) = apply {
        mediaSelector = selector
        MediaPickerManager.getInstance().mediaSelector = selector
    }

    fun show() {
        val activity: FragmentActivity = mediaPicker.activity ?: return
        val intent = Intent(activity, com.cgfay.picker.compose.PickerComposeActivity::class.java)
        intent.putExtra(MediaPicker.PICKER_PARAMS, pickerParam)
        activity.startActivity(intent)
    }
}
