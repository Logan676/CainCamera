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
import com.cgfay.caincamera.presenter.FFMediaRecordPresenter
import com.cgfay.caincamera.renderer.FFRecordRenderer
import com.cgfay.caincamera.widget.GLRecordView
import com.cgfay.camera.widget.RecordButton
import com.cgfay.camera.widget.RecordProgressView
import android.opengl.GLSurfaceView

@Composable
fun FFMediaRecordScreen(onFinish: () -> Unit) {
    val context = LocalContext.current
    val activity = context as FragmentActivity

    val showDelete = remember { mutableStateOf(false) }
    val showNext = remember { mutableStateOf(false) }
    val showSwitch = remember { mutableStateOf(true) }
    val showDialog = remember { mutableStateOf(false) }

    val glRecordViewHolder = remember { mutableStateOf<GLRecordView?>(null) }
    val progressViewHolder = remember { mutableStateOf<RecordProgressView?>(null) }
    val recordButtonHolder = remember { mutableStateOf<RecordButton?>(null) }
    val presenterHolder = remember { mutableStateOf<FFMediaRecordPresenter?>(null) }
    var renderer by remember { mutableStateOf<FFRecordRenderer?>(null) }

    val viewCallback = remember {
        object : FFMediaRecordView {
            override fun hidViews() {
                showDelete.value = false
                showNext.value = false
                showSwitch.value = false
            }
            override fun showViews() {
                val size = presenterHolder.value?.getRecordVideoSize() ?: 0
                showDelete.value = size > 0
                showNext.value = size > 0
                showSwitch.value = true
                recordButtonHolder.value?.reset()
            }
            override fun setProgress(progress: Float) {
                progressViewHolder.value?.setProgress(progress)
            }
            override fun addProgressSegment(progress: Float) {
                progressViewHolder.value?.addProgressSegment(progress)
            }
            override fun deleteProgressSegment() {
                progressViewHolder.value?.deleteProgressSegment()
            }
            override fun bindSurfaceTexture(surfaceTexture: SurfaceTexture) {
                glRecordViewHolder.value?.queueEvent { renderer?.bindSurfaceTexture(surfaceTexture) }
            }
            override fun updateTextureSize(width: Int, height: Int) {
                renderer?.setTextureSize(width, height)
            }
            override fun onFrameAvailable() {
                glRecordViewHolder.value?.requestRender()
            }
            override fun showProgressDialog() { showDialog.value = true }
            override fun hideProgressDialog() { showDialog.value = false }
            override fun showToast(msg: String) { Toast.makeText(context, msg, Toast.LENGTH_SHORT).show() }
        }
    }

    val presenter = presenterHolder.value ?: FFMediaRecordPresenter(activity, viewCallback).also { presenterHolder.value = it }
    if (renderer == null) {
        renderer = FFRecordRenderer(presenter)
    }

    DisposableEffect(Unit) {
        presenter.setRecordSeconds(15)
        presenter.onResume()
        onDispose {
            presenter.onPause()
            presenter.release()
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
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(9f / 16f)
                .align(Alignment.TopCenter)
        )
        AndroidView(
            factory = { RecordProgressView(it).also { view -> progressViewHolder.value = view } },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .padding(6.dp)
                .align(Alignment.TopCenter)
        )
        if (showSwitch.value) {
            Button(
                onClick = { if (!presenter.isRecording()) presenter.switchCamera() },
                modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)
            ) { Text("Switch") }
        }
        AndroidView(
            factory = {
                RecordButton(it).apply {
                    addRecordStateListener(object : RecordButton.RecordStateListener {
                        override fun onRecordStart() { presenter.startRecord() }
                        override fun onRecordStop() { presenter.stopRecord() }
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
        if (showDelete.value) {
            Button(
                onClick = { presenter.deleteLastVideo() },
                modifier = Modifier.align(Alignment.BottomStart).padding(40.dp)
            ) { Text("Delete") }
        }
        if (showNext.value) {
            Button(
                onClick = { presenter.mergeAndEdit() },
                modifier = Modifier.align(Alignment.BottomEnd).padding(40.dp)
            ) { Text("Next") }
        }
        if (showDialog.value) {
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
