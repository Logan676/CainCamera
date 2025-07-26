package com.cgfay.media.recorder


/**
 * Internal listener used by MediaRecorder to report recording state.
 */
internal interface OnRecordListener {
internal interface OnRecordListener {
    /** Called when recording starts. */
    fun onRecordStart(type: MediaType)

    /** Called periodically with the current duration. */
    fun onRecording(type: MediaType, duration: Long)

    /** Called when recording is finished. */
    fun onRecordFinish(info: RecordInfo)
}
