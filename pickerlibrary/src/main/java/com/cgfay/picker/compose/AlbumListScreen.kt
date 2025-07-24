package com.cgfay.picker.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.cgfay.picker.model.AlbumData
import com.cgfay.scan.R

@Composable
fun AlbumListScreen(onBack: () -> Unit, viewModel: PickerViewModel) {
    val albums by viewModel.albumList.collectAsState()
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(text = "Albums") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(painterResource(id = R.drawable.ic_media_picker_close), contentDescription = null)
                }
            }
        )
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(albums) { album ->
                AlbumRow(album = album, onClick = {
                    viewModel.selectAlbum(album)
                    onBack()
                })
            }
        }
    }
}

@Composable
private fun AlbumRow(album: AlbumData, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_media_picker_preview),
            contentDescription = null,
            modifier = Modifier.size(56.dp)
        )
        Column(modifier = Modifier.padding(start = 12.dp)) {
            Text(text = album.displayName, style = MaterialTheme.typography.bodyLarge)
            Text(text = album.count.toString(), style = MaterialTheme.typography.bodySmall)
        }
    }
}
