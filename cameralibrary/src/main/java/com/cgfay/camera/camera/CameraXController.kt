package com.cgfay.camera.camera

import android.annotation.SuppressLint
import android.graphics.Rect
import android.graphics.SurfaceTexture
import android.util.Log
import android.util.Size
import android.view.Surface
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.ZoomState
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.google.common.util.concurrent.ListenableFuture
import java.util.Objects
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executor
import java.util.concurrent.Executors

/**
 * CameraX wrapper implementation
 */
class CameraXController(private val lifecycleOwner: FragmentActivity) : ICameraController {

    companion object {
        private const val TAG = "CameraXController"
        private const val DEFAULT_16_9_WIDTH = 720
        private const val DEFAULT_16_9_HEIGHT = 1280
    }

    private var previewWidth = DEFAULT_16_9_WIDTH
    private var previewHeight = DEFAULT_16_9_HEIGHT
    private var rotation = 90

    private var facingFront = true

    private var cameraProvider: ProcessCameraProvider? = null
    private var camera: Camera? = null
    private var preview: Preview? = null

    private val executor: Executor = Executors.newSingleThreadExecutor()
    private var previewAnalyzer: ImageAnalysis? = null
    private var previewCallback: PreviewCallback? = null
    private var surfaceTextureListener: OnSurfaceTextureListener? = null
    private var frameAvailableListener: OnFrameAvailableListener? = null
    private var outputTexture: SurfaceTexture? = null

    init {
        Log.d(TAG, "CameraXController: created!")
    }

    @SuppressLint("RestrictedApi")
    override fun openCamera() {
        val cameraProviderFuture: ListenableFuture<ProcessCameraProvider> =
            ProcessCameraProvider.getInstance(lifecycleOwner)
        cameraProviderFuture.addListener({
            try {
                cameraProvider = cameraProviderFuture.get()
                bindCameraUseCases()
            } catch (e: ExecutionException) {
                e.printStackTrace()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(lifecycleOwner))
    }

    private fun bindCameraUseCases() {
        val provider = cameraProvider ?: return
        provider.unbindAll()

        preview = Preview.Builder()
            .setTargetResolution(Size(previewWidth, previewHeight))
            .build()

        preview?.setSurfaceProvider { surfaceRequest ->
            val surfaceTexture = createDetachedSurfaceTexture(surfaceRequest.resolution)
            val surface = Surface(surfaceTexture)
            surfaceRequest.provideSurface(surface, executor) {
                surface.release()
            }
            surfaceTextureListener?.onSurfaceTexturePrepared(outputTexture!!)
        }

        previewAnalyzer = ImageAnalysis.Builder()
            .setTargetResolution(Size(previewWidth, previewHeight))
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
        previewAnalyzer?.setAnalyzer(executor, PreviewCallbackAnalyzer(previewCallback))

        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(if (facingFront) CameraSelector.LENS_FACING_FRONT else CameraSelector.LENS_FACING_BACK)
            .build()

        camera = provider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, previewAnalyzer)
    }

    private fun createDetachedSurfaceTexture(size: Size): SurfaceTexture {
        if (outputTexture == null) {
            outputTexture = SurfaceTexture(0)
            outputTexture!!.setDefaultBufferSize(size.width, size.height)
            outputTexture!!.detachFromGLContext()
            outputTexture!!.setOnFrameAvailableListener { texture ->
                frameAvailableListener?.onFrameAvailable(texture)
            }
        }
        return outputTexture!!
    }

    private fun releaseSurfaceTexture() {
        outputTexture?.release()
        outputTexture = null
    }

    @SuppressLint("RestrictedApi")
    override fun closeCamera() {
        try {
            cameraProvider?.unbindAll()
            cameraProvider = null
            releaseSurfaceTexture()
        } catch (e: Exception) {
            e.printStackTrace()
        }
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

    @SuppressLint("RestrictedApi")
    override fun switchCamera() {
        val front = isFront()
        setFront(!front)
        cameraProvider?.unbindAll()

        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(if (facingFront) CameraSelector.LENS_FACING_FRONT else CameraSelector.LENS_FACING_BACK)
            .build()

        camera = cameraProvider?.bindToLifecycle(lifecycleOwner, cameraSelector, preview, previewAnalyzer)
    }

    override fun setFront(front: Boolean) {
        facingFront = front
    }

    override fun isFront(): Boolean = facingFront

    override fun getOrientation(): Int = rotation

    override fun getPreviewWidth(): Int {
        return if (rotation == 90 || rotation == 270) previewHeight else previewWidth
    }

    override fun getPreviewHeight(): Int {
        return if (rotation == 90 || rotation == 270) previewWidth else previewHeight
    }

    override fun canAutoFocus(): Boolean = false

    override fun setFocusArea(rect: Rect?) {}

    override fun getFocusArea(x: Float, y: Float, width: Int, height: Int, focusSize: Int): Rect? = null

    override fun supportTorch(front: Boolean): Boolean {
        return if (camera != null) {
            !camera!!.cameraInfo.hasFlashUnit()
        } else {
            true
        }
    }

    override fun setFlashLight(on: Boolean) {
        if (supportTorch(isFront())) {
            Log.e(TAG, "Failed to set flash light: $on")
            return
        }
        camera?.cameraControl?.enableTorch(on)
    }

    override fun zoomIn() {
        camera?.let { cam ->
            val zoomState = Objects.requireNonNull(cam.cameraInfo.zoomState.value)
            val currentZoomRatio = minOf(zoomState.maxZoomRatio, zoomState.zoomRatio + 0.1f)
            cam.cameraControl.setZoomRatio(currentZoomRatio)
        }
    }

    override fun zoomOut() {
        camera?.let { cam ->
            val zoomState = Objects.requireNonNull(cam.cameraInfo.zoomState.value)
            val currentZoomRatio = maxOf(zoomState.minZoomRatio, zoomState.zoomRatio - 0.1f)
            cam.cameraControl.setZoomRatio(currentZoomRatio)
        }
    }
}
