package com.cgfay.caincamera.viewmodel

import android.app.Activity
import android.content.Intent
import android.graphics.SurfaceTexture
import android.media.AudioFormat
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import com.cgfay.camera.camera.CameraApi
import com.cgfay.camera.camera.CameraController
import com.cgfay.camera.camera.CameraXController
import com.cgfay.camera.camera.ICameraController
import com.cgfay.camera.camera.OnFrameAvailableListener
import com.cgfay.camera.camera.OnSurfaceTextureListener
import com.cgfay.camera.camera.PreviewCallback
import com.cgfay.camera.utils.PathConstraints
import com.cgfay.media.CainCommandEditor
import com.cgfay.media.recorder.AVFormatter
import com.cgfay.media.recorder.FFAudioRecorder
import com.cgfay.media.recorder.FFMediaRecorder
import com.cgfay.uitls.utils.FileUtils
import com.cgfay.video.activity.VideoEditActivity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.ArrayList

class FFMediaRecordViewModel(
    private val activity: FragmentActivity
) : ViewModel(), PreviewCallback, FFAudioRecorder.OnRecordCallback,
    OnSurfaceTextureListener, OnFrameAvailableListener, FFMediaRecorder.OnRecordListener {

    private val _uiState = MutableStateFlow(RecordUiState())
    val uiState: StateFlow<RecordUiState> = _uiState.asStateFlow()

    private val commandEditor = CainCommandEditor()
    private var maxDuration = 15_000
    private var remainDuration = maxDuration

    private var previewRotate = 0
    private var recordWidth = 0
    private var recordHeight = 0
    private val audioRecorder = FFAudioRecorder().apply {
        setOnRecordCallback(this@FFMediaRecordViewModel)
        setSampleFormat(AudioFormat.ENCODING_PCM_16BIT)
    }
    private var mediaRecorder: FFMediaRecorder? = null
    private var isRecording = false
    private val handler = Handler(Looper.getMainLooper())
    private val videoList = ArrayList<VideoInfo>()

    private val cameraController: ICameraController = if (CameraApi.hasCamera2(activity)) {
        CameraXController(activity)
    } else {
        CameraController(activity)
    }

    init {
        cameraController.setPreviewCallback(this)
        cameraController.setOnFrameAvailableListener(this)
        cameraController.setOnSurfaceTextureListener(this)
    }

    fun onResume() { openCamera() }

    fun onPause() { closeCamera() }

    fun release() { commandEditor.release() }

    fun switchCamera() { cameraController.switchCamera() }

    fun setRecordSeconds(seconds: Int) {
        maxDuration = seconds * 1000
        remainDuration = maxDuration
    }

    fun setAudioEnable(enable: Boolean) { Log.d(TAG, "setAudioEnable: $enable") }

    fun startRecord() {
        if (isRecording) {
            Log.e(TAG, "startRecord: recording state is error")
            return
        }
        mediaRecorder?.release()
        previewRotate = cameraController.orientation
        mediaRecorder = FFMediaRecorder.RecordBuilder(generateOutputPath())
            .setVideoParams(recordWidth, recordHeight, AVFormatter.PIXEL_FORMAT_NV21, 25)
            .setRotate(if (cameraController.isFront) 360 - previewRotate else previewRotate)
            .setMirror(cameraController.isFront)
            .setAudioParams(audioRecorder.sampleRate, AVFormatter.getSampleFormat(audioRecorder.sampleFormat), audioRecorder.channels)
            .create().apply { setRecordListener(this@FFMediaRecordViewModel) }
        mediaRecorder?.startRecord()
        audioRecorder.start()
    }

    fun stopRecord() {
        if (isRecording) isRecording = false
        mediaRecorder?.stopRecord()
        audioRecorder.stop()
    }

    override fun onRecordFinish() { Log.d(TAG, "onRecordFinish: audio record finish") }

    override fun onRecordSample(data: ByteArray) {
        if (isRecording) {
            mediaRecorder?.recordAudioFrame(data, data.size)
        }
    }

    fun isRecording(): Boolean = isRecording

    override fun onRecordStart() {
        _uiState.value = _uiState.value.copy(showViews = false)
        isRecording = true
    }

    override fun onRecording(duration: Float) {
        val progress = duration / maxDuration
        _uiState.value = _uiState.value.copy(progress = progress)
        if (duration > remainDuration) stopRecord()
    }

    override fun onRecordFinish(success: Boolean, duration: Float) {
        isRecording = false
        mediaRecorder?.let {
            if (success) {
                videoList.add(VideoInfo(it.output, duration))
                remainDuration -= duration.toInt()
                val currentProgress = duration / maxDuration
                val segs = _uiState.value.progressSegments.toMutableList()
                segs.add(currentProgress)
                _uiState.value = _uiState.value.copy(progressSegments = segs)
            }
        }
        _uiState.value = _uiState.value.copy(showViews = true, toast = "录制成功")
    }

    override fun onRecordError(msg: String) {
        _uiState.value = _uiState.value.copy(toast = msg)
        mediaRecorder?.release()
        mediaRecorder = null
    }

    override fun onSurfaceTexturePrepared(surfaceTexture: SurfaceTexture) {
        _uiState.value = _uiState.value.copy(surfaceTexture = surfaceTexture)
    }

    override fun onFrameAvailable(surfaceTexture: SurfaceTexture) {
        _uiState.value = _uiState.value.copy(frameAvailable = !_uiState.value.frameAvailable)
    }

    override fun onPreviewFrame(data: ByteArray) {
        if (mediaRecorder != null && isRecording) {
            handler.post {
                mediaRecorder?.recordVideoFrame(data, data.size, recordWidth, recordHeight, AVFormatter.PIXEL_FORMAT_NV21)
            }
        }
    }

    fun deleteLastVideo() {
        val index = videoList.size - 1
        if (index >= 0) {
            val info = videoList[index]
            val path = info.fileName
            remainDuration += info.duration.toInt()
            if (!path.isNullOrEmpty()) {
                FileUtils.deleteFile(path)
                videoList.removeAt(index)
            }
        }
        val segs = _uiState.value.progressSegments.toMutableList()
        if (segs.isNotEmpty()) segs.removeAt(segs.size - 1)
        _uiState.value = _uiState.value.copy(progressSegments = segs, showViews = true)
    }

    private fun openCamera() {
        cameraController.openCamera()
        calculateImageSize()
    }

    private fun calculateImageSize() {
        previewRotate = cameraController.orientation
        recordWidth = cameraController.previewWidth
        recordHeight = cameraController.previewHeight
        val width: Int
        val height: Int
        if (previewRotate == 90 || previewRotate == 270) {
            width = recordHeight
            height = recordWidth
        } else {
            width = recordWidth
            height = recordHeight
        }
        _uiState.value = _uiState.value.copy(textureSize = width to height)
    }

    private fun closeCamera() { cameraController.closeCamera() }

    fun mergeAndEdit() {
        if (videoList.isEmpty()) return
        if (videoList.size == 1) {
            val path = videoList[0].fileName
            val outputPath = generateOutputPath()
            FileUtils.copyFile(path, outputPath)
            val intent = Intent(activity, VideoEditActivity::class.java)
            intent.putExtra(VideoEditActivity.VIDEO_PATH, outputPath)
            activity.startActivity(intent)
        } else {
            _uiState.value = _uiState.value.copy(showDialog = true)
            val videos = videoList.mapNotNull { it.fileName }
            val finalPath = generateOutputPath()
            commandEditor.execCommand(CainCommandEditor.concatVideo(activity, videos, finalPath)) { result ->
                _uiState.value = _uiState.value.copy(showDialog = false)
                if (result == 0) {
                    val intent = Intent(activity, VideoEditActivity::class.java)
                    intent.putExtra(VideoEditActivity.VIDEO_PATH, finalPath)
                    activity.startActivity(intent)
                } else {
                    _uiState.value = _uiState.value.copy(toast = "合成失败")
                }
            }
        }
    }

    fun getActivity(): Activity = activity

    val recordVideoSize: Int
        get() = videoList.size

    fun generateOutputPath(): String = PathConstraints.getVideoCachePath(activity)

    data class VideoInfo(val fileName: String, val duration: Float)

    companion object { private const val TAG = "FFMediaRecordVM" }
}
