package com.cgfay.caincamera.ui

import android.app.Activity
import android.database.Cursor
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.cgfay.uitls.bean.MusicData
import com.cgfay.uitls.scanner.LocalMusicScanner
import com.cgfay.uitls.utils.StringUtils

@Composable
fun MusicPickerScreen(
    onClose: () -> Unit,
    onMusicSelected: (MusicData) -> Unit
) {
    val context = LocalContext.current
    var musicList by remember { mutableStateOf(listOf<MusicData>()) }

    val scanner = remember {
        LocalMusicScanner(context as Activity, object : LocalMusicScanner.MusicScanCallbacks {
            override fun onMusicScanFinish(cursor: Cursor) {
                val list = mutableListOf<MusicData>()
                cursor.use { c ->
                    while (c.moveToNext()) {
                        list.add(MusicData.valueof(c))
                    }
                }
                musicList = list
            }
            override fun onMusicScanReset() { musicList = emptyList() }
        })
    }

    DisposableEffect(Unit) {
        scanner.scanLocalMusic()
        onDispose { scanner.destroy() }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = null)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Music Selection", modifier = Modifier.weight(1f))
        }
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(musicList) { data ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onMusicSelected(data) }
                        .padding(start = 10.dp, end = 10.dp, top = 15.dp)
                ) {
                    Text(text = data.name ?: "", color = Color.Black)
                    Text(
                        text = StringUtils.generateMillisTime(data.duration.toInt()),
                        color = Color.Black,
                        modifier = Modifier.padding(top = 5.dp)
                    )
                }
                Divider(color = Color.Gray, thickness = 1.dp)
            }
        }
    }
}

