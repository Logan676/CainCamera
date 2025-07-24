package com.cgfay.widget

import android.content.Context
import androidx.appcompat.R

/**
 * Kotlin version of ThemeUtils.
 * Validates that the current context is using an AppCompat-based theme.
 */
internal object ThemeUtils {
    private val APPCOMPAT_CHECK_ATTRS = intArrayOf(R.attr.colorPrimary)

    fun checkAppCompatTheme(context: Context) {
        val a = context.obtainStyledAttributes(APPCOMPAT_CHECK_ATTRS)
        val failed = !a.hasValue(0)
        a.recycle()
        if (failed) {
            throw IllegalArgumentException(
                "You need to use a Theme.AppCompat theme (or descendant) with the design library.")
        }
    }
}
