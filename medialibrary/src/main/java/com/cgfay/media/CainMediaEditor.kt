package com.cgfay.media

import androidx.annotation.NonNull
import com.cgfay.uitls.utils.FileUtils
import com.cgfay.uitls.utils.NativeLibraryLoader
import java.io.Closeable

/**
 * 媒体编辑器
 */
class CainMediaEditor : Closeable {

    private external fun nativeInit(): Long
    private external fun nativeRelease(handle: Long)
    private external fun videoCut(handle: Long, srcPath: String, dstPath: String, start: Float, duration: Float, speed: Float, listener: OnEditProcessListener?)
    private external fun audioCut(handle: Long, srcPath: String, dstPath: String, start: Float, duration: Float, speed: Float, listener: OnEditProcessListener?)
    private external fun videoReverse(handle: Long, srcPath: String, dstPath: String, listener: OnEditProcessListener?)

    private var handle: Long = 0

    init {
        NativeLibraryLoader.loadLibraries(
            "ffmpeg",
            "soundtouch",
            "yuv",
            "media_editor"
        )
        handle = nativeInit()
    }

    /** 释放资源 */
    fun release() {
        if (handle != 0L) {
            nativeRelease(handle)
            handle = 0
        }
    }

    override fun close() {
        release()
    }

    /** 视频裁剪 */
    fun videoCut(@NonNull srcPath: String, @NonNull dstPath: String, start: Float, duration: Float, listener: OnEditProcessListener?) {
        if (FileUtils.fileExists(srcPath)) {
            videoCut(handle, srcPath, dstPath, start, duration, 1.0f, listener)
        } else {
            listener?.onError("source path is not exists.")
        }
    }

    /** 视频裁剪，带倍速调整 */
    fun videoSpeedCut(@NonNull srcPath: String, @NonNull dstPath: String, start: Float, duration: Float, speed: Float, listener: OnEditProcessListener?) {
        if (FileUtils.fileExists(srcPath)) {
            videoCut(handle, srcPath, dstPath, start, duration, speed, listener)
        } else {
            listener?.onError("source path is not exists.")
        }
    }

    /** 音频裁剪 */
    fun audioCut(@NonNull srcPath: String, @NonNull dstPath: String, start: Float, duration: Float, listener: OnEditProcessListener?) {
        if (FileUtils.fileExists(srcPath)) {
            audioCut(handle, srcPath, dstPath, start, duration, 1.0f, listener)
        } else {
            listener?.onError("source path is not exists.")
        }
    }

    /** 音频裁剪，带倍速处理 */
    fun audioSpeedCut(@NonNull srcPath: String, @NonNull dstPath: String, start: Float, duration: Float, speed: Float, listener: OnEditProcessListener?) {
        if (FileUtils.fileExists(srcPath)) {
            audioCut(handle, srcPath, dstPath, start, duration, speed, listener)
        } else {
            listener?.onError("source path is not exists.")
        }
    }

    /** TODO 视频逆序处理，不支持存在B帧的视频进行逆序 */
    fun videoReverse(@NonNull srcPath: String, @NonNull dstPath: String, listener: OnEditProcessListener?) {
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
