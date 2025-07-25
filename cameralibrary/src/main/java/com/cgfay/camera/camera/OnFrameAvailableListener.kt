package com.cgfay.camera.camera

import android.graphics.SurfaceTexture

/**
 * Callback when a new frame is available on the SurfaceTexture
 */
fun interface OnFrameAvailableListener {
    fun onFrameAvailable(surfaceTexture: SurfaceTexture)
}
