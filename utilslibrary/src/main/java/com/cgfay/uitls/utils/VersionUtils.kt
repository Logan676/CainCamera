package com.cgfay.uitls.utils

import android.annotation.TargetApi
import android.os.Build
import android.os.StrictMode

object VersionUtils {
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    fun enableStrictMode(klass: Class<*>) {
        if (hasGingerbread()) {
            val threadPolicyBuilder = StrictMode.ThreadPolicy.Builder()
                .detectAll()
                .penaltyLog()
            val vmPolicyBuilder = StrictMode.VmPolicy.Builder()
                .detectAll()
                .penaltyLog()
            if (hasHoneycomb()) {
                threadPolicyBuilder.penaltyFlashScreen()
                vmPolicyBuilder.setClassInstanceLimit(klass, 1)
            }
            StrictMode.setThreadPolicy(threadPolicyBuilder.build())
            StrictMode.setVmPolicy(vmPolicyBuilder.build())
        }
    }

    fun hasFroyo(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO
    fun hasGingerbread(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD
    fun hasHoneycomb(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB
    fun hasHoneycombMR1(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1
    fun hasJellyBean(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN
    fun hasKitKat(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
}
