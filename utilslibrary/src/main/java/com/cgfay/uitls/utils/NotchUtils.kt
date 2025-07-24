package com.cgfay.uitls.utils

import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.os.Build
import android.util.Log
import android.view.DisplayCutout
import android.view.View
import android.view.WindowInsets

/**
 * 刘海屏工具
 * @author CainHuang
 * @date 2019/6/18
 */
object NotchUtils {

    /** 判断是否是刘海屏 */
    @JvmStatic
    fun hasNotchScreen(activity: Activity): Boolean {
        return (getInt("ro.miui.notch", activity) == 1
                || hasNotchAtHuawei(activity)
                || hasNotchAtOPPO(activity)
                || hasNotchAtVivo(activity)
                || isAndroidP(activity) != null
                || getBarHeight(activity) >= 80)
    }

    /** Android P 刘海屏判断 */
    @TargetApi(28)
    private fun isAndroidP(activity: Activity): DisplayCutout? {
        val decorView = activity.window.decorView
        if (decorView != null && Build.VERSION.SDK_INT >= 28) {
            val windowInsets = decorView.rootWindowInsets
            if (windowInsets != null) return windowInsets.displayCutout
        }
        return null
    }

    /** 获取状态栏高度 */
    private fun getBarHeight(activity: Activity): Int {
        val resourceId = activity.resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (resourceId > 0) activity.resources.getDimensionPixelSize(resourceId) else 0
    }

    /** 判断是否小米手机 */
    private fun isXiaomi(): Boolean {
        return "Xiaomi" == Build.MANUFACTURER
    }

    /** 小米刘海屏 */
    private fun getInt(key: String, activity: Activity): Int {
        var result = 0
        if (isXiaomi()) {
            try {
                val classLoader = activity.classLoader
                val systemProperties = classLoader.loadClass("android.os.SystemProperties")
                val paramTypes = arrayOf<Class<*>>(String::class.java, Int::class.javaPrimitiveType!!)
                val getInt = systemProperties.getMethod("getInt", *paramTypes)
                val params = arrayOf<Any>(key, 0)
                result = getInt.invoke(systemProperties, *params) as Int
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return result
    }

    /** 华为刘海屏 */
    private fun hasNotchAtHuawei(context: Context): Boolean {
        var ret = false
        try {
            val classLoader = context.classLoader
            val hwNotchSizeUtil = classLoader.loadClass("com.huawei.android.util.HwNotchSizeUtil")
            val get = hwNotchSizeUtil.getMethod("hasNotchInScreen")
            ret = get.invoke(hwNotchSizeUtil) as Boolean
        } catch (e: Exception) {
            Log.e("Notch", "hasNotchAtHuawei Exception")
        } finally {
            return ret
        }
    }

    private const val VIVO_NOTCH = 0x00000020

    /** VIVO刘海屏 */
    private fun hasNotchAtVivo(context: Context): Boolean {
        var ret = false
        try {
            val classLoader = context.classLoader
            val ftFeature = classLoader.loadClass("android.util.FtFeature")
            val method = ftFeature.getMethod("isFeatureSupport", Int::class.javaPrimitiveType)
            ret = method.invoke(ftFeature, VIVO_NOTCH) as Boolean
        } catch (e: Exception) {
            Log.e("Notch", "hasNotchAtVivo Exception")
        } finally {
            return ret
        }
    }

    /** OPPO刘海屏 */
    private fun hasNotchAtOPPO(context: Context): Boolean {
        return context.packageManager.hasSystemFeature("com.oppo.feature.screen.heteromorphism")
    }
}
