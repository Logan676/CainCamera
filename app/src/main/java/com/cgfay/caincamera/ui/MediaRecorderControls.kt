package com.cgfay.caincamera.ui

import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.cgfay.media.recorder.FFMediaRecorder

/**
 * Simple start/stop buttons controlling [FFMediaRecorder].
 */
@Composable
fun MediaRecorderControls(
    recorder: FFMediaRecorder,
    modifier: Modifier = Modifier
) {
    var recording by remember { mutableStateOf(false) }
    Button(
        onClick = {
            if (recording) {
                recorder.stopRecord()
            } else {
                recorder.startRecord()
            }
            recording = !recording
        },
        modifier = modifier
    ) {
        Text(if (recording) "Stop" else "Start")
    }
}

