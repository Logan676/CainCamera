package com.cgfay.caincamera.activity

import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.commit
import com.cgfay.caincamera.fragment.VideoPlayerFragment

class VideoPlayerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val path = intent.getStringExtra(PATH)
        setContent {
            VideoPlayerScreen(this, path)
        }
    }

    companion object {
        const val PATH = "PATH"
    }
}

private const val FRAGMENT_VIDEO_PLAYER = "fragment_video_player"

@Composable
fun VideoPlayerScreen(activity: FragmentActivity, path: String?) {
    val containerId = remember { View.generateViewId() }
    AndroidView(
        factory = { context ->
            FragmentContainerView(context).apply { id = containerId }
        },
        modifier = Modifier
    )

    LaunchedEffect(path) {
        val manager = activity.supportFragmentManager
        manager.findFragmentByTag(FRAGMENT_VIDEO_PLAYER)?.let { fragment ->
            manager.commit { remove(fragment) }
        }
        path?.let {
            manager.commit {
                replace(containerId, VideoPlayerFragment.newInstance(it), FRAGMENT_VIDEO_PLAYER)
            }
        }
    }
}
