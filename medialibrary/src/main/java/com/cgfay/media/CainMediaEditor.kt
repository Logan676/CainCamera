package com.cgfay.media

import androidx.annotation.NonNull
import android.util.Log
import com.cgfay.uitls.utils.NativeLibraryLoader
import com.cgfay.uitls.utils.FileUtils
import java.io.Closeable

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

    // 初始化
    private external fun nativeInit(): Long
    // 释放资源
    private external fun nativeRelease(handle: Long)
    // 视频裁剪
    private external fun videoCut(handle: Long, srcPath: String, dstPath: String,
        start: Float, duration: Float, speed: Float, listener: OnEditProcessListener?)
    // 音频裁剪
    private external fun audioCut(handle: Long, srcPath: String, dstPath: String,
        start: Float, duration: Float, speed: Float, listener: OnEditProcessListener?)
    // 视频逆序
    private external fun videoReverse(handle: Long, srcPath: String, dstPath: String,
        listener: OnEditProcessListener?)

    private var handle: Long = nativeInit()

    /**
     * 释放资源
     */
    fun release() {
        if (handle != 0L) {
            nativeRelease(handle)
            handle = 0L
        }
    }

    override fun close() {
        release()
    }

    /**
     * 视频裁剪
     */
    fun videoCut(
        srcPath: String,
        dstPath: String,
        start: Float,
        duration: Float,
        listener: OnEditProcessListener?
    ) {
        if (FileUtils.fileExists(srcPath)) {
            videoCut(handle, srcPath, dstPath, start, duration, 1.0f, listener)
        } else {
            listener?.onError("source path is not exists.")
        }
    }

    /**
     * 视频裁剪，带倍速调整
     */
    fun videoSpeedCut(
        srcPath: String,
        dstPath: String,
        start: Float,
        duration: Float,
        speed: Float,
        listener: OnEditProcessListener?
    ) {
        if (FileUtils.fileExists(srcPath)) {
            videoCut(handle, srcPath, dstPath, start, duration, speed, listener)
        } else {
            listener?.onError("source path is not exists.")
        }
    }

    /**
     * 音频裁剪
     */
    fun audioCut(
        srcPath: String,
        dstPath: String,
        start: Float,
        duration: Float,
        listener: OnEditProcessListener?
    ) {
        if (FileUtils.fileExists(srcPath)) {
            audioCut(handle, srcPath, dstPath, start, duration, 1.0f, listener)
        } else {
            listener?.onError("source path is not exists.")
        }
    }

    /**
     * 音频裁剪，带倍速处理
     */
    fun audioSpeedCut(
        srcPath: String,
        dstPath: String,
        start: Float,
        duration: Float,
        speed: Float,
        listener: OnEditProcessListener?
    ) {
        if (FileUtils.fileExists(srcPath)) {
            audioCut(handle, srcPath, dstPath, start, duration, speed, listener)
        } else {
            listener?.onError("source path is not exists.")
        }
    }

    /**
     * TODO 视频逆序处理，不支持存在B帧的视频进行逆序
     */
    fun videoReverse(srcPath: String, dstPath: String, listener: OnEditProcessListener?) {
        if (FileUtils.fileExists(srcPath)) {
            videoReverse(handle, srcPath, dstPath, listener)
        } else {
            listener?.onError("source path is not exists.")
        }
    }

    /**
     * 编辑处理监听器
     */
    interface OnEditProcessListener {
        fun onProcessing(percent: Int)
        fun onSuccess()
        fun onError(msg: String)
    }
}
