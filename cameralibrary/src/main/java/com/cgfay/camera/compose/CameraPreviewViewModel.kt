package com.cgfay.camera.compose

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

import com.cgfay.camera.presenter.CameraPreviewView

/**
 * ViewModel used by [CameraActivity] and related composables. It also acts as
 * the [CameraPreviewView] implementation so the presenter can dispatch
 * callbacks without requiring a Fragment instance.
 */
class CameraPreviewViewModel : ViewModel(), CameraPreviewView {

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

