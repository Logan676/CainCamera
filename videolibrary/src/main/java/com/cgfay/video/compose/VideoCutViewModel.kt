package com.cgfay.video.compose

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class VideoCutViewModel : ViewModel() {
    private val _videoPath = MutableStateFlow("")
    val videoPath: StateFlow<String> get() = _videoPath

    private val _progress = MutableStateFlow(0)
    val progress: StateFlow<Int> get() = _progress

    fun setVideoPath(path: String) {
        _videoPath.value = path
    }

    fun startCut() {
        // TODO trigger video cut logic
        _progress.value = 50 // fake progress for sample
    }
}
