package com.cgfay.picker.compose

import android.graphics.PointF
import android.widget.VideoView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.cgfay.picker.model.MediaData
import com.cgfay.picker.utils.MediaMetadataUtils
import com.cgfay.picker.widget.subsamplingview.ImageSource
import com.cgfay.picker.widget.subsamplingview.OnImageEventListener
import com.cgfay.picker.widget.subsamplingview.SubsamplingScaleImageView
import com.cgfay.uitls.utils.DisplayUtils

/**
 * Preview screen for displaying a single [MediaData].
 */
@Composable
fun MediaPreviewScreen(mediaData: MediaData?, onClose: () -> Unit) {
    if (mediaData == null) {
        LaunchedEffect(Unit) { onClose() }
        return
    }

    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val videoViewState = remember { mutableStateOf<VideoView?>(null) }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        if (mediaData.isImage()) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    SubsamplingScaleImageView(ctx).apply {
                        maxScale = MAX_SCALE
                        setOnClickListener { onClose() }
                        setOnImageEventListener(object : OnImageEventListener {
                            override fun onImageLoaded(width: Int, height: Int) {
                                calculatePictureScale(this@apply, width, height)
                            }
                        })
                        setImage(ImageSource.uri(mediaData.contentUri))
                    }
                }
            )
        } else {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    VideoView(ctx).apply {
                        videoViewState.value = this
                        setOnPreparedListener { seekTo(0) }
                        setOnCompletionListener { seekTo(0) }
                        setOnErrorListener { _, _, _ ->
                            stopPlayback()
                            false
                        }
                        setVideoPath(MediaMetadataUtils.getPath(context, mediaData.contentUri))
                        setOnClickListener { onClose() }
                        start()
                    }
                }
            )
        }
    }

    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            val videoView = videoViewState.value
            when (event) {
                Lifecycle.Event.ON_RESUME -> if (videoView != null && !videoView.isPlaying) videoView.start()
                Lifecycle.Event.ON_PAUSE -> if (videoView != null && videoView.canPause()) videoView.pause()
                Lifecycle.Event.ON_DESTROY -> videoView?.stopPlayback()
                else -> Unit
            }
        }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
            videoViewState.value?.stopPlayback()
        }
    }
}

private fun calculatePictureScale(view: SubsamplingScaleImageView, width: Int, height: Int) {
    if (height >= LONG_IMG_MINIMUM_LENGTH && height / width >= LONG_IMG_ASPECT_RATIO) {
        val screenWidth = DisplayUtils.getScreenWidth(view.context)
        val scale = screenWidth / width.toFloat()
        val centerX = screenWidth / 2f
        view.setScaleAndCenterWithAnim(scale, PointF(centerX, 0f))
        view.setDoubleTapZoomScale(scale)
    }
}

private const val MAX_SCALE = 15f
private const val LONG_IMG_ASPECT_RATIO = 3
private const val LONG_IMG_MINIMUM_LENGTH = 1500
