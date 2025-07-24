package com.cgfay.caincamera.viewmodel

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.SurfaceTexture
import android.opengl.EGLContext
import android.os.Environment
import androidx.annotation.NonNull
import androidx.lifecycle.ViewModel
import com.cgfay.caincamera.activity.BaseRecordActivity
import com.cgfay.camera.camera.CameraApi
import com.cgfay.camera.camera.CameraController
import com.cgfay.camera.camera.CameraXController
import com.cgfay.camera.camera.ICameraController
import com.cgfay.camera.camera.OnFrameAvailableListener
import com.cgfay.camera.camera.OnSurfaceTextureListener
import com.cgfay.camera.utils.PathConstraints
import com.cgfay.media.CainCommandEditor
import com.cgfay.media.recorder.AudioParams
import com.cgfay.media.recorder.HWMediaRecorder
import com.cgfay.media.recorder.MediaInfo
import com.cgfay.media.recorder.MediaType
import com.cgfay.media.recorder.OnRecordStateListener
import com.cgfay.media.recorder.RecordInfo
import com.cgfay.media.recorder.SpeedMode
import com.cgfay.media.recorder.VideoParams
import com.cgfay.uitls.utils.FileUtils
import com.cgfay.video.activity.VideoEditActivity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.util.ArrayList

class RecordViewModel(private val activity: BaseRecordActivity) : ViewModel(),
    OnSurfaceTextureListener,
    OnFrameAvailableListener,
    OnRecordStateListener {

    private val _uiState = MutableStateFlow(RecordUiState())
    val uiState: StateFlow<RecordUiState> = _uiState.asStateFlow()

    private val videoParams = VideoParams().apply {
        videoPath = getVideoTempPath(activity)
    }
    private val audioParams = AudioParams().apply {
        audioPath = getAudioTempPath(activity)
    }

    private var operateStarted = false
    private var currentProgress = 0f
    private var maxDuration: Long = 0
    private var remainDuration: Long = 0

    private val hwMediaRecorder = HWMediaRecorder(this)
    private val videoList = mutableListOf<MediaInfo>()
    private var audioInfo: RecordInfo? = null
    private var videoInfo: RecordInfo? = null
    private val commandEditor = CainCommandEditor()

    private val cameraController: ICameraController = if (CameraApi.hasCamera2(activity)) {
        CameraXController(activity)
    } else {
        CameraController(activity)
    }

    init {
        cameraController.setOnFrameAvailableListener(this)
        cameraController.setOnSurfaceTextureListener(this)
    }

    fun onResume() { openCamera() }

    fun onPause() { closeCamera() }

    fun switchCamera() { cameraController.switchCamera() }

    fun release() {
        hwMediaRecorder.release()
        commandEditor.release()
    }

    fun setSpeedMode(mode: SpeedMode) {
        videoParams.speedMode = mode
        audioParams.speedMode = mode
    }

    fun setRecordSeconds(seconds: Int) {
        maxDuration = seconds * HWMediaRecorder.SECOND_IN_US
        remainDuration = maxDuration
        videoParams.maxDuration = maxDuration
        audioParams.maxDuration = maxDuration
    }

    fun setAudioEnable(enable: Boolean) { hwMediaRecorder.setEnableAudio(enable) }

    fun startRecord() {
        if (operateStarted) return
        hwMediaRecorder.startRecord(videoParams, audioParams)
        operateStarted = true
    }

    fun stopRecord() {
        if (!operateStarted) return
        operateStarted = false
        hwMediaRecorder.stopRecord()
    }

    override fun onRecordStart() {
        _uiState.value = _uiState.value.copy(showViews = false)
    }

    override fun onRecording(duration: Long) {
        val progress = duration.toFloat() / videoParams.maxDuration
        _uiState.value = _uiState.value.copy(progress = progress)
        if (duration > remainDuration) stopRecord()
    }

    override fun onRecordFinish(info: RecordInfo) {
        if (info.type == MediaType.AUDIO) {
            audioInfo = info
        } else if (info.type == MediaType.VIDEO) {
            videoInfo = info
            currentProgress = info.duration.toFloat() / videoParams.maxDuration
        }
        if (hwMediaRecorder.enableAudio() && (audioInfo == null || videoInfo == null)) {
            _uiState.value = _uiState.value.copy(showViews = true)
            return
        }
        if (hwMediaRecorder.enableAudio()) {
            val currentFile = generateOutputPath()
            FileUtils.createFile(currentFile)
            commandEditor.execCommand(
                CainCommandEditor.mergeAudioVideo(videoInfo!!.fileName, audioInfo!!.fileName, currentFile)
            ) { result ->
                if (result == 0) {
                    videoList.add(MediaInfo(currentFile, videoInfo!!.duration))
                    remainDuration -= videoInfo!!.duration
                    val segs = _uiState.value.progressSegments.toMutableList()
                    segs.add(currentProgress)
                    _uiState.value = _uiState.value.copy(showViews = true, progressSegments = segs)
                    currentProgress = 0f
                }
                FileUtils.deleteFile(audioInfo!!.fileName)
                FileUtils.deleteFile(videoInfo!!.fileName)
                audioInfo = null
                videoInfo = null
                if (remainDuration <= 0) mergeAndEdit()
            }
        } else {
            videoInfo?.let {
                val currentFile = generateOutputPath()
                FileUtils.moveFile(it.fileName, currentFile)
                videoList.add(MediaInfo(currentFile, it.duration))
                remainDuration -= it.duration
                audioInfo = null
                videoInfo = null
                val segs = _uiState.value.progressSegments.toMutableList()
                segs.add(currentProgress)
                _uiState.value = _uiState.value.copy(showViews = true, progressSegments = segs)
                currentProgress = 0f
            }
        }
        operateStarted = false
    }

    fun onBindSharedContext(context: EGLContext) {
        videoParams.eglContext = context
    }

    fun onRecordFrameAvailable(texture: Int, timestamp: Long) {
        if (operateStarted && hwMediaRecorder.isRecording) {
            hwMediaRecorder.frameAvailable(texture, timestamp)
        }
    }

    override fun onSurfaceTexturePrepared(surfaceTexture: SurfaceTexture) {
        _uiState.value = _uiState.value.copy(surfaceTexture = surfaceTexture)
    }

    override fun onFrameAvailable(surfaceTexture: SurfaceTexture) {
        _uiState.value = _uiState.value.copy(frameAvailable = !_uiState.value.frameAvailable)
    }

    fun deleteLastVideo() {
        val index = videoList.size - 1
        if (index >= 0) {
            val mediaInfo = videoList[index]
            val path = mediaInfo.fileName
            remainDuration += mediaInfo.duration
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
        cameraController.setFront(false)
        cameraController.openCamera()
        calculateImageSize()
    }

    private fun calculateImageSize() {
        val width: Int
        val height: Int
        if (cameraController.orientation == 90 || cameraController.orientation == 270) {
            width = cameraController.previewHeight
            height = cameraController.previewWidth
        } else {
            width = cameraController.previewWidth
            height = cameraController.previewHeight
        }
        videoParams.setVideoSize(width, height)
        _uiState.value = _uiState.value.copy(textureSize = width to height)
    }

    private fun closeCamera() {
        cameraController.closeCamera()
    }

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

    companion object {
        private fun getAudioTempPath(context: Context): String {
            val directoryPath: String = if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()) {
                context.externalCacheDir!!.absolutePath
            } else {
                context.cacheDir.absolutePath
            }
            val path = directoryPath + File.separator + "temp.aac"
            val file = File(path)
            if (!file.parentFile.exists()) {
                file.parentFile.mkdirs()
            }
            return path
        }

        private fun getVideoTempPath(context: Context): String {
            val directoryPath: String = if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState() && context.externalCacheDir != null) {
                context.externalCacheDir!!.absolutePath
            } else {
                context.cacheDir.absolutePath
            }
            val path = directoryPath + File.separator + "temp.mp4"
            val file = File(path)
            if (!file.parentFile.exists()) {
                file.parentFile.mkdirs()
            }
            return path
        }
    }
}
