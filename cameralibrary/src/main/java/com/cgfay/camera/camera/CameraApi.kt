package com.cgfay.camera.camera

import android.annotation.TargetApi
import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Build
import android.util.Log
import androidx.annotation.NonNull
import com.cgfay.uitls.utils.DisplayUtils
import com.cgfay.uitls.utils.SystemUtils

/**
 * Utilities for querying camera capabilities.
 */
object CameraApi {

    private const val TAG = "CameraApi"

    /**
     * Whether the device supports the Camera2 API.
     */
    @JvmStatic
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    fun hasCamera2(context: Context?): Boolean {
        if (context == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return false
        }
        return try {
            val manager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager?
                ?: return false
            val idList = manager.cameraIdList
            if (idList.isEmpty()) {
                false
            } else {
                var notNull = true
                for (str in idList) {
                    if (str == null || str.trim().isEmpty()) {
                        notNull = false
                        break
                    }
                    val characteristics = manager.getCameraCharacteristics(str)
                    val level =
                        characteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL)
                    if (level == CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY ||
                        level == CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED
                    ) {
                        notNull = false
                        break
                    }
                }
                notNull
            }
        } catch (ignore: Throwable) {
            false
        }
    }

    /**
     * Whether the device has a front facing camera.
     */
    @JvmStatic
    fun hasFrontCamera(@NonNull context: Context): Boolean {
        val brand = SystemUtils.getDeviceBrand()
        val model = SystemUtils.getSystemModel()
        // Special handling for Huawei foldable devices when unfolded
        if (brand.contains("HUAWEI") && model.contains("TAH-")) {
            var width = DisplayUtils.getDisplayWidth(context)
            var height = DisplayUtils.getDisplayHeight(context)
            if (width < 0 || height < 0) {
                return true
            }
            if (width < height) {
                val temp = width
                width = height
                height = temp
            }
            Log.d(TAG, "hasFrontCamera: $model, width = $width, height = $height")
            if (width.toFloat() / height <= 4.0f / 3.0f) {
                return false
            }
        }
        return true
    }
}
