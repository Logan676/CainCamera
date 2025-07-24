package com.cgfay.video.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.cgfay.video.bean.EffectMimeType
import com.cgfay.video.widget.VideoTextureView

@Composable
fun VideoEditNavGraph(videoPath: String?, onFinish: () -> Unit) {
    val navController = rememberNavController()
    NavHost(navController, startDestination = "edit/{path}") {
        composable(
            route = "edit/{path}",
            arguments = listOf(navArgument("path") { type = NavType.StringType })
        ) { backStackEntry ->
            val path = backStackEntry.arguments?.getString("path") ?: ""
            VideoEditScreen(path = path, onBack = onFinish)
        }
    }
}

@Composable
fun VideoEditScreen(
    path: String,
    onBack: () -> Unit,
    viewModel: VideoEditViewModel = viewModel()
) {
    viewModel.setVideoPath(path)
    val selected by viewModel.selectedEffect.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(1f)) {
            AndroidView(factory = { context -> VideoTextureView(context) }, modifier = Modifier.fillMaxSize())
        }
        LazyRow(modifier = Modifier.fillMaxWidth().height(60.dp)) {
            items(EffectMimeType.values()) { type ->
                Text(
                    text = type.displayName,
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .clickable { viewModel.selectEffect(type.displayName) }
                )
            }
        }
        Text(text = selected ?: "No effect", modifier = Modifier.padding(16.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(onClick = onBack) { Text("Back") }
            Button(onClick = { /* TODO save */ }) { Text("Save") }
        }
    }
}
