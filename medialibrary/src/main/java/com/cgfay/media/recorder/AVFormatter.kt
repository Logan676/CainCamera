package com.cgfay.media.recorder

import android.media.AudioFormat
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.RadioButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Format definitions aligned with the native layer. This Kotlin version
 * provides a small Compose component for selecting audio sample formats.
 */
object AVFormatter {
    // Pixel formats
    const val PIXEL_FORMAT_NONE = 0
    const val PIXEL_FORMAT_NV21 = 1
    const val PIXEL_FORMAT_YV12 = 2
    const val PIXEL_FORMAT_NV12 = 3
    const val PIXEL_FORMAT_YUV420P = 4
    const val PIXEL_FORMAT_YUV420SP = 5
    const val PIXEL_FORMAT_ARGB = 6
    const val PIXEL_FORMAT_ABGR = 7
    const val PIXEL_FORMAT_RGBA = 8

    // Sample formats
    const val SAMPLE_FORMAT_NONE = 0
    const val SAMPLE_FORMAT_8BIT = 8
    const val SAMPLE_FORMAT_16BIT = 16
    const val SAMPLE_FORMAT_FLOAT = 32

    /**
     * Translate Android's [AudioFormat] value to a sample format constant.
     */
    fun getSampleFormat(audioFormat: Int): Int = when (audioFormat) {
        AudioFormat.ENCODING_PCM_8BIT -> SAMPLE_FORMAT_8BIT
        AudioFormat.ENCODING_PCM_16BIT -> SAMPLE_FORMAT_16BIT
        AudioFormat.ENCODING_PCM_FLOAT -> SAMPLE_FORMAT_FLOAT
        else -> SAMPLE_FORMAT_NONE
    }

    /**
     * Jetpack Compose selector allowing users to choose an audio sample format.
     *
     * @param selected current selected format state
     * @param modifier optional [Modifier]
     * @param onFormatSelected callback when a format is chosen
     */
    @Composable
    fun SampleFormatSelector(
        selected: MutableState<Int> = remember { mutableStateOf(SAMPLE_FORMAT_16BIT) },
        modifier: Modifier = Modifier,
        onFormatSelected: (Int) -> Unit = {},
    ) {
        val formats = listOf(SAMPLE_FORMAT_8BIT, SAMPLE_FORMAT_16BIT, SAMPLE_FORMAT_FLOAT)
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
        SAMPLE_FORMAT_8BIT -> "8-bit PCM"
        SAMPLE_FORMAT_16BIT -> "16-bit PCM"
        SAMPLE_FORMAT_FLOAT -> "Float PCM"
        else -> "Unknown"
    }
}

