package com.cgfay.caincamera.activity

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.cgfay.caincamera.ui.MusicMergeNavGraph

/**
 * 视频音乐合成
 */
class MusicMergeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hideNavigationBar()
        val videoPath = intent.getStringExtra(PATH) ?: ""
        setContent {
            MusicMergeNavGraph(videoPath = videoPath) { finish() }
        }
    }

    private fun hideNavigationBar() {
        if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) {
            window.decorView.systemUiVisibility = View.GONE
        } else if (Build.VERSION.SDK_INT >= 19) {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
            val decorView = window.decorView
            val uiOptions = (View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_FULLSCREEN)
            decorView.systemUiVisibility = uiOptions
            decorView.setOnSystemUiVisibilityChangeListener { hideNavigationBar() }
        }
    }

    companion object {
        const val PATH = "video_path"
    }
}
