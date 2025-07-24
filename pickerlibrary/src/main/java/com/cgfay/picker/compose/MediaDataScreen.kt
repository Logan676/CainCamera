package com.cgfay.picker.compose

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.loader.app.LoaderManager
import coil.compose.AsyncImage
import com.cgfay.picker.model.AlbumData
import com.cgfay.picker.model.MediaData
import com.cgfay.picker.scanner.IMediaDataReceiver
import com.cgfay.picker.scanner.ImageDataScanner
import com.cgfay.picker.scanner.MediaDataScanner
import com.cgfay.picker.scanner.VideoDataScanner
import com.cgfay.uitls.utils.StringUtils

@Composable
fun MediaDataScreen(
    loaderManager: LoaderManager,
    album: AlbumData?,
    spanCount: Int,
    scannerProvider: (Context, LoaderManager, IMediaDataReceiver) -> MediaDataScanner,
    modifier: Modifier = Modifier,
    itemContent: @Composable (MediaData) -> Unit
) {
    val context = LocalContext.current
    val mediaList = remember { mutableStateListOf<MediaData>() }

    val receiver = remember {
        IMediaDataReceiver { list ->
            mediaList.clear()
            mediaList.addAll(list)
        }
    }

    val scanner = remember { scannerProvider(context, loaderManager, receiver) }

    DisposableEffect(scanner) {
        scanner.resume()
        onDispose { scanner.destroy() }
    }

    LaunchedEffect(album) {
        album?.let { scanner.loadAlbumMedia(it) }
    }

    LazyVerticalGrid(columns = GridCells.Fixed(spanCount), modifier = modifier) {
        items(mediaList, key = { it.hashCode() }) { media ->
            itemContent(media)
        }
    }
}

@Composable
fun ImageDataScreen(
    loaderManager: LoaderManager,
    album: AlbumData?,
    spanCount: Int,
    onPreview: (MediaData) -> Unit
) {
    MediaDataScreen(loaderManager, album, spanCount, { ctx, lm, receiver ->
        ImageDataScanner(ctx, lm, receiver)
    }) { media ->
        AsyncImage(
            model = media.contentUri,
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clickable { onPreview(media) },
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
fun VideoDataScreen(
    loaderManager: LoaderManager,
    album: AlbumData?,
    spanCount: Int,
    onPreview: (MediaData) -> Unit
) {
    MediaDataScreen(loaderManager, album, spanCount, { ctx, lm, receiver ->
        VideoDataScanner(ctx, lm, receiver)
    }) { media ->
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clickable { onPreview(media) }
        ) {
            AsyncImage(
                model = media.contentUri,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Text(
                text = StringUtils.generateStandardTime(media.durationMs.toInt()),
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(4.dp)
            )
        }
    }
}
