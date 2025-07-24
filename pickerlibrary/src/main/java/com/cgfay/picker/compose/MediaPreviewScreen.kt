package com.cgfay.picker.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.cgfay.picker.model.MediaData
import com.cgfay.scan.R
import coil.compose.AsyncImage

@Composable
fun MediaPreviewScreen(media: MediaData?, onClose: () -> Unit) {
    if (media == null) {
        LaunchedEffect(Unit) { onClose() }
        return
    }
    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        AsyncImage(
            model = media.contentUri,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )
        IconButton(onClick = onClose) {
            Icon(painterResource(id = R.drawable.ic_media_picker_close), contentDescription = null, tint = Color.White)
        }
    }
}
