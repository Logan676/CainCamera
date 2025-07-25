package com.cgfay.filter.gles

import android.graphics.SurfaceTexture
import android.opengl.EGL14
import android.opengl.EGLConfig
import android.opengl.EGLContext
import android.opengl.EGLDisplay
import android.opengl.EGLExt
import android.opengl.EGLSurface
import android.util.Log
import android.view.Surface

class EglCore @JvmOverloads constructor(sharedContext: EGLContext? = null, flags: Int = 0) {
    private var mEGLDisplay: EGLDisplay = EGL14.EGL_NO_DISPLAY
    private var mEGLContext: EGLContext = EGL14.EGL_NO_CONTEXT
    private var mEGLConfig: EGLConfig? = null
    private var mGlVersion = -1

    init {
        var ctx = sharedContext ?: EGL14.EGL_NO_CONTEXT
        mEGLDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
        if (mEGLDisplay == EGL14.EGL_NO_DISPLAY) {
            throw RuntimeException("unable to get EGL14 display")
        }
        val version = IntArray(2)
        if (!EGL14.eglInitialize(mEGLDisplay, version, 0, version, 1)) {
            mEGLDisplay = null
            throw RuntimeException("unable to initialize EGL14")
        }
        if (flags and FLAG_TRY_GLES3 != 0) {
            val config = getConfig(flags, 3)
            if (config != null) {
                val attrib3List = intArrayOf(
                    EGL14.EGL_CONTEXT_CLIENT_VERSION, 3,
                    EGL14.EGL_NONE
                )
                val context = EGL14.eglCreateContext(mEGLDisplay, config, ctx, attrib3List, 0)
                if (EGL14.eglGetError() == EGL14.EGL_SUCCESS) {
                    mEGLConfig = config
                    mEGLContext = context
                    mGlVersion = 3
                }
            }
        }
        if (mEGLContext == EGL14.EGL_NO_CONTEXT) {
            val config = getConfig(flags, 2)
            require(config != null) { "Unable to find a suitable EGLConfig" }
            val attrib2List = intArrayOf(
                EGL14.EGL_CONTEXT_CLIENT_VERSION, 2,
                EGL14.EGL_NONE
            )
            val context = EGL14.eglCreateContext(mEGLDisplay, config, ctx, attrib2List, 0)
            checkEglError("eglCreateContext")
            mEGLConfig = config
            mEGLContext = context
            mGlVersion = 2
        }
        val values = IntArray(1)
        EGL14.eglQueryContext(
            mEGLDisplay,
            mEGLContext,
            EGL14.EGL_CONTEXT_CLIENT_VERSION,
            values,
            0
        )
        Log.d(TAG, "EGLContext created, client version " + values[0])
    }

    fun release() {
        if (mEGLDisplay != EGL14.EGL_NO_DISPLAY) {
            EGL14.eglMakeCurrent(
                mEGLDisplay,
                EGL14.EGL_NO_SURFACE,
                EGL14.EGL_NO_SURFACE,
                EGL14.EGL_NO_CONTEXT
            )
            EGL14.eglDestroyContext(mEGLDisplay, mEGLContext)
            EGL14.eglReleaseThread()
            EGL14.eglTerminate(mEGLDisplay)
        }
        mEGLDisplay = EGL14.EGL_NO_DISPLAY
        mEGLContext = EGL14.EGL_NO_CONTEXT
        mEGLConfig = null
    }

    val eglContext: EGLContext
        get() = mEGLContext

    @Throws(Throwable::class)
    protected fun finalize() {
        try {
            if (mEGLDisplay != EGL14.EGL_NO_DISPLAY) {
                Log.w(TAG, "WARNING: EglCore was not explicitly released -- state may be leaked")
                release()
            }
        } finally {
        }
    }

    fun releaseSurface(eglSurface: EGLSurface) {
        EGL14.eglDestroySurface(mEGLDisplay, eglSurface)
    }

    fun createWindowSurface(surface: Any): EGLSurface {
        require(surface is Surface || surface is SurfaceTexture) { "invalid surface: $surface" }
        val surfaceAttribs = intArrayOf(EGL14.EGL_NONE)
        val eglSurface = EGL14.eglCreateWindowSurface(mEGLDisplay, mEGLConfig, surface, surfaceAttribs, 0)
        checkEglError("eglCreateWindowSurface")
        requireNotNull(eglSurface) { "surface was null" }
        return eglSurface
    }

    fun createOffscreenSurface(width: Int, height: Int): EGLSurface {
        val surfaceAttribs = intArrayOf(
            EGL14.EGL_WIDTH, width,
            EGL14.EGL_HEIGHT, height,
            EGL14.EGL_NONE
        )
        val eglSurface = EGL14.eglCreatePbufferSurface(mEGLDisplay, mEGLConfig, surfaceAttribs, 0)
        checkEglError("eglCreatePbufferSurface")
        requireNotNull(eglSurface) { "surface was null" }
        return eglSurface
    }

    fun makeCurrent(eglSurface: EGLSurface) {
        if (mEGLDisplay == EGL14.EGL_NO_DISPLAY) {
            Log.d(TAG, "NOTE: makeCurrent w/o display")
        }
        if (!EGL14.eglMakeCurrent(mEGLDisplay, eglSurface, eglSurface, mEGLContext)) {
            throw RuntimeException("eglMakeCurrent failed")
        }
    }

    fun makeCurrent(drawSurface: EGLSurface, readSurface: EGLSurface) {
        if (mEGLDisplay == EGL14.EGL_NO_DISPLAY) {
            Log.d(TAG, "NOTE: makeCurrent w/o display")
        }
        if (!EGL14.eglMakeCurrent(mEGLDisplay, drawSurface, readSurface, mEGLContext)) {
            throw RuntimeException("eglMakeCurrent(draw,read) failed")
        }
    }

    fun makeNothingCurrent() {
        if (!EGL14.eglMakeCurrent(
                mEGLDisplay,
                EGL14.EGL_NO_SURFACE,
                EGL14.EGL_NO_SURFACE,
                EGL14.EGL_NO_CONTEXT
            )
        ) {
            throw RuntimeException("eglMakeCurrent failed")
        }
    }

    fun swapBuffers(eglSurface: EGLSurface): Boolean {
        return EGL14.eglSwapBuffers(mEGLDisplay, eglSurface)
    }

    fun setPresentationTime(eglSurface: EGLSurface, nsecs: Long) {
        EGLExt.eglPresentationTimeANDROID(mEGLDisplay, eglSurface, nsecs)
    }

    fun isCurrent(eglSurface: EGLSurface): Boolean {
        return mEGLContext == EGL14.eglGetCurrentContext() &&
            eglSurface == EGL14.eglGetCurrentSurface(EGL14.EGL_DRAW)
    }

    fun querySurface(eglSurface: EGLSurface, what: Int): Int {
        val value = IntArray(1)
        EGL14.eglQuerySurface(mEGLDisplay, eglSurface, what, value, 0)
        return value[0]
    }

    fun queryString(what: Int): String? {
        return EGL14.eglQueryString(mEGLDisplay, what)
    }

    val glVersion: Int
        get() = mGlVersion

    private fun getConfig(flags: Int, version: Int): EGLConfig? {
        var renderableType = EGL14.EGL_OPENGL_ES2_BIT
        if (version >= 3) {
            renderableType = renderableType or EGLExt.EGL_OPENGL_ES3_BIT_KHR
        }
        val attribList = intArrayOf(
            EGL14.EGL_RED_SIZE, 8,
            EGL14.EGL_GREEN_SIZE, 8,
            EGL14.EGL_BLUE_SIZE, 8,
            EGL14.EGL_ALPHA_SIZE, 8,
            EGL14.EGL_RENDERABLE_TYPE, renderableType,
            EGL14.EGL_NONE, 0,
            EGL14.EGL_NONE
        )
        if (flags and FLAG_RECORDABLE != 0) {
            attribList[attribList.size - 3] = EGL_RECORDABLE_ANDROID
            attribList[attribList.size - 2] = 1
        }
        val configs = arrayOfNulls<EGLConfig>(1)
        val numConfigs = IntArray(1)
        if (!EGL14.eglChooseConfig(
                mEGLDisplay,
                attribList,
                0,
                configs,
                0,
                configs.size,
                numConfigs,
                0
            )
        ) {
            Log.w(TAG, "unable to find RGB8888 / $version EGLConfig")
            return null
        }
        return configs[0]
    }

    private fun checkEglError(msg: String) {
        val error = EGL14.eglGetError()
        if (error != EGL14.EGL_SUCCESS) {
            throw RuntimeException("$msg: EGL error: 0x" + Integer.toHexString(error))
        }
    }

    companion object {
        private const val TAG = "EGLCore"
        const val FLAG_RECORDABLE = 0x01
        const val FLAG_TRY_GLES3 = 0x02
        private const val EGL_RECORDABLE_ANDROID = 0x3142

        fun logCurrent(msg: String) {
            val display = EGL14.eglGetCurrentDisplay()
            val context = EGL14.eglGetCurrentContext()
            val surface = EGL14.eglGetCurrentSurface(EGL14.EGL_DRAW)
            Log.i(TAG, "Current EGL ($msg): display=" + display + ", context=" + context + ", surface=" + surface)
        }
    }
}
