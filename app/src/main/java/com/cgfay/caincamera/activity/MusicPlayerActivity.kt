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
import com.cgfay.caincamera.fragment.MusicPlayerFragment
import com.cgfay.uitls.bean.MusicData
import com.cgfay.uitls.fragment.MusicPickerFragment

class MusicPlayerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MusicPlayerScreen(this) }
    }
}

private const val FRAGMENT_MUSIC_PLAYER = "fragment_music_player"

@Composable
fun MusicPlayerScreen(activity: FragmentActivity) {
    val containerId = remember { View.generateViewId() }
    AndroidView(
        factory = { context -> FragmentContainerView(context).apply { id = containerId } },
        modifier = Modifier
    )

    LaunchedEffect(Unit) {
        if (activity.supportFragmentManager.findFragmentByTag(FRAGMENT_MUSIC_PLAYER) == null) {
            val fragment = MusicPickerFragment().apply {
                addOnMusicSelectedListener(object : MusicPickerFragment.OnMusicSelectedListener {
                    override fun onMusicSelectClose() {
                        activity.finish()
                    }

                    override fun onMusicSelected(musicData: MusicData) {
                        activity.supportFragmentManager.commit {
                            replace(
                                containerId,
                                MusicPlayerFragment.newInstance(musicData.path),
                                FRAGMENT_MUSIC_PLAYER
                            )
                        }
                    }
                })
            }
            activity.supportFragmentManager.commit {
                replace(containerId, fragment, FRAGMENT_MUSIC_PLAYER)
            }
        }
    }
}
