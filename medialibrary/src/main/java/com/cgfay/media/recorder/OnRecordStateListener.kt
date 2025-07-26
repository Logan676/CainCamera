package com.cgfay.media.recorder


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

