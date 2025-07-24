package com.cgfay.caincamera.ui

import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.cgfay.media.MusicPlayer
import java.io.IOException

@Composable
fun MusicPlayerScreen(path: String) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val player = remember { MusicPlayer() }
    var duration by remember { mutableStateOf(0f) }
    var progress by remember { mutableStateOf(0f) }
    var speedProgress by remember { mutableStateOf(50f) }
    var playing by remember { mutableStateOf(false) }
    val handler = remember { Handler(Looper.getMainLooper()) }

    LaunchedEffect(path) {
        player.setSpeed(1.0f)
        player.setLooping(true)
        player.setOnPreparedListener { mp -> duration = mp.duration }
        player.setOnCurrentPositionListener { _, current, dur ->
            handler.post {
                progress = if (dur > 0f) current / dur * 100f else 0f
            }
        }
        try {
            player.setDataSource(path)
            player.prepare()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START, Lifecycle.Event.ON_RESUME -> {
                    player.start()
                    playing = true
                }
                Lifecycle.Event.ON_PAUSE, Lifecycle.Event.ON_STOP -> {
                    player.pause()
                    playing = false
                }
                Lifecycle.Event.ON_DESTROY -> {
                    player.release()
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            player.release()
        }
    }

    MusicPlayerContent(
        path = path,
        isPlaying = playing,
        progress = progress,
        speedProgress = speedProgress,
        onPlayPause = {
            if (player.isPlaying) {
                player.pause()
                playing = false
            } else {
                player.start()
                playing = true
            }
        },
        onProgressChange = { seek ->
            progress = seek
            if (duration > 0) player.seekTo(seek / 100f * duration)
        },
        onSpeedChange = { sp ->
            speedProgress = sp
            val speed = if (sp > 50f) 1.0f + (sp - 50f) / 50f else 0.5f + sp / 100f
            player.setSpeed(speed)
        }
    )
}

@Composable
private fun MusicPlayerContent(
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

