package com.cgfay.image.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import com.cgfay.uitls.utils.BitmapUtils

class ImagePreviewActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val path = intent.getStringExtra(PATH)
        setContent {
            ImagePreviewScreen(path)
        }
    }

    companion object {
        const val PATH = "PATH"
    }
}

@Composable
fun ImagePreviewScreen(path: String?) {
    val bitmap = remember(path) { path?.let { BitmapUtils.getBitmapFromFile(it) } }
    Column {
        Text(text = "\u56fe\u7247\u8def\u5f84\uff1a" + (path ?: ""))
        bitmap?.let {
            Image(
                bitmap = it.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.fillMaxWidth(),
                contentScale = ContentScale.Crop
            )
        }
    }
}
