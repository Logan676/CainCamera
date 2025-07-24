package com.cgfay.picker

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import java.lang.ref.WeakReference

class MediaPicker private constructor(activity: FragmentActivity?, fragment: Fragment? = null) {
    private val weakActivity = WeakReference(activity)
    private val weakFragment = WeakReference(fragment)

    val activity: FragmentActivity?
        get() = weakActivity.get()

    val fragment: Fragment?
        get() = weakFragment.get()

    companion object {
        const val PICKER_PARAMS = "PICKER_PARAMS"

        /** Create a picker starting from an activity. This is the preferred
         *  entry point when using Jetpack Compose screens. */
        fun from(activity: FragmentActivity): MediaPickerBuilder = MediaPickerBuilder(MediaPicker(activity))

        /** Legacy helper for fragment-based UIs. */
        fun from(fragment: Fragment): MediaPickerBuilder = MediaPickerBuilder(MediaPicker(fragment.activity, fragment))
    }
}
