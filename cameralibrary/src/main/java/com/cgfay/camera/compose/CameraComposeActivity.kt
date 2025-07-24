package com.cgfay.camera.compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.cgfay.camera.presenter.CameraPreviewPresenter

class CameraComposeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val presenter = CameraPreviewPresenter()
        setContent {
            CameraPreviewScreen(presenter = presenter)
        }
    }
}
