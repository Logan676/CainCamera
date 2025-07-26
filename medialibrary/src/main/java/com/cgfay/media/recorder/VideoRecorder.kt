package com.cgfay.media.recorder

import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import androidx.annotation.NonNull
import com.cgfay.filter.gles.EglCore
import com.cgfay.filter.gles.WindowSurface
import com.cgfay.filter.glfilter.base.GLImageFilter
import com.cgfay.filter.glfilter.utils.OpenGLUtils
import com.cgfay.filter.glfilter.utils.TextureRotationUtils
import java.io.IOException
import java.lang.ref.WeakReference
import java.nio.FloatBuffer

/**
 * Kotlin implementation of the old VideoRecorder.
 */
class VideoRecorder : Runnable, VideoEncoder.OnEncodingListener {

    private var inputWindowSurface: WindowSurface? = null
    private var eglCore: EglCore? = null
    private var imageFilter: GLImageFilter? = null
    private var vertexBuffer: FloatBuffer? = null
    private var textureBuffer: FloatBuffer? = null

    private var videoEncoder: VideoEncoder? = null

    @Volatile
    private var handler: RecordHandler? = null

    private val readyFence = Object()
    private var ready = false
    private var running = false

    private var recordListener: OnRecordListener? = null

    private var drawFrameIndex = 0
    private var firstTime = -1L

    fun setOnRecordListener(listener: OnRecordListener?) {
        recordListener = listener
    }

    fun startRecord(params: VideoParams) {
        Log.d(TAG, "VideoRecorder: startRecord()")
        synchronized(readyFence) {
            if (running) {
                Log.w(TAG, "VideoRecorder thread already running")
                return
            }
            running = true
            Thread(this, "VideoRecorder").start()
            while (!ready) {
                try {
                    readyFence.wait()
                } catch (_: InterruptedException) {
                }
            }
        }
        drawFrameIndex = 0
        firstTime = -1
        handler?.sendMessage(handler!!.obtainMessage(MSG_START_RECORDING, params))
    }

    fun stopRecord() {
        handler?.let {
            it.sendMessage(it.obtainMessage(MSG_STOP_RECORDING))
            it.sendMessage(it.obtainMessage(MSG_QUIT))
        }
    }

    fun release() {
        handler?.sendMessage(handler!!.obtainMessage(MSG_QUIT))
    }

    fun isRecording(): Boolean = synchronized(readyFence) { running }

    fun frameAvailable(texture: Int, timestamp: Long) {
        synchronized(readyFence) {
            if (!ready) return
        }
        if (timestamp == 0L) return
        handler?.sendMessage(handler!!.obtainMessage(MSG_FRAME_AVAILABLE, (timestamp shr 32).toInt(), timestamp.toInt(), texture))
    }

    override fun onEncoding(duration: Long) {
        recordListener?.onRecording(MediaType.VIDEO, duration)
    }

    override fun run() {
        Looper.prepare()
        synchronized(readyFence) {
            handler = RecordHandler(this)
            ready = true
            readyFence.notify()
        }
        Looper.loop()
        Log.d(TAG, "Video record thread exiting")
        synchronized(readyFence) {
            ready = false
            running = false
            handler = null
        }
    }

    private class RecordHandler(recorder: VideoRecorder) : Handler(Looper.myLooper()!!) {
        private val weakRecorder = WeakReference(recorder)

        override fun handleMessage(msg: Message) {
            val encoder = weakRecorder.get() ?: return
            when (msg.what) {
                MSG_START_RECORDING -> encoder.onStartRecord(msg.obj as VideoParams)
                MSG_STOP_RECORDING -> encoder.onStopRecord()
                MSG_FRAME_AVAILABLE -> {
                    val timestamp = (msg.arg1.toLong() shl 32) or (msg.arg2.toLong() and 0xffffffffL)
                    encoder.onRecordFrameAvailable(msg.obj as Int, timestamp)
                }
                MSG_QUIT -> Looper.myLooper()?.quit()
                else -> throw RuntimeException("Unhandled msg what=" + msg.what)
            }
        }
    }

    private fun onStartRecord(@NonNull params: VideoParams) {
        Log.d(TAG, "onStartRecord $params")
        vertexBuffer = OpenGLUtils.createFloatBuffer(TextureRotationUtils.CubeVertices)
        textureBuffer = OpenGLUtils.createFloatBuffer(TextureRotationUtils.TextureVertices)
        try {
            videoEncoder = VideoEncoder(params, this)
        } catch (ioe: IOException) {
            throw RuntimeException(ioe)
        }
        eglCore = EglCore(params.eglContext, EglCore.FLAG_RECORDABLE)
        inputWindowSurface = WindowSurface(eglCore, videoEncoder!!.getInputSurface(), true)
        inputWindowSurface!!.makeCurrent()
        imageFilter = GLImageFilter(null)
        imageFilter!!.onInputSizeChanged(params.videoWidth, params.videoHeight)
        imageFilter!!.onDisplaySizeChanged(params.videoWidth, params.videoHeight)
        recordListener?.onRecordStart(MediaType.VIDEO)
    }

    private fun onStopRecord() {
        videoEncoder ?: return
        Log.d(TAG, "onStopRecord")
        videoEncoder!!.drainEncoder(true)
        videoEncoder!!.release()
        imageFilter?.release(); imageFilter = null
        inputWindowSurface?.release(); inputWindowSurface = null
        eglCore?.release(); eglCore = null
        recordListener?.onRecordFinish(
            RecordInfo(videoEncoder!!.getVideoParams().videoPath, videoEncoder!!.getDuration(), MediaType.VIDEO)
        )
        videoEncoder = null
    }

    private fun onRecordFrameAvailable(texture: Int, timestampNanos: Long) {
        Log.d(TAG, "onRecordFrameAvailable")
        val encoder = videoEncoder ?: return
        val mode = encoder.getVideoParams().speedMode
        if (mode == SpeedMode.MODE_FAST || mode == SpeedMode.MODE_EXTRA_FAST) {
            var interval = 2
            if (mode == SpeedMode.MODE_EXTRA_FAST) interval = 3
            if (drawFrameIndex % interval == 0) {
                drawFrame(texture, timestampNanos)
            }
        } else {
            drawFrame(texture, timestampNanos)
        }
        drawFrameIndex++
    }

    private fun drawFrame(texture: Int, timestampNanos: Long) {
        inputWindowSurface!!.makeCurrent()
        imageFilter!!.drawFrame(texture, vertexBuffer, textureBuffer)
        inputWindowSurface!!.setPresentationTime(getPTS(timestampNanos))
        inputWindowSurface!!.swapBuffers()
        videoEncoder!!.drainEncoder(false)
    }

    private fun getPTS(timestampNanos: Long): Long {
        val mode = videoEncoder!!.getVideoParams().speedMode
        return if (mode == SpeedMode.MODE_NORMAL) {
            timestampNanos
        } else {
            val time = System.nanoTime()
            if (firstTime <= 0) firstTime = time
            (firstTime + (time - firstTime) / mode.speed).toLong()
        }
    }

    companion object {
        private const val TAG = "VideoRecorder"
        private const val VERBOSE = true
        private const val MSG_START_RECORDING = 0
        private const val MSG_STOP_RECORDING = 1
        private const val MSG_FRAME_AVAILABLE = 2
        private const val MSG_QUIT = 3
    }
}
