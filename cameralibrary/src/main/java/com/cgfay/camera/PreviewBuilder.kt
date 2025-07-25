package com.cgfay.camera

import android.app.Activity
import android.content.Intent
import android.hardware.Camera
import com.cgfay.camera.activity.CameraActivity
import com.cgfay.camera.camera.CameraParam
import com.cgfay.camera.listener.OnPreviewCaptureListener
import com.cgfay.camera.model.AspectRatio
import com.cgfay.cameralibrary.R

/**
 * Kotlin replacement for [PreviewBuilder]. Used to configure and launch
 * the camera preview screen implemented with Compose.
 */
class PreviewBuilder internal constructor(
    private val previewEngine: PreviewEngine,
    ratio: AspectRatio
) {
    private val cameraParam: CameraParam = CameraParam.getInstance().apply {
        setAspectRatio(ratio)
    }

    fun showFacePoints(show: Boolean) = apply {
        cameraParam.drawFacePoints = show
    }

    fun showFps(show: Boolean) = apply {
        cameraParam.showFps = show
    }

    fun expectFps(fps: Int) = apply {
        cameraParam.expectFps = fps
    }

    fun expectWidth(width: Int) = apply {
        cameraParam.expectWidth = width
    }

    fun expectHeight(height: Int) = apply {
        cameraParam.expectHeight = height
    }

    fun highDefinition(highDefinition: Boolean) = apply {
        cameraParam.highDefinition = highDefinition
    }

    fun backCamera(backCamera: Boolean) = apply {
        cameraParam.backCamera = backCamera
        if (cameraParam.backCamera) {
            cameraParam.cameraId = Camera.CameraInfo.CAMERA_FACING_BACK
        }
    }

    fun focusWeight(weight: Int) = apply {
        cameraParam.setFocusWeight(weight)
    }

    fun recordable(recordable: Boolean) = apply {
        cameraParam.recordable = recordable
    }

    fun recordTime(recordTime: Int) = apply {
        cameraParam.recordTime = recordTime
    }

    fun recordAudio(recordAudio: Boolean) = apply {
        cameraParam.recordAudio = recordAudio
    }

    fun takeDelay(takeDelay: Boolean) = apply {
        cameraParam.takeDelay = takeDelay
    }

    fun luminousEnhancement(luminousEnhancement: Boolean) = apply {
        cameraParam.luminousEnhancement = luminousEnhancement
    }

    fun setPreviewCaptureListener(listener: OnPreviewCaptureListener?) = apply {
        cameraParam.captureListener = listener
    }

    /**
     * Launch the camera preview screen.
     */
    fun startPreview() {
        val activity: Activity = previewEngine.getActivity() ?: return
        val intent = Intent(activity, CameraActivity::class.java)
        activity.startActivity(intent)
        activity.overridePendingTransition(R.anim.anim_slide_up, 0)
    }
}
