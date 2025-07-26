package com.cgfay.media.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cgfay.media.recorder.AudioParams

/**
 * Simple editor UI for modifying [AudioParams].
 */
@Composable
fun AudioParamsEditor(params: AudioParams, modifier: Modifier = Modifier) {
    var sampleRateText by remember { mutableStateOf(params.sampleRate.toString()) }
    var bitRateText by remember { mutableStateOf(params.bitRate.toString()) }

    Column(modifier = modifier.padding(16.dp)) {
        OutlinedTextField(
            value = sampleRateText,
            onValueChange = {
                sampleRateText = it
                params.sampleRate = it.toIntOrNull() ?: params.sampleRate
            },
            label = { Text("Sample Rate") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = bitRateText,
            onValueChange = {
                bitRateText = it
                params.bitRate = it.toIntOrNull() ?: params.bitRate
            },
            label = { Text("Bit Rate") },
            modifier = Modifier.fillMaxWidth()
        )
    }
}
