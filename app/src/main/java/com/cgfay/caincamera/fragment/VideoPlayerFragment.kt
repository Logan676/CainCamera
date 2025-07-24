package com.cgfay.caincamera.fragment

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import com.cgfay.media.VideoPlayer
import com.cgfay.uitls.utils.DisplayUtils
import com.cgfay.uitls.utils.StringUtils
import java.io.IOException

class VideoPlayerFragment : Fragment() {

    companion object {
        private const val PATH = "PATH"
        fun newInstance(path: String): VideoPlayerFragment {
            val fragment = VideoPlayerFragment()
            fragment.arguments = Bundle().apply { putString(PATH, path) }
            return fragment
        }
    }

    private var videoPlayer: VideoPlayer? = null
    private var duration: Float = 0f
    private val progressState = mutableStateOf(0f)
    private val speedState = mutableStateOf(50f)
    private var playing by mutableStateOf(false)
    private var currentTime by mutableStateOf("")
    private var seeking = false
    private val handler = Handler(Looper.getMainLooper())
    private var surfaceView: SurfaceView? = null

    override fun onCreateView(
        inflater: android.view.LayoutInflater,
        container: android.view.ViewGroup?,
        savedInstanceState: Bundle?
    ): android.view.View {
        val path = requireArguments().getString(PATH) ?: ""
        initPlayer(path)
        return ComposeView(requireContext()).apply {
            setContent {
                VideoPlayerScreen(
                    path = path,
                    progress = progressState.value,
                    speedProgress = speedState.value,
                    currentPosition = currentTime,
                    isPlaying = playing,
                    surfaceProvider = {
                        if (surfaceView == null) {
                            surfaceView = SurfaceView(it).apply {
                                holder.addCallback(object : SurfaceHolder.Callback {
                                    override fun surfaceCreated(holder: SurfaceHolder) {
                                        videoPlayer?.setSurface(holder.surface)
                                    }
                                    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}
                                    override fun surfaceDestroyed(holder: SurfaceHolder) {
                                        videoPlayer?.setSurface(null)
                                    }
                                })
                            }
                        }
                        surfaceView!!
                    },
                    onPlayPause = { togglePlayPause() },
                    onProgressChange = { value ->
                        progressState.value = value
                    },
                    onProgressChangeFinished = {
                        seekTo(progressState.value)
                    },
                    onSpeedChange = { sp ->
                        speedState.value = sp
                        val speed = if (sp > 50f) 1.0f + (sp - 50f) / 50f else 0.5f + sp / 100f
                        setSpeed(speed)
                    }
                )
            }
        }
    }

    override fun onStart() {
        super.onStart()
        videoPlayer?.start()
        playing = true
    }

    override fun onResume() {
        super.onResume()
        videoPlayer?.start()
        playing = true
    }

    override fun onPause() {
        videoPlayer?.pause()
        playing = false
        super.onPause()
    }

    override fun onDestroy() {
        videoPlayer?.stop()
        videoPlayer?.release()
        videoPlayer = null
        super.onDestroy()
    }

    private fun togglePlayPause() {
        videoPlayer?.let { mp ->
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
        videoPlayer = VideoPlayer().apply {
            setSpeed(1.0f)
            setLooping(true)
            setOnPreparedListener { mp ->
                if (context != null && surfaceView != null) {
                    val params = surfaceView!!.layoutParams
                    params.width = DisplayUtils.getScreenWidth(requireContext())
                    params.height = mp.videoHeight * params.width / mp.videoWidth
                    surfaceView!!.layoutParams = params
                    duration = mp.duration
                }
            }
            setOnCurrentPositionListener { _, current, dur ->
                if (seeking) return@setOnCurrentPositionListener
                handler.post {
                    progressState.value = current
                    currentTime = StringUtils.generateStandardTime(current.toInt())
                }
            }
            setOnSeekCompleteListener {
                handler.postDelayed({ seeking = false }, 100)
            }
            setOnErrorListener { _, _, _ -> false }
            setOnCompletionListener { }
            try {
                setDataSource(path)
                prepare()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun seekTo(progress: Float) {
        handler.post {
            videoPlayer?.let {
                seeking = true
                it.seekTo(progress)
            }
        }
    }

    private fun setSpeed(speed: Float) {
        videoPlayer?.setSpeed(speed)
    }
}

@Composable
private fun VideoPlayerScreen(
    path: String,
    progress: Float,
    speedProgress: Float,
    currentPosition: String,
    isPlaying: Boolean,
    surfaceProvider: (android.content.Context) -> SurfaceView,
    onPlayPause: () -> Unit,
    onProgressChange: (Float) -> Unit,
    onProgressChangeFinished: () -> Unit,
    onSpeedChange: (Float) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx -> surfaceProvider(ctx) },
            modifier = Modifier.weight(1f)
        )
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = currentPosition)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "progress", modifier = Modifier.padding(end = 8.dp))
                Slider(
                    value = progress,
                    onValueChange = onProgressChange,
                    onValueChangeFinished = onProgressChangeFinished,
                    valueRange = 0f..duration.coerceAtLeast(1f)
                )
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
}
