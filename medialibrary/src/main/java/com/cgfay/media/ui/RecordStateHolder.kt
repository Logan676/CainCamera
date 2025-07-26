package com.cgfay.media.ui

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.Composable
import com.cgfay.media.recorder.OnRecordStateListener
import com.cgfay.media.recorder.RecordInfo

/**
 * Simple Compose state holder implementing [OnRecordStateListener].
 */
class RecordStateHolder : OnRecordStateListener {

    private val _isRecording = mutableStateOf(false)
    private val _duration = mutableStateOf(0L)
    private val _info = mutableStateOf<RecordInfo?>(null)

    val isRecording: State<Boolean> = _isRecording
    val duration: State<Long> = _duration
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

/**
 * Compose helper to remember a [RecordStateHolder] instance.
 */
@Composable
fun rememberRecordStateHolder(): RecordStateHolder = remember { RecordStateHolder() }
