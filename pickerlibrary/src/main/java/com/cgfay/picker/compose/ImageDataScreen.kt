package com.cgfay.picker.compose

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.loader.app.LoaderManager
import com.cgfay.picker.model.MediaData
import com.cgfay.picker.scanner.ImageDataScanner

@Composable
fun ImageDataScreen(
    columns: Int = 3,
    onMediaClick: (MediaData) -> Unit = {}
) {
    val activity = LocalContext.current as ComponentActivity
    MediaDataScreen(columns, { receiver ->
        ImageDataScanner(activity, LoaderManager.getInstance(activity), receiver)
    }, onMediaClick)
}
