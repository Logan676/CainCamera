package com.cgfay.camera.camera

import android.graphics.Rect
import android.graphics.SurfaceTexture

/**
 * Camera controller interface
 */
interface ICameraController {
    /** Open the camera */
    fun openCamera()

    /** Close the camera */
    fun closeCamera()

    /** Set surface texture listener */
    fun setOnSurfaceTextureListener(listener: OnSurfaceTextureListener?)

    /** Set preview callback */
    fun setPreviewCallback(callback: PreviewCallback?)

    /** Set frame available listener */
    fun setOnFrameAvailableListener(listener: OnFrameAvailableListener?)

    /** Switch camera */
    fun switchCamera()

    /** Set front camera flag */
    fun setFront(front: Boolean)

    /** Is front camera */
    fun isFront(): Boolean

    /** Orientation of preview surface */
    fun getOrientation(): Int

    /** Preview width */
    fun getPreviewWidth(): Int

    /** Preview height */
    fun getPreviewHeight(): Int

    /** Whether auto focus is supported */
    fun canAutoFocus(): Boolean

    /** Set focus area */
    fun setFocusArea(rect: Rect?)

    /** Get focus area */
    fun getFocusArea(x: Float, y: Float, width: Int, height: Int, focusSize: Int): Rect?

    /** Whether torch is supported */
    fun supportTorch(front: Boolean): Boolean

    /** Enable or disable flash light */
    fun setFlashLight(on: Boolean)

    /** Zoom in */
    fun zoomIn()

    /** Zoom out */
    fun zoomOut()
}
