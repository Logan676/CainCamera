package com.cgfay.uitls.utils

import android.content.Context
import android.util.TypedValue

object DensityUtils {
    fun dp2px(context: Context, dpVal: Float): Int =
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dpVal,
            context.resources.displayMetrics
        ).toInt()

    fun px2dp(context: Context, pxVal: Float): Float =
        pxVal / context.resources.displayMetrics.density

    fun sp2px(context: Context, spVal: Float): Int =
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            spVal,
            context.resources.displayMetrics
        ).toInt()

    fun px2sp(context: Context, pxVal: Float): Float =
        pxVal / context.resources.displayMetrics.scaledDensity
}
