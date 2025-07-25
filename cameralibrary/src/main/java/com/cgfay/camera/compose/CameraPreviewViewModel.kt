package com.cgfay.camera.compose

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class CameraPreviewViewModel : ViewModel() {

    var showResourcePanel by mutableStateOf(false)
        private set

    fun toggleResourcePanel() {
        showResourcePanel = !showResourcePanel
    }

    fun hideResourcePanel() {
        showResourcePanel = false
    }

    fun onBackPressed(): Boolean {
        return if (showResourcePanel) {
            hideResourcePanel()
            true
        } else {
            false
        }
    }

    fun cancelRecordIfNeeded() {}
}

