package com.cgfay.caincamera.activity

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
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
import com.cgfay.caincamera.fragment.MusicMergeFragment
import com.cgfay.uitls.bean.MusicData
import com.cgfay.uitls.fragment.MusicPickerFragment

/**
 * 视频音乐合成
 */
class MusicMergeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hideNavigationBar()
        val videoPath = intent.getStringExtra(PATH)
        setContent {
            MusicMergeScreen(this, videoPath)
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

private const val FRAGMENT_MUSIC_MERGE = "fragment_music_merge"

@Composable
fun MusicMergeScreen(activity: FragmentActivity, videoPath: String?) {
    val containerId = remember { View.generateViewId() }
    AndroidView(
        factory = { context -> FragmentContainerView(context).apply { id = containerId } },
        modifier = Modifier
    )

    LaunchedEffect(Unit) {
        if (activity.supportFragmentManager.findFragmentByTag(FRAGMENT_MUSIC_MERGE) == null) {
            val picker = MusicPickerFragment().apply {
                addOnMusicSelectedListener(object : MusicPickerFragment.OnMusicSelectedListener {
                    override fun onMusicSelectClose() { activity.finish() }
                    override fun onMusicSelected(musicData: MusicData) {
                        val fragment = MusicMergeFragment.newInstance().apply {
                            setVideoPath(videoPath)
                            setMusicPath(musicData.path, musicData.duration)
                        }
                        activity.supportFragmentManager.commit {
                            replace(containerId, fragment, FRAGMENT_MUSIC_MERGE)
                        }
                    }
                })
            }
            activity.supportFragmentManager.commit { add(containerId, picker) }
        }
    }
}
