package com.cgfay.filter.gles

import android.opengl.EGL14
import android.opengl.EGLSurface
import android.opengl.GLES30
import android.util.Log
import com.cgfay.filter.glfilter.utils.OpenGLUtils
import java.nio.ByteBuffer
import java.nio.ByteOrder

open class EglSurfaceBase(protected var mEglCore: EglCore) {
    private var mEGLSurface: EGLSurface = EGL14.EGL_NO_SURFACE
    private var mWidth = -1
    private var mHeight = -1

    fun createWindowSurface(surface: Any) {
        check(mEGLSurface == EGL14.EGL_NO_SURFACE) { "surface already created" }
        mEGLSurface = mEglCore.createWindowSurface(surface)
    }

    fun createOffscreenSurface(width: Int, height: Int) {
        check(mEGLSurface == EGL14.EGL_NO_SURFACE) { "surface already created" }
        mEGLSurface = mEglCore.createOffscreenSurface(width, height)
        mWidth = width
        mHeight = height
    }

    val width: Int
        get() = if (mWidth < 0) mEglCore.querySurface(mEGLSurface, EGL14.EGL_WIDTH) else mWidth

    val height: Int
        get() = if (mHeight < 0) mEglCore.querySurface(mEGLSurface, EGL14.EGL_HEIGHT) else mHeight

    fun releaseEglSurface() {
        mEglCore.releaseSurface(mEGLSurface)
        mEGLSurface = EGL14.EGL_NO_SURFACE
        mWidth = -1
        mHeight = -1
    }

    fun makeCurrent() {
        mEglCore.makeCurrent(mEGLSurface)
    }

    fun makeCurrentReadFrom(readSurface: EglSurfaceBase) {
        mEglCore.makeCurrent(mEGLSurface, readSurface.mEGLSurface)
    }

    fun swapBuffers(): Boolean {
        val result = mEglCore.swapBuffers(mEGLSurface)
        if (!result) {
            Log.d(TAG, "WARNING: swapBuffers() failed")
        }
        return result
    }

    fun setPresentationTime(nsecs: Long) {
        mEglCore.setPresentationTime(mEGLSurface, nsecs)
    }

    fun getCurrentFrame(): ByteBuffer {
        val width = width
        val height = height
        val buf = ByteBuffer.allocateDirect(width * height * 4)
        buf.order(ByteOrder.LITTLE_ENDIAN)
        GLES30.glReadPixels(0, 0, width, height, GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE, buf)
        OpenGLUtils.checkGlError("glReadPixels")
        buf.rewind()
        return buf
    }

    companion object {
        private const val TAG = "EglSurfaceBase"
    }
}
