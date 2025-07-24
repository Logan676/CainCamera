package com.cgfay.video.compose

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class VideoEditViewModel : ViewModel() {
    private val _videoPath = MutableStateFlow("")
    val videoPath: StateFlow<String> get() = _videoPath

    private val _selectedEffect = MutableStateFlow<String?>(null)
    val selectedEffect: StateFlow<String?> get() = _selectedEffect

    fun setVideoPath(path: String) {
        _videoPath.value = path
    }

    fun selectEffect(effectName: String) {
        _selectedEffect.value = effectName
    }
}
