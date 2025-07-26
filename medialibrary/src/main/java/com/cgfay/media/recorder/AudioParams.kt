package com.cgfay.media.recorder

import android.media.AudioFormat

/**
 * Parameters used for audio recording.
 */
class AudioParams @JvmOverloads constructor(
    var sampleRate: Int = SAMPLE_RATE,
    var channel: Int = AudioFormat.CHANNEL_IN_STEREO,
    var bitRate: Int = BIT_RATE,
    var audioFormat: Int = AudioFormat.ENCODING_PCM_16BIT,
    var speedMode: SpeedMode = SpeedMode.MODE_NORMAL,
    var audioPath: String? = null,
    var maxDuration: Long = 0L
) {
    companion object {
        const val MIME_TYPE = "audio/mp4a-latm"
        const val SAMPLE_RATE = 44100
        const val BIT_RATE = 128000
    }
}

