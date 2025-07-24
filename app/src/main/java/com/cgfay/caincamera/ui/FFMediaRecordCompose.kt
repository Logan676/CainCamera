package com.cgfay.caincamera.ui

import android.graphics.SurfaceTexture
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cgfay.caincamera.renderer.FFRecordRenderer
import com.cgfay.caincamera.viewmodel.FFMediaRecordViewModel
import com.cgfay.caincamera.viewmodel.FFMediaRecordViewModelFactory
import com.cgfay.caincamera.widget.GLRecordView
import com.cgfay.camera.widget.RecordButton
import com.cgfay.camera.widget.RecordProgressView
import android.opengl.GLSurfaceView

@Composable
fun FFMediaRecordScreen(onFinish: () -> Unit) {
    val context = LocalContext.current
    val activity = context as FragmentActivity

    val viewModel: FFMediaRecordViewModel = viewModel(factory = FFMediaRecordViewModelFactory(activity))
    val uiState by viewModel.uiState.collectAsState()

    var renderer by remember { mutableStateOf<FFRecordRenderer?>(null) }
    if (renderer == null) {
        renderer = FFRecordRenderer(viewModel.presenter)
        viewModel.renderer = renderer
    }

    DisposableEffect(Unit) {
        viewModel.onResume()
        onDispose {
            viewModel.onPause()
            viewModel.release()
        }
    }

    val glRecordViewHolder = remember { mutableStateOf<GLRecordView?>(null) }
    val progressViewHolder = remember { mutableStateOf<RecordProgressView?>(null) }
    val recordButtonHolder = remember { mutableStateOf<RecordButton?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = {
                GLRecordView(it).apply {
                    setEGLContextClientVersion(3)
                    setRenderer(renderer)
                    renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
                    glRecordViewHolder.value = this
                    viewModel.glRecordView = this
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(9f / 16f)
                .align(Alignment.TopCenter)
        )
        AndroidView(
            factory = { RecordProgressView(it).also { view ->
                progressViewHolder.value = view
                viewModel.progressView = view
            } },
            update = { view ->
                view.clear()
                uiState.segments.forEach { view.addProgressSegment(it) }
                view.setProgress(uiState.progress)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .padding(6.dp)
                .align(Alignment.TopCenter)
        )
        if (uiState.showSwitch) {
            Button(
                onClick = { if (!viewModel.isRecording()) viewModel.switchCamera() },
                modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)
            ) { Text("Switch") }
        }
        AndroidView(
            factory = {
                RecordButton(it).apply {
                    addRecordStateListener(object : RecordButton.RecordStateListener {
                        override fun onRecordStart() { viewModel.startRecord() }
                        override fun onRecordStop() { viewModel.stopRecord() }
                        override fun onZoom(percent: Float) {}
                    })
                    recordButtonHolder.value = this
                    viewModel.recordButton = this
                }
            },
            modifier = Modifier
                .size(120.dp)
                .align(Alignment.BottomCenter)
                .padding(bottom = 40.dp)
        )
        if (uiState.showDelete) {
            Button(
                onClick = { viewModel.deleteLastVideo() },
                modifier = Modifier.align(Alignment.BottomStart).padding(40.dp)
            ) { Text("Delete") }
        }
        if (uiState.showNext) {
            Button(
                onClick = { viewModel.mergeAndEdit() },
                modifier = Modifier.align(Alignment.BottomEnd).padding(40.dp)
            ) { Text("Next") }
        }
        if (uiState.showDialog) {
            AlertDialog(
                onDismissRequest = {},
                title = { Text("Processing") },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Please wait")
                    }
                },
                confirmButton = {}
            )
        }
        IconButton(onClick = onFinish, modifier = Modifier.align(Alignment.TopStart).padding(16.dp)) {
            Icon(Icons.Filled.ArrowBack, contentDescription = null)
        }
    }
}
