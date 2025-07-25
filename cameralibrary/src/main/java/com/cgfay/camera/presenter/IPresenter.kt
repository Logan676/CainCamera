package com.cgfay.camera.presenter

import android.content.Context

abstract class IPresenter<T>(private val target: T) {
    fun getTarget(): T = target

    open fun onCreate() {}
    open fun onStart() {}
    open fun onResume() {}
    open fun onPause() {}
    open fun onStop() {}
    open fun onDestroy() {}

    abstract fun getContext(): Context
}
