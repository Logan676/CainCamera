package com.cgfay.media.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.cgfay.media.recorder.AudioEncoder

@Composable
fun rememberAudioEncoder(
    bitrate: Int,
    sampleRate: Int,
    channelCount: Int,
    outputPath: String
): AudioEncoder = remember(bitrate, sampleRate, channelCount, outputPath) {
    AudioEncoder(bitrate, sampleRate, channelCount).apply { setOutputPath(outputPath) }
}
