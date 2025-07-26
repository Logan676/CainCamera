package com.cgfay.media.recorder

import android.util.Log
import com.cgfay.uitls.utils.NativeLibraryLoader

/**
 * Kotlin version of FFMediaRecorder that uses native ffmpeg based recorder.
 */
class FFMediaRecorder private constructor() : AutoCloseable {

    private var handle: Long = nativeInit()
    private var dstPath: String? = null

    companion object {
        private const val TAG = "FFMediaRecorder"

        init {
            NativeLibraryLoader.loadLibraries(
                "ffmpeg",
                "soundtouch",
                "yuv",
                "ffrecorder"
            )
        }
    }

    override fun close() {
        if (handle != 0L) {
            nativeRelease(handle)
            handle = 0L
        }
    }

    @Suppress("deprecation")
    protected fun finalize() {
        close()
    }

    // native methods
    private external fun nativeInit(): Long
    private external fun nativeRelease(handle: Long)
    private external fun setRecordListener(handle: Long, listener: Any?)
    private external fun setOutput(handle: Long, dstPath: String)
    private external fun setAudioEncoder(handle: Long, encoder: String)
    private external fun setVideoEncoder(handle: Long, encoder: String)
    private external fun setAudioFilter(handle: Long, filter: String)
    private external fun setVideoFilter(handle: Long, filter: String)
    private external fun setVideoRotate(handle: Long, rotate: Int)
    private external fun setMirror(handle: Long, mirror: Boolean)
    private external fun setVideoParams(
        handle: Long,
        width: Int,
        height: Int,
        frameRate: Int,
        pixelFormat: Int,
        maxBitRate: Long,
        quality: Int
    )
    private external fun setAudioParams(
        handle: Long,
        sampleRate: Int,
        sampleFormat: Int,
        channels: Int
    )
    private external fun recordVideoFrame(
        handle: Long,
        data: ByteArray,
        length: Int,
        width: Int,
        height: Int,
        pixelFormat: Int
    ): Int
    private external fun recordAudioFrame(handle: Long, data: ByteArray, length: Int): Int
    private external fun startRecord(handle: Long)
    private external fun stopRecord(handle: Long)

    fun setRecordListener(listener: OnRecordListener?) {
        setRecordListener(handle, listener)
    }

    fun setOutput(dstPath: String) {
        this.dstPath = dstPath
        setOutput(handle, dstPath)
    }

    val output: String?
        get() = dstPath

    fun setAudioEncoder(encoder: String) { setAudioEncoder(handle, encoder) }
    fun setVideoEncoder(encoder: String) { setVideoEncoder(handle, encoder) }
    fun setAudioFilter(filter: String) { setAudioFilter(handle, filter) }
    fun setVideoFilter(filter: String) { setVideoFilter(handle, filter) }
    fun setVideoRotate(rotate: Int) { setVideoRotate(handle, rotate) }
    fun setMirror(mirror: Boolean) { setMirror(handle, mirror) }

    fun setVideoParams(
        width: Int,
        height: Int,
        frameRate: Int,
        pixelFormat: Int,
        maxBitRate: Long,
        quality: Int
    ) {
        setVideoParams(handle, width, height, frameRate, pixelFormat, maxBitRate, quality)
    }

    fun setAudioParams(sampleRate: Int, sampleFormat: Int, channels: Int) {
        setAudioParams(handle, sampleRate, sampleFormat, channels)
    }

    fun recordVideoFrame(data: ByteArray, length: Int, width: Int, height: Int, pixelFormat: Int) {
        recordVideoFrame(handle, data, length, width, height, pixelFormat)
    }

    fun recordAudioFrame(data: ByteArray, length: Int) {
        recordAudioFrame(handle, data, length)
    }

    fun startRecord() { startRecord(handle) }

    fun stopRecord() { stopRecord(handle) }

    class RecordBuilder(private val dstPath: String) {
        private var width = -1
        private var height = -1
        private var rotate = 0
        private var mirror = false
        private var pixelFormat = -1
        private var frameRate = -1
        private var maxBitRate = -1L
        private var quality = 23
        private var videoEncoder: String? = null
        private var videoFilter: String = "null"

        private var sampleRate = -1
        private var sampleFormat = -1
        private var channels = -1
        private var audioEncoder: String? = null
        private var audioFilter: String = "anull"

        fun setVideoParams(width: Int, height: Int, pixelFormat: Int, frameRate: Int) = apply {
            this.width = width
            this.height = height
            this.pixelFormat = pixelFormat
            this.frameRate = frameRate
        }

        fun setAudioParams(sampleRate: Int, sampleFormat: Int, channels: Int) = apply {
            this.sampleRate = sampleRate
            this.sampleFormat = sampleFormat
            this.channels = channels
        }

        fun setVideoFilter(videoFilter: String) = apply {
            if (videoFilter.isNotBlank()) this.videoFilter = videoFilter
        }

        fun setAudioFilter(audioFilter: String) = apply {
            if (audioFilter.isNotBlank()) this.audioFilter = audioFilter
        }

        fun setRotate(rotate: Int) = apply { this.rotate = rotate }
        fun setMirror(mirror: Boolean) = apply { this.mirror = mirror }
        fun setMaxBitRate(maxBitRate: Long) = apply { this.maxBitRate = maxBitRate }
        fun setQuality(quality: Int) = apply { this.quality = quality }
        fun setVideoEncoder(encoder: String) = apply { this.videoEncoder = encoder }
        fun setAudioEncoder(encoder: String) = apply { this.audioEncoder = encoder }

        fun create(): FFMediaRecorder {
            val recorder = FFMediaRecorder()
            recorder.setOutput(dstPath)
            recorder.setVideoParams(width, height, frameRate, pixelFormat, maxBitRate, quality)
            recorder.setVideoRotate(rotate)
            recorder.setMirror(mirror)
            recorder.setAudioParams(sampleRate, sampleFormat, channels)
            videoEncoder?.let { recorder.setVideoEncoder(it) }
            audioEncoder?.let { recorder.setAudioEncoder(it) }
            if (videoFilter != "null") recorder.setVideoFilter(videoFilter)
            if (audioFilter != "anull") recorder.setAudioFilter(audioFilter)
            return recorder
        }
    }

    interface OnRecordListener {
        fun onRecordStart()
        fun onRecording(duration: Float)
        fun onRecordFinish(success: Boolean, duration: Float)
        fun onRecordError(msg: String)
    }
}

