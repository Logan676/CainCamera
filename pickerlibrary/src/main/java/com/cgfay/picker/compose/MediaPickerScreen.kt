package com.cgfay.picker.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.cgfay.picker.model.MediaData
import com.cgfay.scan.R
import coil.compose.AsyncImage

@Composable
fun MediaPickerScreen(
    onPreview: (MediaData) -> Unit,
    onShowAlbums: () -> Unit,
    viewModel: PickerViewModel
) {
    val mediaList by viewModel.mediaList.collectAsState()
    val selected by viewModel.selectedMedia.collectAsState()
    val album by viewModel.selectedAlbum.collectAsState()
    val currentTab by viewModel.currentTab.collectAsState()
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
        TabRow(selectedTabIndex = currentTab) {
            if (!viewModel.pickerParam.showVideoOnly()) {
                Tab(selected = currentTab == 0, onClick = { viewModel.selectTab(0) }) {
                    Text("Images", modifier = Modifier.padding(12.dp))
                }
            }
            if (!viewModel.pickerParam.showImageOnly()) {
                val index = if (viewModel.pickerParam.showVideoOnly()) 0 else 1
                Tab(selected = currentTab == index, onClick = { viewModel.selectTab(index) }) {
                    Text("Videos", modifier = Modifier.padding(12.dp))
                }
            }
        }
        LazyVerticalGrid(columns = GridCells.Fixed(viewModel.pickerParam.spanCount), modifier = Modifier.weight(1f)) {
            items(mediaList) { media ->
                Box(modifier = Modifier
                    .padding(2.dp)
                    .clickable { viewModel.toggle(media) }, contentAlignment = Alignment.Center) {
                    AsyncImage(
                        model = media.contentUri,
                        contentDescription = null,
                        modifier = Modifier.fillMaxWidth().aspectRatio(1f)
                    )
                    if (selected.contains(media)) {
                        Text("\u2713", modifier = Modifier.align(Alignment.TopEnd).padding(4.dp))
                    }
                }
            }
        }
    }
}
