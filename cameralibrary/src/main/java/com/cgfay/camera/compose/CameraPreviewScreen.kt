package com.cgfay.camera.compose

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun CameraPreviewScreen(viewModel: CameraPreviewViewModel = viewModel()) {
    BackHandler { viewModel.onBackPressed() }
    Box(modifier = Modifier.fillMaxSize())
}
