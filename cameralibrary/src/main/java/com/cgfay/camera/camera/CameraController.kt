package com.cgfay.camera.camera

import android.app.Activity
import android.graphics.Rect
import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.Surface
import androidx.annotation.NonNull

/**
 * Kotlin version of CameraController.
 */
class CameraController(private val activity: Activity) : ICameraController, Camera.PreviewCallback {

    companion object {
        private const val TAG = "CameraController"
        private const val DEFAULT_16_9_WIDTH = 1280
        private const val DEFAULT_16_9_HEIGHT = 720
    }

    private var expectFps = CameraParam.DESIRED_PREVIEW_FPS
    private var previewWidth = DEFAULT_16_9_WIDTH
    private var previewHeight = DEFAULT_16_9_HEIGHT
    private var orientation = 0
    private var camera: Camera? = null
    private var cameraId: Int
    private var surfaceTextureListener: OnSurfaceTextureListener? = null
    private var previewCallback: PreviewCallback? = null
    private var frameAvailableListener: OnFrameAvailableListener? = null
    private var outputTexture: SurfaceTexture? = null
    private var outputThread: HandlerThread? = null

    init {
        Log.d(TAG, "CameraController: created！")
        cameraId = if (CameraApi.hasFrontCamera(activity)) {
            Camera.CameraInfo.CAMERA_FACING_FRONT
        } else {
            Camera.CameraInfo.CAMERA_FACING_BACK
        }
    }

    override fun openCamera() {
        closeCamera()
        if (camera != null) {
            throw RuntimeException("camera already initialized!")
        }
        camera = Camera.open(cameraId)
        if (camera == null) {
            throw RuntimeException("Unable to open camera")
        }
        val cameraParam = CameraParam.getInstance()
        cameraParam.cameraId = cameraId
        val parameters = camera!!.parameters
        cameraParam.supportFlash = checkSupportFlashLight(parameters)
        cameraParam.previewFps = chooseFixedPreviewFps(parameters, expectFps * 1000)
        parameters.setRecordingHint(true)
        if (cameraId == Camera.CameraInfo.CAMERA_FACING_BACK && supportAutoFocusFeature(parameters)) {
            camera!!.cancelAutoFocus()
            parameters.focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO
        }
        camera!!.parameters = parameters
        setPreviewSize(camera!!, previewWidth, previewHeight)
        setPictureSize(camera!!, previewWidth, previewHeight)
        orientation = calculateCameraPreviewOrientation(activity)
        camera!!.setDisplayOrientation(orientation)
        releaseSurfaceTexture()
        outputTexture = createDetachedSurfaceTexture()
        try {
            camera!!.setPreviewTexture(outputTexture)
            camera!!.setPreviewCallback(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        camera!!.startPreview()
        surfaceTextureListener?.onSurfaceTexturePrepared(outputTexture)
    }

    private fun createDetachedSurfaceTexture(): SurfaceTexture {
        val surfaceTexture = SurfaceTexture(0)
        surfaceTexture.detachFromGLContext()
        if (Build.VERSION.SDK_INT >= 21) {
            outputThread?.quit()
            outputThread = HandlerThread("FrameAvailableThread").apply { start() }
            surfaceTexture.setOnFrameAvailableListener({ texture ->
                frameAvailableListener?.onFrameAvailable(texture)
            }, Handler(outputThread!!.looper))
        } else {
            surfaceTexture.setOnFrameAvailableListener { texture ->
                frameAvailableListener?.onFrameAvailable(texture)
            }
        }
        return surfaceTexture
    }

    private fun releaseSurfaceTexture() {
        outputTexture?.release()
        outputTexture = null
        outputThread?.quitSafely()
        outputThread = null
    }

    override fun closeCamera() {
        camera?.let {
            it.setPreviewCallback(null)
            it.setPreviewCallbackWithBuffer(null)
            it.addCallbackBuffer(null)
            it.stopPreview()
            it.release()
        }
        camera = null
        releaseSurfaceTexture()
    }

    override fun setOnSurfaceTextureListener(listener: OnSurfaceTextureListener?) {
        surfaceTextureListener = listener
    }

    override fun setPreviewCallback(callback: PreviewCallback?) {
        previewCallback = callback
    }

    override fun setOnFrameAvailableListener(listener: OnFrameAvailableListener?) {
        frameAvailableListener = listener
    }

    override fun switchCamera() {
        var front = !isFront
        front = front && CameraApi.hasFrontCamera(activity)
        if (front != isFront) {
            setFront(front)
            openCamera()
        }
    }

    override fun onPreviewFrame(data: ByteArray?, camera: Camera?) {
        if (data != null) {
            previewCallback?.onPreviewFrame(data)
        }
    }

    override fun setFront(front: Boolean) {
        cameraId = if (front) {
            Camera.CameraInfo.CAMERA_FACING_FRONT
        } else {
            Camera.CameraInfo.CAMERA_FACING_BACK
        }
    }

    override val isFront: Boolean
        get() = cameraId == Camera.CameraInfo.CAMERA_FACING_FRONT

    override fun getOrientation(): Int = orientation
    override fun getPreviewWidth(): Int = previewWidth
    override fun getPreviewHeight(): Int = previewHeight

    override fun canAutoFocus(): Boolean {
        val focusModes = camera?.parameters?.supportedFocusModes
        return focusModes != null && focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)
    }

    override fun setFocusArea(rect: Rect) {
        camera?.let { cam ->
            val parameters = cam.parameters
            if (supportAutoFocusFeature(parameters)) {
                parameters.focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO
            }
            if (parameters.maxNumFocusAreas > 0) {
                val focusAreas = ArrayList<Camera.Area>()
                focusAreas.add(Camera.Area(rect, CameraParam.Weight))
                if (parameters.maxNumFocusAreas > 0) {
                    parameters.focusAreas = focusAreas
                }
                if (parameters.maxNumMeteringAreas > 0) {
                    parameters.meteringAreas = focusAreas
                }
                cam.parameters = parameters
                cam.autoFocus { success, camera ->
                    val params = camera.parameters
                    if (supportAutoFocusFeature(params)) {
                        camera.cancelAutoFocus()
                        params.focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO
                    }
                    camera.parameters = params
                    camera.autoFocus(null)
                }
            }
        }
    }

    override fun getFocusArea(x: Float, y: Float, width: Int, height: Int, focusSize: Int): Rect {
        return calculateTapArea(x, y, width, height, focusSize, 1.0f)
    }

    override fun supportTorch(front: Boolean): Boolean {
        if (front) return true
        return !checkSupportFlashLight(camera)
    }

    override fun setFlashLight(on: Boolean) {
        if (supportTorch(isFront)) return
        camera?.let {
            val parameters = it.parameters
            parameters.flashMode = if (on) Camera.Parameters.FLASH_MODE_TORCH else Camera.Parameters.FLASH_MODE_OFF
            it.parameters = parameters
        }
    }

    private fun canZoom(): Boolean {
        return camera != null && camera!!.parameters.isZoomSupported
    }

    override fun zoomIn() {
        if (canZoom()) {
            val parameters = camera!!.parameters
            val current = parameters.zoom
            val maxZoom = parameters.maxZoom
            parameters.zoom = (current + 1).coerceAtMost(maxZoom)
            camera!!.parameters = parameters
        }
    }

    override fun zoomOut() {
        if (canZoom()) {
            val parameters = camera!!.parameters
            val current = parameters.zoom
            parameters.zoom = (current - 1).coerceAtLeast(0)
            camera!!.parameters = parameters
        }
    }

    private fun setPreviewSize(camera: Camera, expectWidth: Int, expectHeight: Int) {
        val parameters = camera.parameters
        val size = calculatePerfectSize(parameters.supportedPreviewSizes, expectWidth, expectHeight, CalculateType.Lower)
        parameters.setPreviewSize(size.width, size.height)
        previewWidth = size.width
        previewHeight = size.height
        camera.parameters = parameters
    }

    private fun setPictureSize(camera: Camera, expectWidth: Int, expectHeight: Int) {
        val parameters = camera.parameters
        val size = calculatePerfectSize(parameters.supportedPictureSizes, expectWidth, expectHeight, CalculateType.Max)
        parameters.setPictureSize(size.width, size.height)
        camera.parameters = parameters
    }

    private fun calculateCameraPreviewOrientation(activity: Activity): Int {
        val info = Camera.CameraInfo()
        Camera.getCameraInfo(CameraParam.getInstance().cameraId, info)
        val rotation = activity.windowManager.defaultDisplay.rotation
        val degrees = when (rotation) {
            Surface.ROTATION_0 -> 0
            Surface.ROTATION_90 -> 90
            Surface.ROTATION_180 -> 180
            Surface.ROTATION_270 -> 270
            else -> 0
        }
        return if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            (360 - (info.orientation + degrees) % 360) % 360
        } else {
            (info.orientation - degrees + 360) % 360
        }
    }

    private fun calculateTapArea(x: Float, y: Float, width: Int, height: Int, focusSize: Int, coefficient: Float): Rect {
        val areaSize = (focusSize * coefficient).toInt()
        val left = clamp(((y / height) * 2000 - 1000).toInt(), areaSize)
        val top = clamp((((height - x) / width) * 2000 - 1000).toInt(), areaSize)
        return Rect(left, top, left + areaSize, top + areaSize)
    }

    private fun clamp(touchCoordinateInCameraReper: Int, focusAreaSize: Int): Int {
        return if (Math.abs(touchCoordinateInCameraReper) + focusAreaSize > 1000) {
            if (touchCoordinateInCameraReper > 0) {
                1000 - focusAreaSize
            } else {
                -1000 + focusAreaSize
            }
        } else {
            touchCoordinateInCameraReper - focusAreaSize / 2
        }
    }

    private fun supportAutoFocusFeature(parameters: Camera.Parameters): Boolean {
        val focusModes = parameters.supportedFocusModes
        return focusModes != null && focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)
    }

    private fun checkSupportFlashLight(camera: Camera?): Boolean {
        if (camera == null) return false
        return checkSupportFlashLight(camera.parameters)
    }

    private fun checkSupportFlashLight(parameters: Camera.Parameters): Boolean {
        if (parameters.flashMode == null) return false
        val supportedFlashModes = parameters.supportedFlashModes
        return !(supportedFlashModes == null || supportedFlashModes.isEmpty() ||
                supportedFlashModes.size == 1 && supportedFlashModes[0] == Camera.Parameters.FLASH_MODE_OFF)
    }

    private fun chooseFixedPreviewFps(parameters: Camera.Parameters, expectedThoudandFps: Int): Int {
        val supportedFps = parameters.supportedPreviewFpsRange
        for (entry in supportedFps) {
            if (entry[0] == entry[1] && entry[0] == expectedThoudandFps) {
                parameters.setPreviewFpsRange(entry[0], entry[1])
                return entry[0]
            }
        }
        val temp = IntArray(2)
        val guess: Int
        parameters.getPreviewFpsRange(temp)
        guess = if (temp[0] == temp[1]) {
            temp[0]
        } else {
            temp[1] / 2
        }
        return guess
    }

    private fun calculatePerfectSize(
        sizes: List<Camera.Size>,
        expectWidth: Int,
        expectHeight: Int,
        calculateType: CalculateType
    ): Camera.Size {
        val sorted = sizes.toMutableList()
        sortList(sorted)
        val bigEnough = ArrayList<Camera.Size>()
        val noBigEnough = ArrayList<Camera.Size>()
        for (size in sorted) {
            if (size.height * expectWidth / expectHeight == size.width) {
                if (size.width > expectWidth && size.height > expectHeight) {
                    bigEnough.add(size)
                } else {
                    noBigEnough.add(size)
                }
            }
        }
        var perfectSize: Camera.Size? = null
        when (calculateType) {
            CalculateType.Min -> {
                perfectSize = if (noBigEnough.size > 1) Collections.min(noBigEnough, CompareAreaSize())
                else if (noBigEnough.size == 1) noBigEnough[0] else null
            }
            CalculateType.Max -> {
                perfectSize = if (bigEnough.size > 1) Collections.max(bigEnough, CompareAreaSize())
                else if (bigEnough.size == 1) bigEnough[0] else null
            }
            CalculateType.Lower -> {
                if (noBigEnough.size > 0) {
                    val size = Collections.max(noBigEnough, CompareAreaSize())
                    if (size.width.toFloat() / expectWidth >= 0.8f && size.height.toFloat() / expectHeight > 0.8f) {
                        perfectSize = size
                    }
                } else if (bigEnough.size > 0) {
                    val size = Collections.min(bigEnough, CompareAreaSize())
                    if (expectWidth.toFloat() / size.width >= 0.8f && expectHeight.toFloat() / size.height >= 0.8f) {
                        perfectSize = size
                    }
                }
            }
            CalculateType.Larger -> {
                if (bigEnough.size > 0) {
                    val size = Collections.min(bigEnough, CompareAreaSize())
                    if (expectWidth.toFloat() / size.width >= 0.8f && expectHeight.toFloat() / size.height >= 0.8f) {
                        perfectSize = size
                    }
                } else if (noBigEnough.size > 0) {
                    val size = Collections.max(noBigEnough, CompareAreaSize())
                    if (size.width.toFloat() / expectWidth >= 0.8f && size.height.toFloat() / expectHeight > 0.8f) {
                        perfectSize = size
                    }
                }
            }
        }
        if (perfectSize == null) {
            var result = sorted[0]
            var widthOrHeight = false
            for (size in sorted) {
                if (size.width == expectWidth && size.height == expectHeight &&
                    size.height.toFloat() / size.width.toFloat() == CameraParam.getInstance().currentRatio
                ) {
                    result = size
                    break
                }
                if (size.width == expectWidth) {
                    widthOrHeight = true
                    if (Math.abs(result.height - expectHeight) > Math.abs(size.height - expectHeight) &&
                        size.height.toFloat() / size.width.toFloat() == CameraParam.getInstance().currentRatio
                    ) {
                        result = size
                        break
                    }
                } else if (size.height == expectHeight) {
                    widthOrHeight = true
                    if (Math.abs(result.width - expectWidth) > Math.abs(size.width - expectWidth) &&
                        size.height.toFloat() / size.width.toFloat() == CameraParam.getInstance().currentRatio
                    ) {
                        result = size
                        break
                    }
                } else if (!widthOrHeight) {
                    if (Math.abs(result.width - expectWidth) > Math.abs(size.width - expectWidth) &&
                        Math.abs(result.height - expectHeight) > Math.abs(size.height - expectHeight) &&
                        size.height.toFloat() / size.width.toFloat() == CameraParam.getInstance().currentRatio
                    ) {
                        result = size
                    }
                }
            }
            perfectSize = result
        }
        return perfectSize
    }

    private fun sortList(list: MutableList<Camera.Size>) {
        Collections.sort(list, CompareAreaSize())
    }

    private class CompareAreaSize : Comparator<Camera.Size> {
        override fun compare(pre: Camera.Size, after: Camera.Size): Int {
            return java.lang.Long.signum(pre.width.toLong() * pre.height - after.width.toLong() * after.height)
        }
    }
}
