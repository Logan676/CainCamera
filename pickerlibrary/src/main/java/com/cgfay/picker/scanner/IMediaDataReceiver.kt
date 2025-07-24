package com.cgfay.picker.scanner

import androidx.annotation.NonNull
import com.cgfay.picker.model.MediaData

fun interface IMediaDataReceiver {
    fun onMediaDataObserve(@NonNull mediaDataList: List<MediaData>)
}
