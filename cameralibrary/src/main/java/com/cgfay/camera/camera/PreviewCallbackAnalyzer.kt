package com.cgfay.camera.camera

import android.annotation.SuppressLint
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy

/**
 * 预览帧分析器
 */
class PreviewCallbackAnalyzer(private val previewCallback: PreviewCallback?) : ImageAnalysis.Analyzer {

    companion object {
        private const val TAG = "PreviewCallbackAnalyzer"
        private const val VERBOSE = false
    }

    @SuppressLint("UnsafeExperimentalUsageError")
    override fun analyze(image: ImageProxy) {
        val start = System.currentTimeMillis()
        if (VERBOSE) {
            Log.d(TAG, "analyze: timestamp - ${image.imageInfo.timestamp}, orientation - ${image.imageInfo.rotationDegrees}, imageFormat - ${image.format}")
        }
        val planeImage = image.image
        if (previewCallback != null && planeImage != null) {
            val data = ImageConvert.getDataFromImage(planeImage, ImageConvert.COLOR_FORMAT_NV21)
            if (data != null) {
                previewCallback.onPreviewFrame(data)
            }
        }
        image.close()
        if (VERBOSE) {
            Log.d(TAG, "convert cost time - ${System.currentTimeMillis() - start}")
        }
    }
}
