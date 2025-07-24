package com.cgfay.caincamera.ui

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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.cgfay.media.VideoPlayer
import com.cgfay.uitls.utils.DisplayUtils
import com.cgfay.uitls.utils.StringUtils
import java.io.IOException

@Composable
fun VideoPlayerScreen(path: String) {
    val context = LocalContext.current
    var duration by remember { mutableStateOf(0f) }
    var progress by remember { mutableStateOf(0f) }
    var speedProgress by remember { mutableStateOf(50f) }
    var playing by remember { mutableStateOf(false) }
    var currentTime by remember { mutableStateOf("") }
    var seeking by remember { mutableStateOf(false) }
    val handler = remember { Handler(Looper.getMainLooper()) }

    val videoPlayer = remember(path) {
        VideoPlayer().apply {
            setSpeed(1.0f)
            setLooping(true)
        }
    }

    val surfaceView = remember {
        SurfaceView(context).apply {
            holder.addCallback(object : SurfaceHolder.Callback {
                override fun surfaceCreated(holder: SurfaceHolder) {
                    videoPlayer.setSurface(holder.surface)
                }
                override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}
                override fun surfaceDestroyed(holder: SurfaceHolder) {
                    videoPlayer.setSurface(null)
                }
            })
        }
    }

    DisposableEffect(videoPlayer, path) {
        videoPlayer.setOnPreparedListener { mp ->
            val params = surfaceView.layoutParams
            params.width = DisplayUtils.getScreenWidth(context)
            params.height = mp.videoHeight * params.width / mp.videoWidth
            surfaceView.layoutParams = params
            duration = mp.duration
        }
        videoPlayer.setOnCurrentPositionListener { _, current, _ ->
            if (seeking) return@setOnCurrentPositionListener
            handler.post {
                progress = current
                currentTime = StringUtils.generateStandardTime(current.toInt())
            }
        }
        videoPlayer.setOnSeekCompleteListener {
            handler.postDelayed({ seeking = false }, 100)
        }
        videoPlayer.setOnErrorListener { _, _, _ -> false }
        videoPlayer.setOnCompletionListener { }
        try {
            videoPlayer.setDataSource(path)
            videoPlayer.prepare()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        videoPlayer.start()
        playing = true
        onDispose {
            videoPlayer.stop()
            videoPlayer.release()
        }
    }

    fun togglePlayPause() {
        if (videoPlayer.isPlaying) {
            videoPlayer.pause()
            playing = false
        } else {
            videoPlayer.start()
            playing = true
        }
    }

    fun seekTo(value: Float) {
        seeking = true
        videoPlayer.seekTo(value)
    }

    fun setSpeed(speed: Float) {
        videoPlayer.setSpeed(speed)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        AndroidView(factory = { surfaceView }, modifier = Modifier.weight(1f))
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = currentTime)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "progress", modifier = Modifier.padding(end = 8.dp))
                Slider(
                    value = progress,
                    onValueChange = { progress = it },
                    onValueChangeFinished = { seekTo(progress) },
                    valueRange = 0f..duration.coerceAtLeast(1f)
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "speed", modifier = Modifier.padding(end = 8.dp))
                Slider(value = speedProgress, onValueChange = { sp ->
                    speedProgress = sp
                    val speed = if (sp > 50f) 1.0f + (sp - 50f) / 50f else 0.5f + sp / 100f
                    setSpeed(speed)
                })
            }
            Button(onClick = { togglePlayPause() }, modifier = Modifier.padding(top = 16.dp)) {
                Text(if (playing) "pause" else "play")
            }
            Text(text = path, modifier = Modifier.padding(top = 16.dp))
        }
    }
}

