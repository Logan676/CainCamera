package com.cgfay.camera.presenter

/**
 * View interface for camera preview callbacks.
 */
interface CameraPreviewView {
    fun deleteProgressSegment()
    fun hideOnRecording()
    fun updateRecordProgress(duration: Float)
    fun addProgressSegment(progress: Float)
    fun resetAllLayout()
    fun showConcatProgressDialog()
    fun hideConcatProgressDialog()
    fun showToast(msg: String)
    fun showFps(fps: Float)
}
