package com.cgfay.camera.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.cgfay.camera.viewmodel.CameraPreviewViewModel
import com.cgfay.camera.ui.CameraPreviewScreen

class CameraComposeActivity : ComponentActivity() {
    private val previewViewModel: CameraPreviewViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CameraPreviewScreen(viewModel = previewViewModel)
        }
    }
}
