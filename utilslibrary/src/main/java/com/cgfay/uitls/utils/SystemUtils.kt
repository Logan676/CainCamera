package com.cgfay.uitls.utils

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.telephony.TelephonyManager
import androidx.core.app.ActivityCompat
import java.util.Locale

object SystemUtils {
    fun getSystemLanguage(): String = Locale.getDefault().language

    fun getSystemLanguageList(): Array<Locale> = Locale.getAvailableLocales()

    fun getSystemVersion(): String = android.os.Build.VERSION.RELEASE

    fun getSystemModel(): String = android.os.Build.MODEL

    fun getDeviceBrand(): String = android.os.Build.BRAND

    @SuppressLint("HardwareIds")
    fun getIMEI(ctx: Context): String? {
        val tm = ctx.getSystemService(Activity.TELEPHONY_SERVICE) as TelephonyManager?
        if (tm != null) {
            if (ActivityCompat.checkSelfPermission(ctx, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                return null
            }
            return tm.deviceId
        }
        return null
    }
}
