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
import androidx.compose.ui.viewinterop.AndroidView
import android.widget.VideoView
import com.cgfay.picker.model.MediaData
import com.cgfay.picker.utils.MediaMetadataUtils
import com.cgfay.picker.widget.subsamplingview.ImageSource
import com.cgfay.picker.widget.subsamplingview.SubsamplingScaleImageView
import com.cgfay.scan.R

@Composable
fun MediaPreviewScreen(media: MediaData, onClose: () -> Unit) {
    val context = LocalContext.current
    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        if (media.isImage()) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    SubsamplingScaleImageView(ctx).apply {
                        val path = MediaMetadataUtils.getPath(context, media.contentUri)
                        if (path != null) {
                            setImage(ImageSource.uri(path))
                        } else {
                            setImage(ImageSource.uri(media.contentUri))
                        }
                    }
                }
            )
        } else {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    VideoView(ctx).apply {
                        setVideoPath(MediaMetadataUtils.getPath(context, media.contentUri))
                        setOnPreparedListener { seekTo(0); start() }
                    }
                }
            )
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
