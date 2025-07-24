package com.cgfay.picker.compose

import androidx.compose.foundation.Image
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
import com.cgfay.scan.R

@Composable
fun MediaPreviewScreen(resId: Int?, onClose: () -> Unit) {
    if (resId == null) {
        LaunchedEffect(Unit) { onClose() }
        return
    }
    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        Image(
            painter = painterResource(id = resId),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )
        IconButton(onClick = onClose) {
            Icon(painterResource(id = R.drawable.ic_media_picker_close), contentDescription = null, tint = Color.White)
        }
    }
}
