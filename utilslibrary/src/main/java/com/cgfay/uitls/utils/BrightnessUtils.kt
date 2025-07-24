package com.cgfay.uitls.utils

import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.Settings
import android.util.Log
import android.view.WindowManager

object BrightnessUtils {
    private const val TAG = "BrightnessUtils"
    const val MAX_BRIGHTNESS = 255

    fun getSystemBrightnessMode(context: Context): Int {
        var brightnessMode = Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
        try {
            brightnessMode = Settings.System.getInt(
                context.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS_MODE
            )
        } catch (e: Exception) {
            Log.e(TAG, "getSystemBrightnessMode: ", e)
        }
        return brightnessMode
    }

    fun setSystemBrightnessMode(context: Context, brightnessMode: Int) {
        try {
            Settings.System.putInt(
                context.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS_MODE,
                brightnessMode
            )
        } catch (e: Exception) {
            Log.e(TAG, "setSystemBrightnessMode: ", e)
        }
    }

    fun getSystemBrightness(context: Context): Int {
        var screenBrightness = MAX_BRIGHTNESS
        try {
            screenBrightness = Settings.System.getInt(
                context.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS
            )
        } catch (e: Exception) {
            Log.e(TAG, "getSystemBrightness: ", e)
        }
        return screenBrightness
    }

    fun setSystemBrightness(context: Context, brightness: Int) {
        try {
            val resolver: ContentResolver = context.contentResolver
            val uri: Uri = Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS)
            Settings.System.putInt(resolver, Settings.System.SCREEN_BRIGHTNESS, brightness)
            resolver.notifyChange(uri, null)
        } catch (e: Exception) {
            Log.e(TAG, "setSystemBrightness: ", e)
        }
    }

    fun setWindowBrightness(activity: Activity, brightness: Int) {
        val lp = activity.window.attributes
        lp.screenBrightness = if (brightness == -1) {
            WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
        } else {
            brightness / MAX_BRIGHTNESS.toFloat()
        }
        activity.window.attributes = lp
    }

    fun restoreSystemBrightness(activity: Activity, brightnessMode: Int, brightness: Int) {
        setSystemBrightnessMode(activity, brightnessMode)
        setSystemBrightness(activity, brightness)
        setWindowBrightness(activity, -MAX_BRIGHTNESS)
    }
}
