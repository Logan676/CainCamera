package com.cgfay.camera.presenter

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.SurfaceTexture
import androidx.annotation.NonNull
import androidx.fragment.app.FragmentActivity
import android.opengl.EGLContext
import android.text.TextUtils
import android.util.Log
import com.cgfay.camera.activity.CameraSettingActivity
import com.cgfay.camera.camera.CameraController
import com.cgfay.camera.camera.CameraParam
import com.cgfay.camera.camera.ICameraController
import com.cgfay.camera.camera.OnFrameAvailableListener
import com.cgfay.camera.camera.OnSurfaceTextureListener
import com.cgfay.camera.camera.PreviewCallback
import com.cgfay.camera.listener.OnCaptureListener
import com.cgfay.camera.listener.OnFpsListener
import com.cgfay.camera.listener.OnPreviewCaptureListener
import com.cgfay.camera.render.CameraRenderer
import com.cgfay.camera.utils.PathConstraints
import com.cgfay.facedetect.engine.FaceTracker
import com.cgfay.facedetect.listener.FaceTrackerCallback
import com.cgfay.filter.glfilter.color.bean.DynamicColor
import com.cgfay.filter.glfilter.makeup.bean.DynamicMakeup
import com.cgfay.filter.glfilter.resource.FilterHelper
import com.cgfay.filter.glfilter.resource.ResourceHelper
import com.cgfay.filter.glfilter.resource.ResourceJsonCodec
import com.cgfay.filter.glfilter.resource.bean.ResourceData
import com.cgfay.filter.glfilter.resource.bean.ResourceType
import com.cgfay.filter.glfilter.stickers.bean.DynamicSticker
import com.cgfay.media.recorder.AudioParams
import com.cgfay.media.recorder.HWMediaRecorder
import com.cgfay.media.recorder.MediaInfo
import com.cgfay.media.recorder.MediaType
import com.cgfay.media.recorder.OnRecordStateListener
import com.cgfay.media.recorder.RecordInfo
import com.cgfay.media.recorder.SpeedMode
import com.cgfay.media.recorder.VideoParams
import com.cgfay.landmark.LandmarkEngine
import com.cgfay.media.CainCommandEditor
import com.cgfay.uitls.utils.BitmapUtils
import com.cgfay.uitls.utils.BrightnessUtils
import com.cgfay.uitls.utils.FileUtils
import com.cgfay.video.activity.VideoEditActivity
import java.io.File
import java.util.ArrayList
import java.util.List

/**
 * Kotlin version of the preview presenter used by Compose screens.
 */
class CameraPreviewPresenter(target: CameraPreviewView) :
    PreviewPresenter<CameraPreviewView>(target), PreviewCallback, FaceTrackerCallback,
    OnCaptureListener, OnFpsListener, OnSurfaceTextureListener, OnFrameAvailableListener,
    OnRecordStateListener {

    private var mFilterIndex = 0
    private val mCameraParam: CameraParam = CameraParam.getInstance()
    private var mActivity: FragmentActivity? = null
    private var mMusicPath: String? = null
    private val mVideoParams = VideoParams()
    private val mAudioParams = AudioParams()
    private var mOperateStarted = false
    private var mCurrentProgress = 0f
    private var mMaxDuration: Long = 0
    private var mRemainDuration: Long = 0
    private var mHWMediaRecorder: HWMediaRecorder? = null
    private val mVideoList: MutableList<MediaInfo> = ArrayList()
    private var mAudioInfo: RecordInfo? = null
    private var mVideoInfo: RecordInfo? = null
    private var mCommandEditor: CainCommandEditor? = CainCommandEditor()
    private lateinit var mCameraController: ICameraController
    private val mCameraRenderer: CameraRenderer = CameraRenderer(this)

    fun onAttach(activity: FragmentActivity) {
        mActivity = activity
        mVideoParams.videoPath = PathConstraints.getVideoTempPath(activity)
        mAudioParams.audioPath = PathConstraints.getAudioTempPath(activity)
        mCameraRenderer.initRenderer()
        mCameraController = CameraController(activity)
        mCameraController.setPreviewCallback(this)
        mCameraController.setOnFrameAvailableListener(this)
        mCameraController.setOnSurfaceTextureListener(this)
        mCameraParam.brightness = if (BrightnessUtils.getSystemBrightnessMode(activity) == 1) {
            -1
        } else {
            BrightnessUtils.getSystemBrightness(activity)
        }
        FaceTracker.getInstance()
            .setFaceCallback(this)
            .previewTrack(true)
            .initTracker()
    }

    fun onDetach() {
        mActivity = null
        mCameraRenderer.destroyRenderer()
    }

    override fun getContext(): Context = mActivity!!

    override fun onBindSharedContext(context: EGLContext) {
        mVideoParams.eglContext = context
        Log.d(TAG, "onBindSharedContext: ")
    }

    override fun onRecordFrameAvailable(texture: Int, timestamp: Long) {
        if (mOperateStarted && mHWMediaRecorder?.isRecording == true) {
            mHWMediaRecorder?.frameAvailable(texture, timestamp)
        }
    }

    override fun onSurfaceCreated(surfaceTexture: SurfaceTexture) {
        mCameraRenderer.onSurfaceCreated(surfaceTexture)
    }

    override fun onSurfaceChanged(width: Int, height: Int) {
        mCameraRenderer.onSurfaceChanged(width, height)
    }

    override fun onSurfaceDestroyed() {
        mCameraRenderer.onSurfaceDestroyed()
    }

    override fun changeResource(resourceData: ResourceData) {
        val type = resourceData.type ?: return
        val unzipFolder = resourceData.unzipFolder
        try {
            when (type) {
                ResourceType.FILTER -> {
                    val folderPath = ResourceHelper.getResourceDirectory(mActivity!!) +
                        File.separator + unzipFolder
                    val color = ResourceJsonCodec.decodeFilterData(folderPath)
                    mCameraRenderer.changeResource(color)
                }
                ResourceType.STICKER -> {
                    val folderPath = ResourceHelper.getResourceDirectory(mActivity!!) +
                        File.separator + unzipFolder
                    val sticker = ResourceJsonCodec.decodeStickerData(folderPath)
                    mCameraRenderer.changeResource(sticker)
                }
                ResourceType.MULTI -> {
                }
                ResourceType.NONE -> mCameraRenderer.changeResource(null as DynamicSticker?)
            }
        } catch (e: Exception) {
            Log.e(TAG, "parseResource: ", e)
        }
    }

    override fun changeDynamicFilter(color: DynamicColor?) {
        mCameraRenderer.changeFilter(color)
    }

    override fun changeDynamicMakeup(makeup: DynamicMakeup?) {
        mCameraRenderer.changeMakeup(makeup)
    }

    override fun changeDynamicFilter(filterIndex: Int) {
        val activity = mActivity ?: return
        val folderPath = FilterHelper.getFilterDirectory(activity) + File.separator +
            FilterHelper.getFilterList()[filterIndex].unzipFolder
        var color: DynamicColor? = null
        if (!FilterHelper.getFilterList()[filterIndex].unzipFolder.equals("none", ignoreCase = true)) {
            try {
                color = ResourceJsonCodec.decodeFilterData(folderPath)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        mCameraRenderer.changeFilter(color)
    }

    override fun previewFilter(): Int {
        mFilterIndex--
        if (mFilterIndex < 0) {
            val count = FilterHelper.getFilterList().size
            mFilterIndex = if (count > 0) count - 1 else 0
        }
        changeDynamicFilter(mFilterIndex)
        return mFilterIndex
    }

    override fun nextFilter(): Int {
        mFilterIndex++
        mFilterIndex %= FilterHelper.getFilterList().size
        changeDynamicFilter(mFilterIndex)
        return mFilterIndex
    }

    override fun getFilterIndex(): Int = mFilterIndex

    override fun showCompare(enable: Boolean) {
        mCameraParam.showCompare = enable
    }

    private fun openCamera() {
        mCameraController.openCamera()
        calculateImageSize()
    }

    private fun calculateImageSize() {
        val width: Int
        val height: Int
        if (mCameraController.orientation == 90 || mCameraController.orientation == 270) {
            width = mCameraController.previewHeight
            height = mCameraController.previewWidth
        } else {
            width = mCameraController.previewWidth
            height = mCameraController.previewHeight
        }
        mVideoParams.setVideoSize(width, height)
        mCameraRenderer.setTextureSize(width, height)
    }

    private fun closeCamera() {
        mCameraController.closeCamera()
    }

    override fun takePicture() {
        mCameraRenderer.takePicture()
    }

    override fun switchCamera() {
        mCameraController.switchCamera()
    }

    override fun startRecord() {
        if (mOperateStarted) return
        if (mHWMediaRecorder == null) {
            mHWMediaRecorder = HWMediaRecorder(this)
        }
        mHWMediaRecorder?.startRecord(mVideoParams, mAudioParams)
        mOperateStarted = true
    }

    override fun stopRecord() {
        if (!mOperateStarted) return
        mOperateStarted = false
        mHWMediaRecorder?.stopRecord()
    }

    override fun cancelRecord() {
        stopRecord()
    }

    override fun isRecording(): Boolean {
        return mOperateStarted && mHWMediaRecorder?.isRecording == true
    }

    override fun setRecordAudioEnable(enable: Boolean) {
        mHWMediaRecorder?.setEnableAudio(enable)
    }

    override fun setRecordSeconds(seconds: Int) {
        mMaxDuration = seconds * HWMediaRecorder.SECOND_IN_US
        mRemainDuration = mMaxDuration
        mVideoParams.maxDuration = mMaxDuration
        mAudioParams.maxDuration = mMaxDuration
    }

    override fun setSpeedMode(mode: SpeedMode) {
        mVideoParams.setSpeedMode(mode)
        mAudioParams.setSpeedMode(mode)
    }

    override fun deleteLastVideo() {
        val index = mVideoList.size - 1
        if (index >= 0) {
            val mediaInfo = mVideoList[index]
            val path = mediaInfo.fileName
            mRemainDuration += mediaInfo.duration
            if (!TextUtils.isEmpty(path)) {
                FileUtils.deleteFile(path)
                mVideoList.removeAt(index)
            }
        }
        getTarget().deleteProgressSegment()
    }

    override fun getRecordedVideoSize(): Int = mVideoList.size

    override fun changeFlashLight(on: Boolean) {
        mCameraController.setFlashLight(on)
    }

    override fun enableEdgeBlurFilter(enable: Boolean) {
        mCameraRenderer.changeEdgeBlur(enable)
    }

    override fun setMusicPath(path: String?) {
        mMusicPath = path
    }

    override fun onOpenCameraSettingPage() {
        mActivity?.let {
            val intent = Intent(it, CameraSettingActivity::class.java)
            it.startActivity(intent)
        }
    }

    fun onCameraOpened() {
        Log.d(TAG, "onCameraOpened: orientation - " + mCameraController.orientation +
                " width - " + mCameraController.previewWidth +
                ", height - " + mCameraController.previewHeight)
        FaceTracker.getInstance()
            .setBackCamera(!mCameraController.isFront)
            .prepareFaceTracker(
                mActivity,
                mCameraController.orientation,
                mCameraController.previewWidth,
                mCameraController.previewHeight
            )
    }

    override fun onSurfaceTexturePrepared(@NonNull surfaceTexture: SurfaceTexture) {
        onCameraOpened()
        mCameraRenderer.bindInputSurfaceTexture(surfaceTexture)
    }

    override fun onPreviewFrame(data: ByteArray) {
        Log.d(TAG, "onPreviewFrame: width - " + mCameraController.previewWidth +
                ", height - " + mCameraController.previewHeight)
        FaceTracker.getInstance()
            .trackFace(data, mCameraController.previewWidth, mCameraController.previewHeight)
    }

    override fun onFaceDetectCallback(points: FloatArray?, isOpenEyes: Boolean, pitch: Float, yaw: Float, roll: Float, time: Long) {}

    override fun onSurfaceTextureUpdated(texture: Int, timestamp: Long) {}

    override fun onRecordStart() {
        getTarget().hideOnRecording()
    }

    override fun onRecording(duration: Long) {
        val progress = duration.toFloat() / mVideoParams.maxDuration
        getTarget().updateRecordProgress(progress)
        if (duration > mRemainDuration) {
            stopRecord()
        }
    }

    override fun onRecordFinish(info: RecordInfo) {
        if (info.type == MediaType.AUDIO) {
            mAudioInfo = info
        } else if (info.type == MediaType.VIDEO) {
            mVideoInfo = info
            mCurrentProgress = info.duration.toFloat() / mVideoParams.maxDuration
        }
        if (mHWMediaRecorder == null) return
        if (mHWMediaRecorder!!.enableAudio() && (mAudioInfo == null || mVideoInfo == null)) return
        if (mHWMediaRecorder!!.enableAudio()) {
            val currentFile = generateOutputPath()
            FileUtils.createFile(currentFile)
            mCommandEditor?.execCommand(
                CainCommandEditor.mergeAudioVideo(
                    mVideoInfo!!.fileName,
                    mAudioInfo!!.fileName,
                    currentFile
                )
            ) { result ->
                if (result == 0) {
                    mVideoList.add(MediaInfo(currentFile, mVideoInfo!!.duration))
                    mRemainDuration -= mVideoInfo!!.duration
                    getTarget().addProgressSegment(mCurrentProgress)
                    getTarget().resetAllLayout()
                    mCurrentProgress = 0f
                }
                FileUtils.deleteFile(mAudioInfo!!.fileName)
                FileUtils.deleteFile(mVideoInfo!!.fileName)
                mAudioInfo = null
                mVideoInfo = null
                if (mRemainDuration <= 0) {
                    mergeAndEdit()
                }
            }
        } else {
            if (mVideoInfo != null) {
                val currentFile = generateOutputPath()
                FileUtils.moveFile(mVideoInfo!!.fileName, currentFile)
                mVideoList.add(MediaInfo(currentFile, mVideoInfo!!.duration))
                mRemainDuration -= mVideoInfo!!.duration
                mAudioInfo = null
                mVideoInfo = null
                getTarget().addProgressSegment(mCurrentProgress)
                getTarget().resetAllLayout()
                mCurrentProgress = 0f
            }
        }
        mHWMediaRecorder?.release()
        mHWMediaRecorder = null
    }

    fun mergeAndEdit() {
        if (mVideoList.isEmpty()) {
            return
        }
        if (mVideoList.size == 1) {
            val path = mVideoList[0].fileName
            val outputPath = generateOutputPath()
            FileUtils.copyFile(path, outputPath)
            val intent = Intent(mActivity, VideoEditActivity::class.java)
            intent.putExtra(VideoEditActivity.VIDEO_PATH, outputPath)
            mActivity?.startActivity(intent)
        } else {
            getTarget().showConcatProgressDialog()
            val videos: MutableList<String> = ArrayList()
            for (info in mVideoList) {
                if (!TextUtils.isEmpty(info.fileName)) {
                    videos.add(info.fileName)
                }
            }
            val finalPath = generateOutputPath()
            mCommandEditor?.execCommand(
                CainCommandEditor.concatVideo(mActivity, videos, finalPath)
            ) { result ->
                getTarget().hideConcatProgressDialog()
                if (result == 0) {
                    onOpenVideoEditPage(finalPath)
                } else {
                    getTarget().showToast("合成失败")
                }
            }
        }
    }

    fun generateOutputPath(): String = PathConstraints.getVideoCachePath(mActivity)

    fun onOpenVideoEditPage(path: String) {
        if (mCameraParam.captureListener != null) {
            mCameraParam.captureListener.onMediaSelectedListener(
                path,
                OnPreviewCaptureListener.MediaTypeVideo
            )
        }
    }

    override fun onCapture(bitmap: Bitmap) {
        val filePath = PathConstraints.getImageCachePath(mActivity)
        BitmapUtils.saveBitmap(filePath, bitmap)
        mCameraParam.captureListener?.onMediaSelectedListener(
            filePath,
            OnPreviewCaptureListener.MediaTypePicture
        )
    }

    override fun onFpsCallback(fps: Float) {
        getTarget().showFps(fps)
    }

    companion object {
        private const val TAG = "CameraPreviewPresenter"
    }
}
