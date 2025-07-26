package com.cgfay.media.ui

import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cgfay.media.CainMediaPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.IOException

@Composable
fun CainPlayerUI(
    path: String,
    modifier: Modifier = Modifier,
    viewModel: CainPlayerViewModel = viewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val surfaceView = remember {
        SurfaceView(context).apply {
            holder.addCallback(object : SurfaceHolder.Callback {
                override fun surfaceCreated(holder: SurfaceHolder) {
                    viewModel.setSurface(holder.surface)
                }
                override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}
                override fun surfaceDestroyed(holder: SurfaceHolder) {
                    viewModel.setSurface(null)
                }
            })
        }
    }

    LaunchedEffect(path) { viewModel.load(path) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START, Lifecycle.Event.ON_RESUME -> viewModel.start()
                Lifecycle.Event.ON_PAUSE, Lifecycle.Event.ON_STOP -> viewModel.pause()
                Lifecycle.Event.ON_DESTROY -> viewModel.release()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            viewModel.release()
        }
    }

    AndroidView(factory = { surfaceView }, modifier = modifier)
}

class CainPlayerViewModel : ViewModel() {
    private val player = CainMediaPlayer()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()

    private val _position = MutableStateFlow(0L)
    val position: StateFlow<Long> = _position.asStateFlow()

    init {
        player.setOnPreparedListener { mp -> _duration.value = mp.duration }
        player.setOnCurrentPositionListener { _, current, _ -> _position.value = current.toLong() }
    }

    fun setSurface(surface: Surface?) { player.setSurface(surface) }

    fun load(path: String) {
        try {
            player.setDataSource(path)
            player.prepare()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun start() { player.start(); _isPlaying.value = true }
    fun pause() { player.pause(); _isPlaying.value = false }
    fun release() { player.release() }
}
