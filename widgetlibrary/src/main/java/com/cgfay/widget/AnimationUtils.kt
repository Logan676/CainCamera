package com.cgfay.widget

import android.view.animation.DecelerateInterpolator
import android.view.animation.Interpolator
import android.view.animation.LinearInterpolator
import androidx.interpolator.view.animation.FastOutLinearInInterpolator
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator

internal object AnimationUtils {
    val LINEAR_INTERPOLATOR: Interpolator = LinearInterpolator()
    val FAST_OUT_SLOW_IN_INTERPOLATOR: Interpolator = FastOutSlowInInterpolator()
    val FAST_OUT_LINEAR_IN_INTERPOLATOR: Interpolator = FastOutLinearInInterpolator()
    val LINEAR_OUT_SLOW_IN_INTERPOLATOR: Interpolator = LinearOutSlowInInterpolator()
    val DECELERATE_INTERPOLATOR: Interpolator = DecelerateInterpolator()

    /**
     * Linear interpolation between [startValue] and [endValue].
     */
    fun lerp(startValue: Float, endValue: Float, fraction: Float): Float =
        startValue + fraction * (endValue - startValue)

    fun lerp(startValue: Int, endValue: Int, fraction: Float): Int =
        startValue + kotlin.math.round(fraction * (endValue - startValue)).toInt()
}
