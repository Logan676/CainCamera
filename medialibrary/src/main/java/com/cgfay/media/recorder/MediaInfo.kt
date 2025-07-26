package com.cgfay.media.recorder

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.cgfay.uitls.utils.StringUtils

/**
 * Holds information about a recorded media segment.
 */
data class MediaInfo(
    val fileName: String,
    val duration: Long
)

/**
 * Displays [MediaInfo] using Jetpack Compose.
 */
@Composable
fun MediaInfoView(info: MediaInfo, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(text = info.fileName)
        Text(text = StringUtils.generateStandardTime(info.duration))
    }
}
