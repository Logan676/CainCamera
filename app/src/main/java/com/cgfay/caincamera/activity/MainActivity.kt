package com.cgfay.caincamera.activity

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.NonNull
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.cgfay.caincamera.R
import com.cgfay.camera.PreviewEngine
import com.cgfay.camera.fragment.NormalMediaSelector
import com.cgfay.camera.model.AspectRatio
import com.cgfay.camera.listener.OnPreviewCaptureListener
import com.cgfay.filter.glfilter.resource.FilterHelper
import com.cgfay.filter.glfilter.resource.MakeupHelper
import com.cgfay.filter.glfilter.resource.ResourceHelper
import com.cgfay.image.activity.ImageEditActivity
import com.cgfay.picker.MediaPicker
import com.cgfay.picker.model.MediaData
import com.cgfay.picker.selector.OnMediaSelector
import com.cgfay.picker.utils.MediaMetadataUtils
import com.cgfay.uitls.utils.PermissionUtils
import com.cgfay.video.activity.VideoEditActivity
import com.cgfay.caincamera.activity.SpeedRecordActivity
import com.cgfay.caincamera.activity.MusicMergeActivity
import com.cgfay.caincamera.activity.MusicPlayerActivity
import com.cgfay.caincamera.activity.VideoPlayerActivity
import com.cgfay.caincamera.activity.DuetRecordActivity
import com.cgfay.caincamera.ui.FFMediaRecordScreen

class MainActivity : ComponentActivity() {
    private var mOnClick = false
    private val mHandler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkPermissions()
        if (PermissionUtils.permissionChecking(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            initResources()
        }
        setContent { MainNavGraph() }
    }

    private fun onDebounced(action: () -> Unit) {
        if (mOnClick) return
        mOnClick = true
        mHandler.postDelayed({ mOnClick = false }, DELAY_CLICK.toLong())
        action()
    }

    private fun checkPermissions() {
        PermissionUtils.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO
            ),
            REQUEST_CODE
        )
    }

    override fun onResume() {
        super.onResume()
        mOnClick = false
    }

    override fun onRequestPermissionsResult(requestCode: Int, @NonNull permissions: Array<String>, @NonNull grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE) {
            if (grantResults.size > 1 && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                initResources()
            }
        }
    }

    /** 初始化动态贴纸、滤镜等资源 */
    private fun initResources() {
        Thread {
            ResourceHelper.initAssetsResource(this)
            FilterHelper.initAssetsFilter(this)
            MakeupHelper.initAssetsMakeup(this)
        }.start()
    }

    /** 打开预览页面 */
    fun previewCamera() {
        if (PermissionUtils.permissionChecking(this, Manifest.permission.CAMERA)) {
            PreviewEngine.from(this)
                .setCameraRatio(AspectRatio.Ratio_16_9)
                .showFacePoints(false)
                .showFps(true)
                .backCamera(true)
                .setPreviewCaptureListener { path, type ->
                    if (type == OnPreviewCaptureListener.MediaTypePicture) {
                        val intent = Intent(this, ImageEditActivity::class.java)
                        intent.putExtra(ImageEditActivity.IMAGE_PATH, path)
                        intent.putExtra(ImageEditActivity.DELETE_INPUT_FILE, true)
                        startActivity(intent)
                    } else if (type == OnPreviewCaptureListener.MediaTypeVideo) {
                        val intent = Intent(this, VideoEditActivity::class.java)
                        intent.putExtra(VideoEditActivity.VIDEO_PATH, path)
                        startActivity(intent)
                    }
                }
                .startPreview()
        } else {
            checkPermissions()
        }
    }

    /** 扫描媒体库 */
    fun scanMedia(enableImage: Boolean, enableVideo: Boolean) {
        MediaPicker.from(this)
            .showImage(enableImage)
            .showVideo(enableVideo)
            .setMediaSelector(NormalMediaSelector())
            .show()
    }

    /** 音视频混合 */
    fun musicMerge() {
        MediaPicker.from(this)
            .showCapture(true)
            .showImage(false)
            .showVideo(true)
            .setMediaSelector(MusicMergeMediaSelector())
            .show()
    }

    private inner class MusicMergeMediaSelector : OnMediaSelector {
        override fun onMediaSelect(context: Context, mediaDataList: List<MediaData>) {
            if (mediaDataList.size == 1) {
                val intent = Intent(context, MusicMergeActivity::class.java)
                intent.putExtra(MusicMergeActivity.PATH, MediaMetadataUtils.getPath(context, mediaDataList[0].contentUri))
                context.startActivity(intent)
            }
        }
    }


    fun musicPlayerTest() {
        startActivity(Intent(this, MusicPlayerActivity::class.java))
    }

    fun videoPlayerTest() {
        MediaPicker.from(this)
            .showCapture(true)
            .showImage(false)
            .showVideo(true)
            .setMediaSelector(VideoPlayerTestSelector())
            .show()
    }

    private class VideoPlayerTestSelector : OnMediaSelector {
        override fun onMediaSelect(context: Context, mediaDataList: List<MediaData>) {
            if (mediaDataList.size == 1) {
                val intent = Intent(context, VideoPlayerActivity::class.java)
                intent.putExtra(VideoPlayerActivity.PATH, MediaMetadataUtils.getPath(context, mediaDataList[0].contentUri))
                context.startActivity(intent)
            }
        }
    }

    fun duetRecord() {
        MediaPicker.from(this)
            .showImage(false)
            .showVideo(true)
            .setMediaSelector { context, mediaDataList ->
                if (mediaDataList.isNotEmpty()) {
                    onDuetRecord(mediaDataList[0])
                }
            }
            .show()
    }

    /** 模拟同框录制 */
    fun onDuetRecord(mediaData: MediaData) {
        val intent = Intent(this, DuetRecordActivity::class.java)
        intent.putExtra(DuetRecordActivity.DUET_MEDIA, mediaData)
        startActivity(intent)
    }

    companion object {
        private const val REQUEST_CODE = 0
        private const val DELAY_CLICK = 500
    }
}

@Composable
fun MainActivity.MainNavGraph() {
    val navController = rememberNavController()
    NavHost(navController, startDestination = "main") {
        composable("main") {
            MainScreen(
                onCamera = { previewCamera() },
                onEditVideo = { scanMedia(false, true) },
                onEditPicture = { scanMedia(true, false) },
                onSpeedRecord = { startActivity(Intent(this@MainActivity, SpeedRecordActivity::class.java)) },
                onMusicMerge = { musicMerge() },
                onFFMediaRecord = { navController.navigate("ffrecord") },
                onMusicPlayer = { musicPlayerTest() },
                onVideoPlayer = { videoPlayerTest() },
                onDuetRecord = { duetRecord() }
            )
        }
        composable("ffrecord") {
            FFMediaRecordScreen { navController.popBackStack() }
        }
    }
}

@Composable
fun MainScreen(
    onCamera: () -> Unit,
    onEditVideo: () -> Unit,
    onEditPicture: () -> Unit,
    onSpeedRecord: () -> Unit,
    onMusicMerge: () -> Unit,
    onFFMediaRecord: () -> Unit,
    onMusicPlayer: () -> Unit,
    onVideoPlayer: () -> Unit,
    onDuetRecord: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(onClick = onCamera) { Text(text = "Camera") }
        Button(onClick = onEditVideo) { Text(text = "Edit Video") }
        Button(onClick = onEditPicture) { Text(text = "Edit Picture") }
        Button(onClick = onSpeedRecord) { Text(text = "Speed Record") }
        Button(onClick = onMusicMerge) { Text(text = "Music Merge") }
        Button(onClick = onFFMediaRecord) { Text(text = "FF Media Record") }
        Button(onClick = onMusicPlayer) { Text(text = "Music Player") }
        Button(onClick = onVideoPlayer) { Text(text = "Video Player") }
        Button(onClick = onDuetRecord) { Text(text = "Duet Record") }
    }
}
