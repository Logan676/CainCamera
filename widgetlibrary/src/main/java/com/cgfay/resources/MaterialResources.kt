package com.cgfay.resources

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.content.res.TypedArray
import android.graphics.drawable.Drawable
import androidx.annotation.RestrictTo
import androidx.annotation.StyleableRes
import androidx.appcompat.content.res.AppCompatResources
import com.google.android.material.resources.TextAppearance

/**
 * Kotlin port of MaterialResources utility originally implemented in Java.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
object MaterialResources {
    @JvmStatic
    fun getColorStateList(context: Context, attributes: TypedArray, @StyleableRes index: Int): ColorStateList? {
        if (attributes.hasValue(index)) {
            val resourceId = attributes.getResourceId(index, 0)
            if (resourceId != 0) {
                AppCompatResources.getColorStateList(context, resourceId)?.let {
                    return it
                }
            }
        }
        return attributes.getColorStateList(index)
    }

    @JvmStatic
    fun getDrawable(context: Context, attributes: TypedArray, @StyleableRes index: Int): Drawable? {
        if (attributes.hasValue(index)) {
            val resourceId = attributes.getResourceId(index, 0)
            if (resourceId != 0) {
                AppCompatResources.getDrawable(context, resourceId)?.let {
                    return it
                }
            }
        }
        return attributes.getDrawable(index)
    }

    @JvmStatic
    @SuppressLint("RestrictedApi")
    fun getTextAppearance(context: Context, attributes: TypedArray, @StyleableRes index: Int): TextAppearance? {
        if (attributes.hasValue(index)) {
            val resourceId = attributes.getResourceId(index, 0)
            if (resourceId != 0) {
                return TextAppearance(context, resourceId)
            }
        }
        return null
    }

    @JvmStatic
    fun getIndexWithValue(attributes: TypedArray, @StyleableRes a: Int, @StyleableRes b: Int): Int =
        if (attributes.hasValue(a)) a else b
}
