package com.cgfay.video.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.cgfay.video.ui.MultiVideoScreen

class MultiVideoActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val pathList = intent.getStringArrayListExtra(PATH)
        setContent {
            MultiVideoScreen(pathList)
        }
    }

    companion object {
        const val PATH = "path"
    }
}
