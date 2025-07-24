package com.cgfay.picker.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import coil.compose.AsyncImage
import com.cgfay.picker.model.MediaData
import com.cgfay.picker.scanner.IMediaDataReceiver
import com.cgfay.picker.scanner.MediaDataScanner

@Composable
fun MediaDataScreen(
    columns: Int = 3,
    scannerFactory: (IMediaDataReceiver) -> MediaDataScanner,
    onMediaClick: (MediaData) -> Unit = {}
) {
    val mediaList = remember { mutableStateListOf<MediaData>() }
    val receiver = remember {
        object : IMediaDataReceiver {
            override fun onMediaDataObserve(mediaDataList: List<MediaData>) {
                mediaList.clear()
                mediaList.addAll(mediaDataList)
            }
        }
    }
    val scanner = remember { scannerFactory(receiver) }
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                scanner.resume()
            }

            override fun onStop(owner: LifecycleOwner) {
                scanner.pause()
            }

            override fun onDestroy(owner: LifecycleOwner) {
                scanner.destroy()
            }
        }
        val lifecycle = lifecycleOwner.lifecycle
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
            scanner.destroy()
        }
    }

    LazyVerticalGrid(columns = GridCells.Fixed(columns)) {
        items(mediaList) { media ->
            AsyncImage(
                model = media.contentUri,
                contentDescription = null,
                modifier = Modifier
                    .padding(2.dp)
                    .clickable { onMediaClick(media) }
            )
        }
    }
}
