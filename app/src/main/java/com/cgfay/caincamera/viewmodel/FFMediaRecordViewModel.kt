package com.cgfay.caincamera.viewmodel

import android.graphics.SurfaceTexture
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import com.cgfay.caincamera.presenter.FFMediaRecordPresenter
import com.cgfay.caincamera.renderer.FFRecordRenderer
import com.cgfay.caincamera.ui.FFMediaRecordView
import com.cgfay.caincamera.widget.GLRecordView
import com.cgfay.camera.widget.RecordButton
import com.cgfay.camera.widget.RecordProgressView
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FFMediaRecordViewModel(private val activity: FragmentActivity) : ViewModel(), FFMediaRecordView {

    data class UiState(
        val showDelete: Boolean = false,
        val showNext: Boolean = false,
        val showSwitch: Boolean = true,
        val progress: Float = 0f,
        val segments: List<Float> = emptyList(),
        val showDialog: Boolean = false
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> get() = _uiState

    val presenter: FFMediaRecordPresenter = FFMediaRecordPresenter(activity, this)

    var glRecordView: GLRecordView? = null
    var progressView: RecordProgressView? = null
    var recordButton: RecordButton? = null
    var renderer: FFRecordRenderer? = null

    init {
        presenter.setRecordSeconds(15)
    }

    fun onResume() { presenter.onResume() }
    fun onPause() { presenter.onPause() }
    fun release() { presenter.release() }
    fun startRecord() { presenter.startRecord() }
    fun stopRecord() { presenter.stopRecord() }
    fun switchCamera() { presenter.switchCamera() }
    fun deleteLastVideo() { presenter.deleteLastVideo() }
    fun mergeAndEdit() { presenter.mergeAndEdit() }
    fun isRecording(): Boolean = presenter.isRecording()

    // FFMediaRecordView implementation
    override fun hidViews() {
        _uiState.value = _uiState.value.copy(
            showDelete = false,
            showNext = false,
            showSwitch = false
        )
    }

    override fun showViews() {
        val size = presenter.recordVideoSize
        _uiState.value = _uiState.value.copy(
            showDelete = size > 0,
            showNext = size > 0,
            showSwitch = true
        )
        recordButton?.reset()
    }

    override fun setProgress(progress: Float) {
        _uiState.value = _uiState.value.copy(progress = progress)
        progressView?.setProgress(progress)
    }

    override fun addProgressSegment(progress: Float) {
        val list = _uiState.value.segments.toMutableList()
        list.add(progress)
        _uiState.value = _uiState.value.copy(progress = 0f, segments = list)
        progressView?.addProgressSegment(progress)
    }

    override fun deleteProgressSegment() {
        val list = _uiState.value.segments.toMutableList()
        if (list.isNotEmpty()) list.removeLast()
        _uiState.value = _uiState.value.copy(progress = 0f, segments = list)
        progressView?.deleteProgressSegment()
    }

    override fun bindSurfaceTexture(surfaceTexture: SurfaceTexture) {
        glRecordView?.queueEvent { renderer?.bindSurfaceTexture(surfaceTexture) }
    }

    override fun updateTextureSize(width: Int, height: Int) {
        renderer?.setTextureSize(width, height)
    }

    override fun onFrameAvailable() {
        glRecordView?.requestRender()
    }

    override fun showProgressDialog() {
        _uiState.value = _uiState.value.copy(showDialog = true)
    }

    override fun hideProgressDialog() {
        _uiState.value = _uiState.value.copy(showDialog = false)
    }

    override fun showToast(msg: String) {
        Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show()
    }
}
