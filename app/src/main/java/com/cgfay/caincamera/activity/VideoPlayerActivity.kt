package com.cgfay.caincamera.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.cgfay.caincamera.ui.VideoPlayerScreen

class VideoPlayerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val path = intent.getStringExtra(PATH)
        setContent {
            path?.let { VideoPlayerScreen(it) }
        }
    }

    companion object {
        const val PATH = "PATH"
    }
}
