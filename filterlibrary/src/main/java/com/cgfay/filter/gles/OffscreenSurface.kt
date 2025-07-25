package com.cgfay.filter.gles

class OffscreenSurface(eglCore: EglCore, width: Int, height: Int) : EglSurfaceBase(eglCore) {
    init {
        createOffscreenSurface(width, height)
    }

    fun release() {
        releaseEglSurface()
    }
}
