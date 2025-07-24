package com.cgfay.video.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.cgfay.video.compose.VideoCutNavGraph

class VideoCutActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val videoPath = intent.getStringExtra(PATH)
        setContent { VideoCutNavGraph(videoPath) { finish() } }
    }

    companion object {
        const val PATH = "path"
    }
}
