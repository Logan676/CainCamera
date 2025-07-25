package com.cgfay.camera.render

import android.graphics.SurfaceTexture
import android.opengl.GLES30
import android.os.Handler
import android.os.Looper
import android.os.Process
import android.util.Log
import android.view.MotionEvent
import android.view.Surface
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import com.cgfay.camera.camera.CameraParam
import com.cgfay.camera.presenter.PreviewPresenter
import com.cgfay.filter.gles.EglCore
import com.cgfay.filter.gles.WindowSurface
import com.cgfay.filter.glfilter.color.bean.DynamicColor
import com.cgfay.filter.glfilter.makeup.bean.DynamicMakeup
import com.cgfay.filter.glfilter.stickers.StaticStickerNormalFilter
import com.cgfay.filter.glfilter.stickers.bean.DynamicSticker
import com.cgfay.filter.glfilter.utils.OpenGLUtils
import java.lang.ref.WeakReference
import javax.microedition.khronos.opengles.GL10

/**
 * Camera renderer running on a dedicated thread.
 */
class CameraRenderer(@NonNull presenter: PreviewPresenter) : Thread(TAG) {

    private val sync = Object()
    private var priority = Process.THREAD_PRIORITY_DISPLAY
    private var looper: Looper? = null
    private var handler: CameraRenderHandler? = null

    private var imageReader: GLImageReader? = null
    private var eglCore: EglCore? = null
    private var displaySurface: WindowSurface? = null
    @Volatile
    private var needToAttach = false
    private var weakSurfaceTexture: WeakReference<SurfaceTexture>? = null
    private val matrix = FloatArray(16)
    private var inputTexture = OpenGLUtils.GL_NOT_TEXTURE
    private var currentTexture = 0
    private val renderManager = RenderManager()
    private val frameRateMeter = FrameRateMeter()
    private var cameraParam = CameraParam.getInstance()
    private val weakPresenter = WeakReference(presenter)
    @Volatile
    private var threadStarted = false

    fun initRenderer() {
        synchronized(this) {
            if (!threadStarted) {
                start()
                threadStarted = true
            }
        }
    }

    fun destroyRenderer() {
        synchronized(this) { quit() }
    }

    fun onPause() {
        weakSurfaceTexture?.clear()
    }

    fun onSurfaceCreated(surface: Surface) {
        handler?.let { it.sendMessage(it.obtainMessage(CameraRenderHandler.MSG_INIT, surface)) }
    }

    fun onSurfaceCreated(surfaceTexture: SurfaceTexture) {
        handler?.let { it.sendMessage(it.obtainMessage(CameraRenderHandler.MSG_INIT, surfaceTexture)) }
    }

    fun onSurfaceChanged(width: Int, height: Int) {
        handler?.sendMessage(handler!!.obtainMessage(CameraRenderHandler.MSG_DISPLAY_CHANGE, width, height))
    }

    fun onSurfaceDestroyed() {
        handler?.sendMessage(handler!!.obtainMessage(CameraRenderHandler.MSG_DESTROY))
    }

    fun setTextureSize(width: Int, height: Int) {
        renderManager.setTextureSize(width, height)
        imageReader?.init(width, height)
    }

    fun bindInputSurfaceTexture(surfaceTexture: SurfaceTexture) {
        queueEvent { onBindInputSurfaceTexture(surfaceTexture) }
    }

    private fun releaseResources() {
        Log.d(TAG, "release")
        imageReader?.release(); imageReader = null
        displaySurface?.makeCurrent()
        if (inputTexture != OpenGLUtils.GL_NOT_TEXTURE) {
            OpenGLUtils.deleteTexture(inputTexture)
            inputTexture = OpenGLUtils.GL_NOT_TEXTURE
        }
        renderManager.release()
        weakSurfaceTexture?.clear()
        displaySurface?.release(); displaySurface = null
        eglCore?.release(); eglCore = null
    }

    fun takePicture() {
        synchronized(sync) { cameraParam.isTakePicture = true }
        requestRender()
    }

    fun queueEvent(runnable: Runnable) { handler?.queueEvent(runnable) }

    fun requestRender() { handler?.sendEmptyMessage(CameraRenderHandler.MSG_RENDER) }

    fun getTouchableFilter(e: MotionEvent): StaticStickerNormalFilter? {
        synchronized(sync) {
            return renderManager.touchDown(e)
        }
    }

    fun changeFilter(color: DynamicColor) {
        handler?.sendMessage(handler!!.obtainMessage(CameraRenderHandler.MSG_CHANGE_FILTER, color))
    }

    fun changeMakeup(makeup: DynamicMakeup?) {
        handler?.sendMessage(handler!!.obtainMessage(CameraRenderHandler.MSG_CHANGE_MAKEUP, makeup))
    }

    fun changeResource(color: DynamicColor) {
        handler?.sendMessage(handler!!.obtainMessage(CameraRenderHandler.MSG_CHANGE_RESOURCE, color))
    }

    fun changeResource(sticker: DynamicSticker) {
        handler?.sendMessage(handler!!.obtainMessage(CameraRenderHandler.MSG_CHANGE_RESOURCE, sticker))
    }

    fun changeEdgeBlur(hasBlur: Boolean) {
        handler?.sendMessage(handler!!.obtainMessage(CameraRenderHandler.MSG_CHANGE_EDGE_BLUR, hasBlur))
    }

    // Internal methods -------------------------------------------------
    fun initRender(surface: Surface) {
        if (weakPresenter.get() == null) return
        Log.d(TAG, "initRender")
        eglCore = EglCore(null, EglCore.FLAG_RECORDABLE)
        displaySurface = WindowSurface(eglCore, surface, false)
        displaySurface!!.makeCurrent()
        GLES30.glDisable(GL10.GL_DITHER)
        GLES30.glClearColor(0f, 0f, 0f, 0f)
        GLES30.glEnable(GL10.GL_CULL_FACE)
        GLES30.glEnable(GL10.GL_DEPTH_TEST)
        renderManager.init(weakPresenter.get()!!.context)
        weakPresenter.get()?.onBindSharedContext(eglCore!!.eglContext)
    }

    fun initRender(surfaceTexture: SurfaceTexture) {
        Log.d(TAG, "initRender")
        eglCore = EglCore(null, EglCore.FLAG_RECORDABLE)
        displaySurface = WindowSurface(eglCore, surfaceTexture)
        displaySurface!!.makeCurrent()
        GLES30.glDisable(GL10.GL_DITHER)
        GLES30.glClearColor(0f, 0f, 0f, 0f)
        GLES30.glEnable(GL10.GL_CULL_FACE)
        GLES30.glEnable(GL10.GL_DEPTH_TEST)
        renderManager.init(weakPresenter.get()!!.context)
        weakPresenter.get()?.onBindSharedContext(eglCore!!.eglContext)
    }

    fun setDisplaySize(width: Int, height: Int) {
        renderManager.setDisplaySize(width, height)
    }

    fun onDrawFrame() {
        if (displaySurface == null || weakSurfaceTexture?.get() == null) return
        displaySurface!!.makeCurrent()
        var timeStamp = 0L
        synchronized(this) {
            val surfaceTexture = weakSurfaceTexture!!.get()
            updateSurfaceTexture(surfaceTexture)
            timeStamp = surfaceTexture.timestamp
        }
        currentTexture = renderManager.drawFrame(inputTexture, matrix)
        weakPresenter.get()?.onRecordFrameAvailable(currentTexture, timeStamp)
        renderManager.drawFacePoint(currentTexture)
        displaySurface!!.swapBuffers()
        synchronized(sync) {
            if (cameraParam.isTakePicture) {
                if (imageReader == null) {
                    imageReader = GLImageReader(eglCore!!.eglContext) { bitmap ->
                        cameraParam.captureCallback?.onCapture(bitmap)
                    }
                    imageReader!!.init(renderManager.getTextureWidth(), renderManager.getTextureHeight())
                }
                imageReader?.drawFrame(currentTexture)
                cameraParam.isTakePicture = false
            }
        }
        calculateFps()
    }

    private fun updateSurfaceTexture(surfaceTexture: SurfaceTexture) {
        synchronized(this) {
            if (needToAttach) {
                if (inputTexture != OpenGLUtils.GL_NOT_TEXTURE) {
                    OpenGLUtils.deleteTexture(inputTexture)
                }
                inputTexture = OpenGLUtils.createOESTexture()
                try {
                    surfaceTexture.attachToGLContext(inputTexture)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                needToAttach = false
            }
        }
        try {
            surfaceTexture.updateTexImage()
            surfaceTexture.getTransformMatrix(matrix)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun onBindInputSurfaceTexture(surfaceTexture: SurfaceTexture) {
        synchronized(this) {
            if (weakSurfaceTexture == null || weakSurfaceTexture!!.get() !== surfaceTexture) {
                weakSurfaceTexture = WeakReference(surfaceTexture)
                needToAttach = true
            }
        }
    }

    private fun calculateFps() {
        cameraParam.fpsCallback?.let {
            frameRateMeter.drawFrameCount()
            it.onFpsCallback(frameRateMeter.fps)
        }
    }

    fun changeEdgeBlurFilter(enableEdgeBlur: Boolean) {
        synchronized(sync) {
            displaySurface!!.makeCurrent()
            renderManager.changeEdgeBlurFilter(enableEdgeBlur)
        }
    }

    fun changeDynamicFilter(color: DynamicColor) {
        synchronized(sync) {
            displaySurface!!.makeCurrent()
            renderManager.changeDynamicFilter(color)
        }
    }

    fun changeDynamicMakeup(makeup: DynamicMakeup?) {
        synchronized(sync) {
            displaySurface!!.makeCurrent()
            renderManager.changeDynamicMakeup(makeup)
        }
    }

    fun changeDynamicResource(color: DynamicColor) {
        synchronized(sync) {
            displaySurface!!.makeCurrent()
            renderManager.changeDynamicResource(color)
        }
    }

    fun changeDynamicResource(sticker: DynamicSticker) {
        synchronized(sync) {
            displaySurface!!.makeCurrent()
            renderManager.changeDynamicResource(sticker)
        }
    }

    override fun run() {
        Looper.prepare()
        synchronized(this) {
            looper = Looper.myLooper()
            notifyAll()
        }
        Process.setThreadPriority(priority)
        Looper.loop()
        handler!!.handleQueueEvent()
        handler!!.removeCallbacksAndMessages(null)
        releaseResources()
        threadStarted = false
        Log.d(TAG, "Thread has delete!")
    }

    private fun getLooperSafe(): Looper? {
        if (!isAlive) return null
        synchronized(this) {
            while (isAlive && looper == null) {
                try {
                    (this as Object).wait()
                } catch (_: InterruptedException) {
                }
            }
        }
        return looper
    }

    @NonNull
    fun getHandler(): CameraRenderHandler {
        if (handler == null) {
            handler = CameraRenderHandler(getLooperSafe()!!, this)
        }
        return handler!!
    }

    private fun quit(): Boolean {
        val l = getLooperSafe()
        l?.quitSafely()
        return l != null
    }

    companion object {
        private const val TAG = "CameraRenderer"
    }
}
