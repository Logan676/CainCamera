package com.cgfay.video.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun MultiVideoScreen(pathList: List<String>?, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        val list = pathList ?: emptyList()
        if (list.isEmpty()) {
            Text(text = "No video selected")
        } else {
            LazyColumn {
                items(list) { path ->
                    Text(text = path)
                }
            }
        }
    }
}
