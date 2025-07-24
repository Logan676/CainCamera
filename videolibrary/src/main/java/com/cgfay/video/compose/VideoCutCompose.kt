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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.cgfay.video.compose.widget.VideoCutBar
import com.cgfay.video.compose.widget.VideoSpeedLevelBar
import com.cgfay.video.compose.widget.VideoTexture

@Composable
fun VideoCutNavGraph(videoPath: String?, onFinish: () -> Unit) {
    val navController = rememberNavController()
    NavHost(navController, startDestination = "cut/{path}") {
        composable(
            route = "cut/{path}",
            arguments = listOf(navArgument("path") { type = NavType.StringType })
        ) { backStackEntry ->
            val path = backStackEntry.arguments?.getString("path") ?: ""
            VideoCutScreen(path = path, onBack = onFinish)
        }
    }
}

@Composable
fun VideoCutScreen(path: String, onBack: () -> Unit, viewModel: VideoCutViewModel = viewModel()) {
    viewModel.setVideoPath(path)
    val progress by viewModel.progress.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        // video preview
        VideoTexture(modifier = Modifier.fillMaxSize())

        // bottom controls
        Column(modifier = Modifier.align(Alignment.BottomCenter)) {
            VideoSpeedLevelBar(modifier = Modifier.fillMaxWidth())
            VideoCutBar(modifier = Modifier.fillMaxWidth().height(70.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(onClick = onBack) { Text("Back") }
                Button(onClick = { viewModel.startCut() }) { Text("Cut") }
            }
            if (progress > 0) {
                Text(text = "Processing $progress%", modifier = Modifier.align(Alignment.CenterHorizontally))
            }
        }
    }
}
