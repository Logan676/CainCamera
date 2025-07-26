package com.cgfay.camera.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cgfay.cameralibrary.R
import com.cgfay.filter.glfilter.color.bean.DynamicColor
import com.cgfay.filter.glfilter.makeup.bean.DynamicMakeup
import com.cgfay.camera.viewmodel.CameraPreviewViewModel
import com.cgfay.camera.ui.PreviewResourceScreen
import com.cgfay.camera.ui.PreviewEffectScreen
import com.cgfay.camera.ui.PreviewSettingScreen

@Composable
fun CameraPreviewScreen(viewModel: CameraPreviewViewModel = viewModel()) {
    BackHandler { viewModel.onBackPressed() }
    Box(modifier = Modifier.fillMaxSize()) {
        Button(
            onClick = { viewModel.toggleResourcePanel() },
            modifier = Modifier.align(Alignment.TopCenter).padding(16.dp)
        ) {
            Text(text = "Resources")
        }

        Button(
            onClick = { viewModel.toggleEffectPanel() },
            modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)
        ) {
            Text(text = "Effects")
        }

        Button(
            onClick = { viewModel.toggleSettingPanel() },
            modifier = Modifier.align(Alignment.TopStart).padding(16.dp)
        ) {
            Text(text = "Settings")
        }

        AnimatedVisibility(
            visible = viewModel.showResourcePanel,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            PreviewResourceScreen { viewModel.onResourceChange(it) }
        }

        AnimatedVisibility(
            visible = viewModel.showEffectPanel,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            PreviewEffectScreen(
                onCompareEffect = { viewModel.onCompareEffect(it) },
                onFilterChange = { color -> viewModel.onFilterChange(color) },
                onMakeupChange = { makeup -> viewModel.onMakeupChange(makeup) }
            )
        }

        AnimatedVisibility(
            visible = viewModel.showSettingPanel,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            PreviewSettingScreen(
                onFlashChanged = {},
                onTouchTakeChanged = {},
                onTimeLapseChanged = {},
                onOpenCameraSetting = {},
                onLuminousCompensationChanged = {},
                onEdgeBlurChanged = {}
            )
        }
    }
}

