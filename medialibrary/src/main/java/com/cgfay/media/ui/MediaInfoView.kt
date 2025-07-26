package com.cgfay.media.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.cgfay.media.recorder.MediaInfo
import com.cgfay.uitls.utils.StringUtils

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
