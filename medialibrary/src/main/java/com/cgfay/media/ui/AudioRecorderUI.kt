package com.cgfay.media.ui

import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.cgfay.media.recorder.AudioRecorder

/**
 * Simple Compose button that controls audio recording.
 */
@Composable
fun AudioRecorderButton(
    recorder: AudioRecorder,
    modifier: Modifier = Modifier
) {
    val isRecording = remember { mutableStateOf(false) }
    Button(
        onClick = {
            if (isRecording.value) {
                recorder.stopRecord()
            } else {
                recorder.startRecord()
            }
            isRecording.value = !isRecording.value
        },
        modifier = modifier
    ) {
        Text(if (isRecording.value) "Stop" else "Record")
    }
}
