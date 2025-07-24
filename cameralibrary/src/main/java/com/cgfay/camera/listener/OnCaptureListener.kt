package com.cgfay.camera.listener

import android.graphics.Bitmap

/**
 * Listener for capture callbacks.
 */
fun interface OnCaptureListener {
    /**
     * Called when a frame is captured.
     */
    fun onCapture(bitmap: Bitmap)
}
