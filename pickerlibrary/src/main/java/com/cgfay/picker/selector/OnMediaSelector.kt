package com.cgfay.picker.selector

import android.content.Context
import com.cgfay.picker.model.MediaData

/**
 * Callback when media is selected.
 */
fun interface OnMediaSelector {
    fun onMediaSelect(context: Context, mediaDataList: List<MediaData>)
}
