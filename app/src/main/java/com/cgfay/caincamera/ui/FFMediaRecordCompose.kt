package com.cgfay.caincamera.ui

import android.widget.Toast
import android.opengl.GLSurfaceView
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.FragmentActivity
import com.cgfay.caincamera.renderer.FFRecordRenderer
import com.cgfay.caincamera.viewmodel.FFMediaRecordViewModel
import com.cgfay.caincamera.widget.GLRecordView
import com.cgfay.camera.widget.RecordButton
import com.cgfay.camera.widget.RecordProgressView
import com.cgfay.uitls.ui.CombineVideoDialog

@Composable
fun FFMediaRecordScreen(onFinish: () -> Unit) {
    val context = LocalContext.current
    val activity = context as FragmentActivity

    val glRecordViewHolder = remember { mutableStateOf<GLRecordView?>(null) }
    val progressViewHolder = remember { mutableStateOf<RecordProgressView?>(null) }
    val recordButtonHolder = remember { mutableStateOf<RecordButton?>(null) }
    val viewModelHolder = remember { mutableStateOf<FFMediaRecordViewModel?>(null) }
    var renderer by remember { mutableStateOf<FFRecordRenderer?>(null) }

    val viewModel = viewModelHolder.value ?: FFMediaRecordViewModel(activity).also { viewModelHolder.value = it }
    if (renderer == null) {
        renderer = FFRecordRenderer(viewModel)
    }

    val uiState by viewModel.uiState.collectAsState()

    DisposableEffect(Unit) {
        viewModel.setRecordSeconds(15)
        viewModel.onResume()
        onDispose {
            viewModel.onPause()
            viewModel.release()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = {
                GLRecordView(it).apply {
                    setEGLContextClientVersion(3)
                    setRenderer(renderer)
                    renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
                    glRecordViewHolder.value = this
                }
            },
            update = {
                uiState.surfaceTexture?.let { texture ->
                    it.queueEvent { renderer?.bindSurfaceTexture(texture) }
                }
                uiState.textureSize?.let { size -> renderer?.setTextureSize(size.first, size.second) }
                if (uiState.frameAvailable) it.requestRender()
            },
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(9f / 16f)
                .align(Alignment.TopCenter)
        )
        AndroidView(
            factory = { RecordProgressView(it).also { view -> progressViewHolder.value = view } },
            update = { view ->
                view.setProgress(uiState.progress)
                view.clear()
                uiState.progressSegments.forEach { seg -> view.addProgressSegment(seg) }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .padding(6.dp)
                .align(Alignment.TopCenter)
        )
        if (uiState.showViews) {
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
                }
            },
            modifier = Modifier
                .size(120.dp)
                .align(Alignment.BottomCenter)
                .padding(bottom = 40.dp)
        )
        if (uiState.showViews && viewModel.recordVideoSize > 0) {
            Button(
                onClick = { viewModel.deleteLastVideo() },
                modifier = Modifier.align(Alignment.BottomStart).padding(40.dp)
            ) { Text("Delete") }
            Button(
                onClick = { viewModel.mergeAndEdit() },
                modifier = Modifier.align(Alignment.BottomEnd).padding(40.dp)
            ) { Text("Next") }
        }
        if (uiState.showDialog) {
            CombineVideoDialog(message = "Please wait", dimable = false) {}
        }
        uiState.toast?.let { msg ->
            LaunchedEffect(msg) {
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            }
        }
        IconButton(onClick = onFinish, modifier = Modifier.align(Alignment.TopStart).padding(16.dp)) {
            Icon(Icons.Filled.ArrowBack, contentDescription = null)
        }
    }
}
