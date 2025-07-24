package com.cgfay.uitls.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.cgfay.uitls.bean.MusicData
import com.cgfay.uitls.viewmodel.MusicPickerViewModel
import com.cgfay.utilslibrary.R

@Composable
fun MusicPickerNavGraph(
    onMusicSelected: (MusicData) -> Unit,
    onClose: () -> Unit,
    viewModel: MusicPickerViewModel = viewModel()
) {
    val navController = rememberNavController()
    NavHost(navController, startDestination = "picker") {
        composable("picker") {
            val list by viewModel.musicList.collectAsState()
            MusicPickerScreen(list, onClose) { data ->
                onMusicSelected(data)
            }
        }
    }
}

@Composable
private fun MusicPickerScreen(
    musicList: List<MusicData>,
    onClose: () -> Unit,
    onMusicSelected: (MusicData) -> Unit
) {
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
                Icon(painterResource(id = R.drawable.ic_close), contentDescription = null)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(id = R.string.music_selection),
                modifier = Modifier.weight(1f),
            )
        }
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(musicList) { data ->
                MusicItem(data) { onMusicSelected(data) }
            }
        }
    }
}

@Composable
private fun MusicItem(data: MusicData, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(start = 10.dp, end = 10.dp, top = 15.dp)
    ) {
        Text(text = data.name ?: "", color = Color.Black)
        Text(
            text = com.cgfay.uitls.utils.StringUtils.generateMillisTime(data.duration.toInt()),
            color = Color.Black,
            modifier = Modifier.padding(top = 5.dp)
        )
    }
    Divider(color = Color.Gray, thickness = 1.dp)
}
