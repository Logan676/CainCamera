package com.cgfay.uitls.utils

import android.content.Context
import android.graphics.Point
import android.os.Build
import android.util.DisplayMetrics
import android.view.Display
import android.view.WindowManager

/**
 * 状态栏和虚拟导航栏
 * @author CainHuang
 * @date 2019/6/18
 */
object DisplayUtils {

    /**
     * 获取屏幕宽度
     */
    @JvmStatic
    fun getScreenWidth(context: Context): Int {
        return context.resources.displayMetrics.widthPixels
    }

    /**
     * 获取屏幕高度
     */
    @JvmStatic
    fun getScreenHeight(context: Context): Int {
        return context.resources.displayMetrics.heightPixels
    }

    /**
     * 获取虚拟导航栏高度
     */
    @JvmStatic
    fun getVirtualBarHeight(context: Context): Int {
        var vh = 0
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = windowManager.defaultDisplay
        val dm = DisplayMetrics()
        try {
            val c: Class<*> = Class.forName("android.view.Display")
            val method = c.getMethod("getRealMetrics", DisplayMetrics::class.java)
            method.invoke(display, dm)
            vh = dm.heightPixels - display.height
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return vh
    }

    @Volatile
    private var mHasCheckAllScreen = false
    @Volatile
    private var mIsAllScreenDevice = false

    /**
     * 判断是否全面屏
     */
    @JvmStatic
    fun isFullScreenDevice(context: Context): Boolean {
        if (mHasCheckAllScreen) {
            return mIsAllScreenDevice
        }
        mHasCheckAllScreen = true
        mIsAllScreenDevice = false
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return false
        }
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager?
        if (windowManager != null) {
            val display = windowManager.defaultDisplay
            val point = Point()
            display.getRealSize(point)
            val width: Float
            val height: Float
            if (point.x < point.y) {
                width = point.x.toFloat()
                height = point.y.toFloat()
            } else {
                width = point.y.toFloat()
                height = point.x.toFloat()
            }
            if (height / width >= 1.97f) {
                mIsAllScreenDevice = true
            }
        }
        return mIsAllScreenDevice
    }

    /**
     * 获取显示宽度
     */
    @JvmStatic
    fun getDisplayWidth(context: Context): Int {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager? ?: return -1
        val display = wm.defaultDisplay
        val size = Point()
        display.getRealSize(size)
        return size.x
    }

    /**
     * 获取显示高度
     */
    @JvmStatic
    fun getDisplayHeight(context: Context): Int {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager? ?: return -1
        val display = wm.defaultDisplay
        val size = Point()
        display.getRealSize(size)
        return size.y
    }
}
