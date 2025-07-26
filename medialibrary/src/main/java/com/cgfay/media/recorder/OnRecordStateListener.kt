package com.cgfay.media.recorder

import androidx.compose.runtime.State
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf

/**
 * Listener for recording state updates. Includes a Compose aware implementation
 * that exposes state as [State] objects for use in UI.
 */
fun interface OnRecordStateListener {
    /** Called when recording starts */
    fun onRecordStart()

    /** Called periodically with current duration */
    fun onRecording(duration: Long)

    /** Called when recording finishes */
    fun onRecordFinish(info: RecordInfo)
}

/**
 * Simple Compose state holder implementing [OnRecordStateListener].
 */
@Stable
class RecordStateHolder : OnRecordStateListener {

    private val _isRecording = mutableStateOf(false)
    private val _duration = mutableStateOf(0L)
    private val _info = mutableStateOf<RecordInfo?>(null)

    /** Indicates whether recording is in progress. */
    val isRecording: State<Boolean> = _isRecording

    /** Current recording duration in microseconds. */
    val duration: State<Long> = _duration

    /** Information about the last completed recording. */
    val info: State<RecordInfo?> = _info

    override fun onRecordStart() {
        _isRecording.value = true
        _duration.value = 0L
        _info.value = null
    }

    override fun onRecording(duration: Long) {
        _duration.value = duration
    }

    override fun onRecordFinish(info: RecordInfo) {
        _isRecording.value = false
        _duration.value = info.duration
        _info.value = info
    }
}
