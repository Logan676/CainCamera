package com.cgfay.camera.camera

import android.hardware.Camera
import com.cgfay.camera.listener.OnCaptureListener
import com.cgfay.camera.listener.OnFpsListener
import com.cgfay.camera.listener.OnPreviewCaptureListener
import com.cgfay.camera.model.AspectRatio
import com.cgfay.camera.model.GalleryType
import com.cgfay.filter.glfilter.beauty.bean.BeautyParam

/**
 * Camera configuration parameters converted to Kotlin.
 */
class CameraParam private constructor() {

    companion object {
        const val MAX_FOCUS_WEIGHT = 1000
        const val DEFAULT_RECORD_TIME = 15000
        const val DEFAULT_16_9_WIDTH = 1280
        const val DEFAULT_16_9_HEIGHT = 720
        const val DEFAULT_4_3_WIDTH = 1024
        const val DEFAULT_4_3_HEIGHT = 768
        const val DESIRED_PREVIEW_FPS = 30
        const val Ratio_4_3 = 0.75f
        const val Ratio_16_9 = 0.5625f
        const val Weight = 100

        private val INSTANCE = CameraParam()
        @JvmStatic
        fun getInstance(): CameraParam = INSTANCE
    }

    var drawFacePoints = false
    var showFps = false
    var aspectRatio = AspectRatio.Ratio_16_9
    var currentRatio = Ratio_16_9
    var expectFps = DESIRED_PREVIEW_FPS
    var previewFps = 0
    var expectWidth = DEFAULT_16_9_WIDTH
    var expectHeight = DEFAULT_16_9_HEIGHT
    var previewWidth = 0
    var previewHeight = 0
    var highDefinition = false
    var orientation = 0
    var backCamera = true
    var cameraId = Camera.CameraInfo.CAMERA_FACING_BACK
    var supportFlash = false
    var focusWeight = 1000
    var recordable = true
    var recordTime = DEFAULT_RECORD_TIME
    var recordAudio = true
    var touchTake = false
    var takeDelay = false
    var luminousEnhancement = false
    var brightness = -1
    var mGalleryType: GalleryType = GalleryType.VIDEO_15S
    var captureListener: OnPreviewCaptureListener? = null
    var captureCallback: OnCaptureListener? = null
    var fpsCallback: OnFpsListener? = null
    var showCompare = false
    var isTakePicture = false
    var enableDepthBlur = false
    var enableVignette = false
    var beauty: BeautyParam = BeautyParam()

    init {
        reset()
    }

    private fun reset() {
        drawFacePoints = false
        showFps = false
        aspectRatio = AspectRatio.Ratio_16_9
        currentRatio = Ratio_16_9
        expectFps = DESIRED_PREVIEW_FPS
        previewFps = 0
        expectWidth = DEFAULT_16_9_WIDTH
        expectHeight = DEFAULT_16_9_HEIGHT
        previewWidth = 0
        previewHeight = 0
        highDefinition = false
        orientation = 0
        backCamera = true
        cameraId = Camera.CameraInfo.CAMERA_FACING_BACK
        supportFlash = false
        focusWeight = MAX_FOCUS_WEIGHT
        recordable = true
        recordTime = DEFAULT_RECORD_TIME
        recordAudio = true
        touchTake = false
        takeDelay = false
        luminousEnhancement = false
        brightness = -1
        mGalleryType = GalleryType.VIDEO_15S
        captureListener = null
        captureCallback = null
        fpsCallback = null
        showCompare = false
        isTakePicture = false
        enableDepthBlur = false
        enableVignette = false
        beauty = BeautyParam()
    }

    fun setAspectRatio(ratio: AspectRatio) {
        aspectRatio = ratio
        if (ratio == AspectRatio.Ratio_16_9) {
            expectWidth = DEFAULT_16_9_WIDTH
            expectHeight = DEFAULT_16_9_HEIGHT
            currentRatio = Ratio_16_9
        } else {
            expectWidth = DEFAULT_4_3_WIDTH
            expectHeight = DEFAULT_4_3_HEIGHT
            currentRatio = Ratio_4_3
        }
    }

    fun setBackCamera(back: Boolean) {
        backCamera = back
        cameraId = if (back) {
            Camera.CameraInfo.CAMERA_FACING_BACK
        } else {
            Camera.CameraInfo.CAMERA_FACING_FRONT
        }
    }

    fun setFocusWeight(weight: Int) {
        require(!(weight < 0 || weight > MAX_FOCUS_WEIGHT)) {
            "focusWeight must be 0 ~ 1000"
        }
        focusWeight = weight
    }
}
