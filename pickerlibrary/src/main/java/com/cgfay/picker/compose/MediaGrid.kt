package com.cgfay.picker.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.cgfay.picker.model.MediaData
import com.cgfay.scan.R
import com.cgfay.uitls.utils.StringUtils

@Composable
fun MediaGrid(
    mediaList: List<MediaData>,
    selected: List<MediaData>,
    columns: Int = 3,
    onPreview: (MediaData) -> Unit,
    onToggle: (MediaData) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        modifier = modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
        contentPadding = PaddingValues(2.dp)
    ) {
        items(mediaList) { media ->
            val index = selected.indexOf(media).takeIf { it >= 0 }?.plus(1)
            MediaGridItem(media, index, onPreview, onToggle)
        }
    }
}

@Composable
private fun MediaGridItem(
    media: MediaData,
    selectedIndex: Int?,
    onPreview: (MediaData) -> Unit,
    onToggle: (MediaData) -> Unit
) {
    Box(modifier = Modifier
        .aspectRatio(1f)
        .clickable { onPreview(media) }
    ) {
        AsyncImage(
            model = media.contentUri,
            contentDescription = null,
            modifier = Modifier.fillMaxSize()
        )
        if (media.isVideo()) {
            Text(
                text = StringUtils.generateStandardTime(media.durationMs.toInt()),
                color = Color.White,
                fontSize = 12.sp,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .background(Color.Black.copy(alpha = 0.5f))
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            )
        }
        Box(
            modifier = Modifier
                .size(24.dp)
                .align(Alignment.TopEnd)
                .padding(4.dp)
                .clickable { onToggle(media) },
            contentAlignment = Alignment.Center
        ) {
            if (selectedIndex != null) {
                Image(
                    painter = painterResource(id = R.drawable.ic_media_picker_selected),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize()
                )
                Text(
                    text = selectedIndex.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Black
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.ic_media_picker_unselected),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
