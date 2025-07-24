package com.cgfay.video.activity

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
import com.cgfay.video.fragment.VideoCutFragment

class VideoCutActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val videoPath = intent.getStringExtra(PATH)
        setContent { VideoCutScreen(this, videoPath) }
    }

    companion object {
        const val PATH = "path"
    }
}

private const val FRAGMENT_VIDEO_CROP = "fragment_video_cut"

@Composable
fun VideoCutScreen(activity: FragmentActivity, videoPath: String?) {
    val containerId = remember { View.generateViewId() }
    AndroidView(factory = { context -> FragmentContainerView(context).apply { id = containerId } }, modifier = Modifier)

    LaunchedEffect(videoPath) {
        if (videoPath.isNullOrEmpty()) {
            activity.finish()
        } else {
            if (activity.supportFragmentManager.findFragmentByTag(FRAGMENT_VIDEO_CROP) == null) {
                val fragment = VideoCutFragment.newInstance()
                fragment.setVideoPath(videoPath)
                activity.supportFragmentManager.commit {
                    replace(containerId, fragment, FRAGMENT_VIDEO_CROP)
                }
            }
        }
    }
}
