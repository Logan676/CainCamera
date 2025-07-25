package com.cgfay.filter.glfilter.effect

import android.content.Context
import com.cgfay.filter.glfilter.base.GLImageFilter

/**
 * Base class for time based effects.
 */
open class GLImageEffectFilter : GLImageFilter {

    /** Current time in milliseconds. */
    protected var mCurrentPosition: Long = 0

    constructor(context: Context) : super(context)

    constructor(context: Context, vertexShader: String, fragmentShader: String) :
        super(context, vertexShader, fragmentShader)

    /**
     * Bind current time in ms.
     */
    fun setCurrentPosition(timeMs: Long) {
        mCurrentPosition = timeMs
        calculateInterval()
    }

    /**
     * Calculate step for animations.
     */
    protected open fun calculateInterval() {}
}
