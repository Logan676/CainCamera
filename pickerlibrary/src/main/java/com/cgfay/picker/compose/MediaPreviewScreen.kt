package com.cgfay.picker.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import android.net.Uri
import com.cgfay.picker.model.MediaData
import com.cgfay.picker.utils.MediaMetadataUtils
import com.cgfay.picker.widget.VideoPlayer
import com.cgfay.picker.widget.ZoomableImage
import com.cgfay.scan.R

@Composable
fun MediaPreviewScreen(media: MediaData, onClose: () -> Unit) {
    val context = LocalContext.current
    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        if (media.isImage()) {
            val path = MediaMetadataUtils.getPath(context, media.contentUri)
            val uri = path?.let { Uri.parse(it) } ?: media.contentUri
            ZoomableImage(uri = uri, modifier = Modifier.fillMaxSize())
        } else {
            val path = MediaMetadataUtils.getPath(context, media.contentUri)
            if (path != null) {
                VideoPlayer(uri = Uri.parse(path), modifier = Modifier.fillMaxSize())
            }
        }
        IconButton(onClick = onClose) {
            Icon(
                painterResource(id = R.drawable.ic_media_picker_close),
                contentDescription = null,
                tint = Color.White
            )
        }
    }
}
