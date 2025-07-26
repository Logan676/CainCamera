package com.cgfay.picker.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.cgfay.picker.model.MediaData
import com.cgfay.scan.R

/** Grid view for media thumbnails */
import com.cgfay.picker.compose.MediaGrid

@Composable
fun MediaPickerScreen(
    onPreview: (MediaData) -> Unit,
    onShowAlbums: () -> Unit,
    viewModel: PickerViewModel
) {
    val mediaList by viewModel.mediaList.collectAsState()
    val selected by viewModel.selectedMedia.collectAsState()
    val album by viewModel.selectedAlbum.collectAsState()
    val tab by viewModel.selectedTab.collectAsState()
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
        TabRow(selectedTabIndex = tab.ordinal) {
            Tab(
                selected = tab == PickerTab.IMAGE,
                onClick = { viewModel.selectTab(PickerTab.IMAGE) },
                text = { Text("Images") }
            )
            Tab(
                selected = tab == PickerTab.VIDEO,
                onClick = { viewModel.selectTab(PickerTab.VIDEO) },
                text = { Text("Videos") }
            )
        }
        MediaGrid(
            mediaList = mediaList,
            selected = selected,
            onPreview = onPreview,
            onToggle = { viewModel.toggle(it) },
            columns = 3,
            modifier = Modifier.weight(1f)
        )
    }
}
