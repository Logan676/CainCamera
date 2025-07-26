package com.cgfay.media

import androidx.annotation.NonNull
import com.cgfay.uitls.utils.FileUtils
import com.cgfay.uitls.utils.NativeLibraryLoader
import java.io.Closeable

/**
 * Media editing helper using native implementation.
 */
class CainMediaEditor : Closeable {

    companion object {
        private const val TAG = "CainMediaEditor"

        init {
            NativeLibraryLoader.loadLibraries(
                "ffmpeg",
                "soundtouch",
                "yuv",
                "media_editor"
            )
        }
    }

    private external fun nativeInit(): Long
    private external fun nativeRelease(handle: Long)
    private external fun videoCut(handle: Long, srcPath: String, dstPath: String, start: Float, duration: Float, speed: Float, listener: OnEditProcessListener?)
    private external fun audioCut(handle: Long, srcPath: String, dstPath: String, start: Float, duration: Float, speed: Float, listener: OnEditProcessListener?)
    private external fun videoReverse(handle: Long, srcPath: String, dstPath: String, listener: OnEditProcessListener?)

    private var handle: Long = nativeInit()

    /** Release native resources */
    fun release() {
        if (handle != 0L) {
            nativeRelease(handle)
            handle = 0
        }
    }

    override fun close() {
        release()
    }

    /**
     * Cut video from [start] with [duration].
     */
    fun videoCut(srcPath: String, dstPath: String, start: Float, duration: Float, listener: OnEditProcessListener?) {
        if (FileUtils.fileExists(srcPath)) {
            videoCut(handle, srcPath, dstPath, start, duration, 1.0f, listener)
        } else {
            listener?.onError("source path is not exists.")
        }
    }

    /** Cut video with speed. */
    fun videoSpeedCut(srcPath: String, dstPath: String, start: Float, duration: Float, speed: Float, listener: OnEditProcessListener?) {
        if (FileUtils.fileExists(srcPath)) {
            videoCut(handle, srcPath, dstPath, start, duration, speed, listener)
        } else {
            listener?.onError("source path is not exists.")
        }
    }

    /** Cut audio from [start] with [duration]. */
    fun audioCut(srcPath: String, dstPath: String, start: Float, duration: Float, listener: OnEditProcessListener?) {
        if (FileUtils.fileExists(srcPath)) {
            audioCut(handle, srcPath, dstPath, start, duration, 1.0f, listener)
        } else {
            listener?.onError("source path is not exists.")
        }
    }

    /** Cut audio with speed. */
    fun audioSpeedCut(srcPath: String, dstPath: String, start: Float, duration: Float, speed: Float, listener: OnEditProcessListener?) {
        if (FileUtils.fileExists(srcPath)) {
            audioCut(handle, srcPath, dstPath, start, duration, speed, listener)
        } else {
            listener?.onError("source path is not exists.")
        }
    }

    /** Reverse video. */
    fun videoReverse(srcPath: String, dstPath: String, listener: OnEditProcessListener?) {
        if (FileUtils.fileExists(srcPath)) {
            videoReverse(handle, srcPath, dstPath, listener)
        } else {
            listener?.onError("source path is not exists.")
        }
    }

    interface OnEditProcessListener {
        fun onProcessing(percent: Int)
        fun onSuccess()
        fun onError(msg: String)
    }
}
