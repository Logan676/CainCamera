package com.cgfay.camera.compose

import androidx.lifecycle.ViewModel

class CameraPreviewViewModel : ViewModel() {
    fun onBackPressed(): Boolean = false
    fun cancelRecordIfNeeded() {}
}

