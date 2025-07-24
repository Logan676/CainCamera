package com.cgfay.video.compose

import androidx.compose.foundation.layout.*
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
import com.cgfay.video.compose.EffectCategoryBar
import com.cgfay.video.compose.VideoEffectList
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
    val category by viewModel.category.collectAsState()
    val effects by viewModel.effectList.collectAsState()
    val selectedIndex by viewModel.selectedIndex.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(1f)) {
            AndroidView(factory = { context -> VideoTextureView(context) }, modifier = Modifier.fillMaxSize())
        }
        EffectCategoryBar(selected = category, onSelected = { viewModel.selectCategory(it) }, modifier = Modifier.fillMaxWidth())
        VideoEffectList(effects = effects, selectedIndex = selectedIndex, onSelected = { index, effect -> viewModel.selectEffect(index) }, modifier = Modifier.height(80.dp).fillMaxWidth())
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
