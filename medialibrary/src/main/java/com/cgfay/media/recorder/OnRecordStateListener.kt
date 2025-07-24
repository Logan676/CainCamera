package com.cgfay.media.recorder

fun interface OnRecordStateListener {
    /** Called when recording starts */
    fun onRecordStart()

    /** Called periodically with current duration */
    fun onRecording(duration: Long)

    /** Called when recording finishes */
    fun onRecordFinish(info: RecordInfo)
}
