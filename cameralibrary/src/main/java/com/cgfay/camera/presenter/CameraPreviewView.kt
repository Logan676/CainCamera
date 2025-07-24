package com.cgfay.camera.presenter

interface CameraPreviewView {
    fun deleteProgressSegment() {}
    fun hideOnRecording() {}
    fun updateRecordProgress(progress: Float) {}
    fun addProgressSegment(progress: Float) {}
    fun resetAllLayout() {}
    fun showConcatProgressDialog() {}
    fun hideConcatProgressDialog() {}
    fun showToast(msg: String) {}
    fun showFps(fps: Float) {}
}
