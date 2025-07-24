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
import com.cgfay.uitls.bean.MusicData
import com.cgfay.uitls.fragment.MusicPickerFragment
import com.cgfay.video.fragment.VideoEditFragment

class VideoEditActivity : ComponentActivity(),
    VideoEditFragment.OnSelectMusicListener,
    MusicPickerFragment.OnMusicSelectedListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val videoPath = intent.getStringExtra(VIDEO_PATH)
        setContent { VideoEditScreen(this, videoPath) }
    }

    override fun onBackPressed() {
        val count = supportFragmentManager.backStackEntryCount
        if (count == 1) {
            val fragment = supportFragmentManager.findFragmentByTag(FRAGMENT_VIDEO_EDIT) as? VideoEditFragment
            fragment?.onBackPressed()
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        val count = supportFragmentManager.backStackEntryCount
        if (count == 1) {
            val fragment = supportFragmentManager.findFragmentByTag(FRAGMENT_VIDEO_EDIT) as? VideoEditFragment
            fragment?.setOnSelectMusicListener(null)
        }
        super.onDestroy()
    }

    override fun onOpenMusicSelectPage() {
        val fragment = MusicPickerFragment()
        fragment.addOnMusicSelectedListener(this)
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(com.cgfay.utilslibrary.R.anim.anim_slide_up, 0)
            .add(android.R.id.content, fragment)
            .addToBackStack(FRAGMENT_MUSIC_SELECT)
            .commit()
    }

    override fun onMusicSelectClose() {
        supportFragmentManager.popBackStack(FRAGMENT_VIDEO_EDIT, 0)
    }

    override fun onMusicSelected(musicData: MusicData) {
        supportFragmentManager.popBackStack(FRAGMENT_VIDEO_EDIT, 0)
        val fragment = supportFragmentManager.findFragmentByTag(FRAGMENT_VIDEO_EDIT) as? VideoEditFragment
        fragment?.setSelectedMusic(musicData.path, musicData.duration)
    }

    companion object {
        const val VIDEO_PATH = "videoPath"
        private const val FRAGMENT_VIDEO_EDIT = "fragment_video_edit"
        private const val FRAGMENT_MUSIC_SELECT = "fragment_video_music_select"
    }
}

@Composable
fun VideoEditScreen(activity: FragmentActivity, videoPath: String?) {
    val containerId = remember { View.generateViewId() }
    AndroidView(factory = { context -> FragmentContainerView(context).apply { id = containerId } }, modifier = Modifier)

    LaunchedEffect(videoPath) {
        if (videoPath.isNullOrEmpty()) {
            activity.finish()
        } else {
            if (activity.supportFragmentManager.findFragmentByTag(VideoEditActivity.FRAGMENT_VIDEO_EDIT) == null) {
                val fragment = VideoEditFragment.newInstance()
                fragment.setOnSelectMusicListener(activity as VideoEditFragment.OnSelectMusicListener)
                fragment.setVideoPath(videoPath)
                activity.supportFragmentManager.commit {
                    replace(containerId, fragment, VideoEditActivity.FRAGMENT_VIDEO_EDIT)
                    addToBackStack(VideoEditActivity.FRAGMENT_VIDEO_EDIT)
                }
            }
        }
    }
}
