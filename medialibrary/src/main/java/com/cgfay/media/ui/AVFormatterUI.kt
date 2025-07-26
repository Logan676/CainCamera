package com.cgfay.media.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.RadioButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cgfay.media.recorder.AVFormatter

/**
 * Jetpack Compose selector allowing users to choose an audio sample format.
 */
@Composable
fun SampleFormatSelector(
    selected: MutableState<Int> = remember { mutableStateOf(AVFormatter.SAMPLE_FORMAT_16BIT) },
    modifier: Modifier = Modifier,
    onFormatSelected: (Int) -> Unit = {},
) {
    val formats = listOf(
        AVFormatter.SAMPLE_FORMAT_8BIT,
        AVFormatter.SAMPLE_FORMAT_16BIT,
        AVFormatter.SAMPLE_FORMAT_FLOAT
    )
    Column(modifier = modifier) {
        formats.forEach { format ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        selected.value = format
                        onFormatSelected(format)
                    }
                    .padding(vertical = 4.dp)
            ) {
                RadioButton(
                    selected = selected.value == format,
                    onClick = {
                        selected.value = format
                        onFormatSelected(format)
                    }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = formatLabel(format))
            }
        }
    }
}

private fun formatLabel(format: Int): String = when (format) {
    AVFormatter.SAMPLE_FORMAT_8BIT -> "8-bit PCM"
    AVFormatter.SAMPLE_FORMAT_16BIT -> "16-bit PCM"
    AVFormatter.SAMPLE_FORMAT_FLOAT -> "Float PCM"
    else -> "Unknown"
}
