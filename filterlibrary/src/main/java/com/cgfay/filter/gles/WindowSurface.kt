package com.cgfay.filter.gles

import android.graphics.SurfaceTexture
import android.view.Surface

class WindowSurface : EglSurfaceBase {
    private var mSurface: Surface? = null
    private var mReleaseSurface: Boolean = false

    constructor(eglCore: EglCore, surface: Surface, releaseSurface: Boolean) : super(eglCore) {
        createWindowSurface(surface)
        mSurface = surface
        mReleaseSurface = releaseSurface
    }

    constructor(eglCore: EglCore, surfaceTexture: SurfaceTexture) : super(eglCore) {
        createWindowSurface(surfaceTexture)
    }

    fun release() {
        releaseEglSurface()
        if (mSurface != null) {
            if (mReleaseSurface) {
                mSurface!!.release()
            }
            mSurface = null
        }
    }

    fun recreate(newEglCore: EglCore) {
        requireNotNull(mSurface) { "not yet implemented for SurfaceTexture" }
        mEglCore = newEglCore
        createWindowSurface(mSurface!!)
    }
}
