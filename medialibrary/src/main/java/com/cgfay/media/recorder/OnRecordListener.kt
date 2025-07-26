package com.cgfay.media.recorder

import androidx.compose.runtime.Stable

/**
 * Internal listener used by MediaRecorder to report recording state.
 */
@Stable
internal interface OnRecordListener {
    /** Called when recording starts. */
    fun onRecordStart(type: MediaType)

    /** Called periodically with the current duration. */
    fun onRecording(type: MediaType, duration: Long)

    /** Called when recording is finished. */
    fun onRecordFinish(info: RecordInfo)
}
