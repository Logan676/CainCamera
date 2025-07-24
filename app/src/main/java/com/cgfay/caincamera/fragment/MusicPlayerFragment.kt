package com.cgfay.caincamera.fragment

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cgfay.media.MusicPlayer
import java.io.IOException

class MusicPlayerFragment : Fragment() {

    companion object {
        private const val PATH = "PATH"
        fun newInstance(path: String): MusicPlayerFragment {
            val fragment = MusicPlayerFragment()
            fragment.arguments = Bundle().apply { putString(PATH, path) }
            return fragment
        }
    }

    private var musicPlayer: MusicPlayer? = null
    private var duration: Float = 0f
    private val progressState = mutableStateOf(0f)
    private val speedState = mutableStateOf(50f)
    private var playing by mutableStateOf(false)
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreateView(
        inflater: android.view.LayoutInflater,
        container: android.view.ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val path = requireArguments().getString(PATH) ?: ""
        initPlayer(path)
        return ComposeView(requireContext()).apply {
            setContent {
                MusicPlayerScreen(
                    path = path,
                    isPlaying = playing,
                    progress = progressState.value,
                    speedProgress = speedState.value,
                    onPlayPause = { togglePlayPause() },
                    onProgressChange = { seek ->
                        progressState.value = seek
                        musicPlayer?.let { mp ->
                            if (duration > 0) mp.seekTo(seek / 100f * duration)
                        }
                    },
                    onSpeedChange = { sp ->
                        speedState.value = sp
                        val speed = if (sp > 50f) 1.0f + (sp - 50f) / 50f else 0.5f + sp / 100f
                        musicPlayer?.setSpeed(speed)
                    }
                )
            }
        }
    }

    override fun onStart() {
        super.onStart()
        musicPlayer?.start()
        playing = true
    }

    override fun onResume() {
        super.onResume()
        musicPlayer?.start()
        playing = true
    }

    override fun onPause() {
        musicPlayer?.pause()
        playing = false
        super.onPause()
    }

    override fun onStop() {
        musicPlayer?.pause()
        playing = false
        super.onStop()
    }

    override fun onDestroy() {
        musicPlayer?.release()
        musicPlayer = null
        super.onDestroy()
    }

    private fun togglePlayPause() {
        musicPlayer?.let { mp ->
            if (mp.isPlaying) {
                mp.pause()
                playing = false
            } else {
                mp.start()
                playing = true
            }
        }
    }

    private fun initPlayer(path: String) {
        musicPlayer = MusicPlayer().apply {
            setSpeed(1.0f)
            setLooping(true)
            setOnPreparedListener { mp ->
                duration = mp.duration
            }
            setOnCurrentPositionListener { _, current, dur ->
                handler.post {
                    progressState.value = if (dur > 0f) current / dur * 100f else 0f
                }
            }
            try {
                setDataSource(path)
                prepare()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}

@Composable
private fun MusicPlayerScreen(
    path: String,
    isPlaying: Boolean,
    progress: Float,
    speedProgress: Float,
    onPlayPause: () -> Unit,
    onProgressChange: (Float) -> Unit,
    onSpeedChange: (Float) -> Unit
) {
    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "progress", modifier = Modifier.padding(end = 8.dp))
            Slider(value = progress, onValueChange = onProgressChange)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "speed", modifier = Modifier.padding(end = 8.dp))
            Slider(value = speedProgress, onValueChange = onSpeedChange)
        }
        Button(onClick = onPlayPause, modifier = Modifier.padding(top = 16.dp)) {
            Text(if (isPlaying) "pause" else "play")
        }
        Text(text = path, modifier = Modifier.padding(top = 16.dp))
    }
}

