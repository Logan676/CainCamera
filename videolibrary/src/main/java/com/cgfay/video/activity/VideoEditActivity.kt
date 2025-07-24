package com.cgfay.video.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.cgfay.video.compose.VideoEditNavGraph

class VideoEditActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val videoPath = intent.getStringExtra(VIDEO_PATH)
        setContent { VideoEditNavGraph(videoPath) { finish() } }
    }

    companion object {
        const val VIDEO_PATH = "videoPath"
    }
}
