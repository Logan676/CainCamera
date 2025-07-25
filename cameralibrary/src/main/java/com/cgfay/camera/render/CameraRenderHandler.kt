package com.cgfay.camera.render

import android.graphics.SurfaceTexture
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.Surface
import android.view.SurfaceHolder
import com.cgfay.filter.glfilter.color.bean.DynamicColor
import com.cgfay.filter.glfilter.makeup.bean.DynamicMakeup
import com.cgfay.filter.glfilter.stickers.bean.DynamicSticker
import java.lang.ref.WeakReference
import java.util.ArrayList

/**
 * Handler for camera rendering thread.
 */
class CameraRenderHandler(looper: Looper, renderer: CameraRenderer) : Handler(looper) {

    private val weakRender = WeakReference(renderer)
    private val eventQueue = ArrayList<Runnable>()

    override fun handleMessage(msg: Message) {
        val render = weakRender.get() ?: return
        handleQueueEvent()
        when (msg.what) {
            MSG_INIT -> when (val obj = msg.obj) {
                is SurfaceHolder -> render.initRender(obj.surface)
                is Surface -> render.initRender(obj)
                is SurfaceTexture -> render.initRender(obj)
            }
            MSG_DISPLAY_CHANGE -> render.setDisplaySize(msg.arg1, msg.arg2)
            MSG_DESTROY -> render.release()
            MSG_RENDER -> render.onDrawFrame()
            MSG_CHANGE_FILTER -> render.changeDynamicFilter(msg.obj as DynamicColor)
            MSG_CHANGE_MAKEUP ->
                render.changeDynamicMakeup(msg.obj as? DynamicMakeup)
            MSG_CHANGE_RESOURCE -> when (val o = msg.obj) {
                null -> render.changeDynamicResource(null as DynamicSticker?)
                is DynamicColor -> render.changeDynamicResource(o)
                is DynamicSticker -> render.changeDynamicResource(o)
            }
            MSG_CHANGE_EDGE_BLUR -> render.changeEdgeBlurFilter(msg.obj as Boolean)
        }
    }

    fun handleQueueEvent() {
        synchronized(this) {
            while (eventQueue.isNotEmpty()) {
                val runnable = eventQueue.removeAt(0)
                runnable.run()
            }
        }
    }

    fun queueEvent(runnable: Runnable) {
        requireNotNull(runnable) { "runnable must not be null" }
        synchronized(this) {
            eventQueue.add(runnable)
            notifyAll()
        }
    }

    companion object {
        const val MSG_INIT = 0x01
        const val MSG_DISPLAY_CHANGE = 0x02
        const val MSG_DESTROY = 0x03
        const val MSG_RENDER = 0x04
        const val MSG_CHANGE_FILTER = 0x05
        const val MSG_CHANGE_MAKEUP = 0x06
        const val MSG_CHANGE_RESOURCE = 0x07
        const val MSG_CHANGE_EDGE_BLUR = 0x08
    }
}
