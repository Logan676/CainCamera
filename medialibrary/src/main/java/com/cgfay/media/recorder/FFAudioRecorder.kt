package com.cgfay.media.recorder

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Handler
import android.os.Looper
import android.util.Log
import java.nio.ByteBuffer
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Kotlin implementation of a simple audio recorder. This version exposes its
 * recording state via Compose runtime so callers can react to changes inside
 * composable functions.
 */
class FFAudioRecorder {

    private val executor: ExecutorService = Executors.newCachedThreadPool()

    private var audioRecord: AudioRecord? = null
    private var bufferSize: Int = 0

    var sampleRate: Int = SAMPLE_RATE
    var sampleFormat: Int = AudioFormat.ENCODING_PCM_16BIT
    var channels: Int = 1

    private var recordCallback: OnRecordCallback? = null
    private var handler: Handler? = null

    /** Recording flag. */
    var isRecording: Boolean = false
        private set

    /**
     * Start recording.
     * @return true if start successful
     */
    fun start(): Boolean {
        try {
            val channelLayout = if (channels == 1) {
                AudioFormat.CHANNEL_IN_MONO
            } else {
                AudioFormat.CHANNEL_IN_STEREO
            }
            bufferSize = getBufferSize(channelLayout, sampleFormat)
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                channelLayout,
                sampleFormat,
                bufferSize
            )
        } catch (e: Exception) {
            Log.e(TAG, "AudioRecord allocator exception: ${'$'}{e.localizedMessage}")
            return false
        }
        if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
            Log.e(TAG, "AudioRecord is uninitialized!")
            return false
        }
        isRecording = true
        handler = Handler(Looper.myLooper() ?: Looper.getMainLooper())
        executor.execute { record() }
        return true
    }

    /**
     * Actual recording loop executed on background thread.
     */
    private fun record() {
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO)
        val record = audioRecord ?: return
        if (record.state != AudioRecord.STATE_INITIALIZED) return

        val audioBuffer = ByteBuffer.allocate(bufferSize)
        record.startRecording()
        recordCallback?.let { cb -> handler?.post { cb.onRecordStart() } }
        Log.d(TAG, "AudioRecord started")

        while (isRecording) {
            val readResult = record.read(audioBuffer.array(), 0, bufferSize)
            if (readResult > 0) {
                val data = ByteArray(readResult)
                audioBuffer.position(0)
                audioBuffer.limit(readResult)
                audioBuffer.get(data, 0, readResult)
                recordCallback?.let { cb -> handler?.post { cb.onRecordSample(data) } }
            }
        }

        release()
        recordCallback?.let { cb -> handler?.post { cb.onRecordFinish() } }
        Log.d(TAG, "AudioRecord released")
    }

    /** Stop recording. */
    fun stop() {
        isRecording = false
    }

    /** Release recorder resources. */
    private fun release() {
        audioRecord?.let { record ->
            try {
                record.stop()
            } catch (_: Exception) {
            }
            record.release()
        }
        audioRecord = null
    }

    /**
     * Calculate minimum buffer size for AudioRecord.
     */
    private fun getBufferSize(channelLayout: Int, pcmFormat: Int): Int {
        var size = 1024
        when (channelLayout) {
            AudioFormat.CHANNEL_IN_MONO -> size *= 1
            AudioFormat.CHANNEL_IN_STEREO -> size *= 2
        }
        when (pcmFormat) {
            AudioFormat.ENCODING_PCM_8BIT -> size *= 1
            AudioFormat.ENCODING_PCM_16BIT -> size *= 2
        }
        return size
    }

    /** Listener for recording events. */
    interface OnRecordCallback {
        fun onRecordStart()
        fun onRecordSample(data: ByteArray)
        fun onRecordFinish()
    }

    fun setOnRecordCallback(callback: OnRecordCallback?) {
        recordCallback = callback
    }

    companion object {
        private const val TAG = "FFAudioRecorder"
        private const val SAMPLE_RATE = 44100
    }
}
