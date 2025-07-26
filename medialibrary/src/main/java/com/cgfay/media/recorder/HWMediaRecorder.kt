package com.cgfay.media.recorder

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

/**
 * Media recorder supporting variable speed recording.
 */
class HWMediaRecorder(
    private val recordStateListener: OnRecordStateListener
) : OnRecordListener {

    companion object {
        private const val TAG = "FFMediaRecorder"
        const val SECOND_IN_US = 1_000_000
        private const val VERBOSE = false
    }

    private val audioRecorder = AudioRecorder().apply { setOnRecordListener(this@HWMediaRecorder) }
    private val videoRecorder = VideoRecorder().apply { setOnRecordListener(this@HWMediaRecorder) }

    private var audioEnable = true
    private var recorderCount = 0
    private var processTime = 0L

    var isRecording by mutableStateOf(false)
        private set

    fun release() {
        videoRecorder.release()
        audioRecorder.release()
    }

    fun setEnableAudio(enable: Boolean) { audioEnable = enable }

    fun enableAudio(): Boolean = audioEnable

    fun startRecord(videoParams: VideoParams, audioParams: AudioParams) {
        if (VERBOSE) Log.d(TAG, "start record")
        videoRecorder.startRecord(videoParams)
        if (audioEnable) {
            try {
                audioRecorder.prepare(audioParams)
                audioRecorder.startRecord()
            } catch (e: Exception) {
                Log.e(TAG, "startRecord: ${e.message}")
            }
        }
    }

    fun stopRecord() {
        if (VERBOSE) Log.d(TAG, "stop recording")
        val time = System.currentTimeMillis()
        videoRecorder.stopRecord()
        if (audioEnable) {
            audioRecorder.stopRecord()
        }
        if (VERBOSE) {
            processTime += System.currentTimeMillis() - time
            Log.d(TAG, "sum of init and release time: $processTime ms")
            processTime = 0
        }
    }

    fun frameAvailable(texture: Int, timestamp: Long) {
        videoRecorder.frameAvailable(texture, timestamp)
    }

    override fun onRecordStart(type: MediaType) {
        recorderCount++
        if (!audioEnable || recorderCount >= 2) {
            recordStateListener.onRecordStart()
            recorderCount = 0
        }
        isRecording = true
    }

    override fun onRecording(type: MediaType, duration: Long) {
        if (type == MediaType.VIDEO) {
            recordStateListener.onRecording(duration)
        }
    }

    override fun onRecordFinish(info: RecordInfo) {
        recordStateListener.onRecordFinish(info)
        isRecording = false
    }
}

/**
 * Compose helper used to remember [HWMediaRecorder] instance.
 */
@Composable
fun rememberHWMediaRecorder(listener: OnRecordStateListener): HWMediaRecorder =
    remember(listener) { HWMediaRecorder(listener) }
