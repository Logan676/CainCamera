package com.cgfay.caincamera.viewmodel

import android.graphics.SurfaceTexture
import androidx.lifecycle.ViewModel
import com.cgfay.caincamera.activity.BaseRecordActivity
import com.cgfay.caincamera.presenter.RecordPresenter
import com.cgfay.media.recorder.SpeedMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class RecordViewModel(private val activity: BaseRecordActivity) : ViewModel() {

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

    val presenter: RecordPresenter = RecordPresenter(activity)

    init {
        presenter.setRecordSeconds(15)
    }

    fun onResume() {
        presenter.onResume()
    }

    fun onPause() {
        presenter.onPause()
    }

    fun release() {
        presenter.release()
    }

    fun setAudioEnable(enable: Boolean) {
        presenter.setAudioEnable(enable)
    }

    fun switchCamera() = presenter.switchCamera()
    fun deleteLastVideo() = presenter.deleteLastVideo()
    fun mergeAndEdit() = presenter.mergeAndEdit()
    fun startRecord() = presenter.startRecord()
    fun stopRecord() = presenter.stopRecord()
    fun setSpeedMode(mode: SpeedMode) = presenter.setSpeedMode(mode)

    // callbacks from activity / presenter
    fun hideViews() {
        _uiState.value = _uiState.value.copy(
            showDelete = false,
            showNext = false,
            showSwitch = false
        )
    }

    fun showViews(recordVideoSize: Int) {
        _uiState.value = _uiState.value.copy(
            showDelete = recordVideoSize > 0,
            showNext = recordVideoSize > 0,
            showSwitch = true
        )
    }

    fun setRecordProgress(progress: Float) {
        _uiState.value = _uiState.value.copy(progress = progress)
    }

    fun addProgressSegment(progress: Float) {
        val list = _uiState.value.segments.toMutableList()
        list.add(progress)
        _uiState.value = _uiState.value.copy(progress = 0f, segments = list)
    }

    fun deleteProgressSegment() {
        val list = _uiState.value.segments.toMutableList()
        if (list.isNotEmpty()) {
            list.removeLast()
        }
        _uiState.value = _uiState.value.copy(progress = 0f, segments = list)
    }

    fun showProgressDialog() {
        _uiState.value = _uiState.value.copy(showDialog = true)
    }

    fun hideProgressDialog() {
        _uiState.value = _uiState.value.copy(showDialog = false)
    }
}
