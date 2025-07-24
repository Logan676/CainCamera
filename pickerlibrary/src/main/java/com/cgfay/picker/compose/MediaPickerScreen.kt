package com.cgfay.picker.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.cgfay.picker.model.MediaData
import com.cgfay.scan.R

@Composable
fun MediaPickerScreen(
    onPreview: (MediaData) -> Unit,
    onShowAlbums: () -> Unit,
    viewModel: PickerViewModel
) {
    val mediaList by viewModel.mediaList.collectAsState()
    val selected by viewModel.selectedMedia.collectAsState()
    val album by viewModel.selectedAlbum.collectAsState()
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(album?.displayName ?: "Media Picker") },
            navigationIcon = {
                IconButton(onClick = { viewModel.finish() }) {
                    Icon(painterResource(R.drawable.ic_media_picker_close), contentDescription = null)
                }
            },
            actions = {
                IconButton(onClick = onShowAlbums) {
                    Icon(painterResource(id = R.drawable.ic_media_album_indicator), contentDescription = null)
                }
                if (selected.isNotEmpty()) {
                    Text(
                        text = "Select(${selected.size})",
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .clickable { viewModel.confirmSelection() }
                    )
                }
            }
        )
        LazyVerticalGrid(columns = GridCells.Fixed(3), modifier = Modifier.weight(1f)) {
            items(mediaList) { media ->
                Box(
                    modifier = Modifier
                        .padding(2.dp)
                        .clickable { onPreview(media) },
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = media.contentUri,
                        contentDescription = null,
                        modifier = Modifier.size(100.dp)
                    )
                }
            }
        }
    }
}
