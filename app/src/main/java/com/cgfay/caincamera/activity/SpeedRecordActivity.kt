package com.cgfay.caincamera.activity

import android.app.Dialog
import android.app.ProgressDialog
import android.graphics.SurfaceTexture
import android.opengl.GLSurfaceView
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.viewinterop.AndroidView
import com.cgfay.uitls.ui.CombineVideoDialog
import com.cgfay.caincamera.R
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.Factory
import com.cgfay.caincamera.viewmodel.RecordViewModel
import com.cgfay.caincamera.renderer.RecordRenderer
import com.cgfay.caincamera.widget.GLRecordView
import com.cgfay.camera.widget.RecordButton
import com.cgfay.camera.widget.RecordProgressView
import com.cgfay.camera.widget.RecordSpeedLevelBar
import com.cgfay.camera.widget.RecordSpeed
import com.cgfay.filter.glfilter.color.bean.DynamicColor
import com.cgfay.filter.glfilter.resource.FilterHelper
import com.cgfay.filter.glfilter.resource.ResourceJsonCodec
import com.cgfay.media.recorder.SpeedMode
import com.cgfay.uitls.utils.NotchUtils
import com.cgfay.uitls.utils.PermissionUtils
import com.cgfay.uitls.utils.StatusBarUtils
import java.io.File

/**
 * Kotlin version of SpeedRecordActivity using Jetpack Compose.
 */
class SpeedRecordActivity : BaseRecordActivity(), View.OnClickListener {

    private lateinit var glRecordView: GLRecordView
    private lateinit var recordButton: RecordButton

    private lateinit var renderer: RecordRenderer
    private lateinit var viewModel: RecordViewModel

    private lateinit var btnSwitch: View
    private lateinit var btnNext: Button
    private lateinit var btnDelete: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return RecordViewModel(this@SpeedRecordActivity) as T
            }
        }
        viewModel = ViewModelProvider(this, factory)[RecordViewModel::class.java]
        viewModel.setRecordSeconds(15)
        renderer = RecordRenderer(viewModel)
        setContent {
            SpeedRecordScreen(this, viewModel, renderer)
        }
    }

    override fun onResume() {
        super.onResume()
        handleFullScreen()
        glRecordView.onResume()
        viewModel.onResume()
        viewModel.setAudioEnable(
            PermissionUtils.permissionChecking(this, android.Manifest.permission.RECORD_AUDIO)
        )
    }

    override fun onPause() {
        super.onPause()
        glRecordView.onPause()
        viewModel.onPause()
        renderer.clear()
    }

    override fun onDestroy() {
        viewModel.release()
        super.onDestroy()
    }

    private fun handleFullScreen() {
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN
        )
        if (NotchUtils.hasNotchScreen(this)) {
            window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            val lp = window.attributes
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                lp.layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
            window.attributes = lp
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btn_switch -> viewModel.switchCamera()
            R.id.btn_delete -> viewModel.deleteLastVideo()
            R.id.btn_next -> viewModel.mergeAndEdit()
        }
    }

    override fun hideViews() {
        runOnUiThread {
            btnDelete.visibility = View.GONE
            btnNext.visibility = View.GONE
            btnSwitch.visibility = View.GONE
        }
    }

    override fun showViews() {
        runOnUiThread {
            val showEditEnable = viewModel.recordVideoSize > 0
            btnDelete.visibility = if (showEditEnable) View.VISIBLE else View.GONE
            btnNext.visibility = if (showEditEnable) View.VISIBLE else View.GONE
            btnSwitch.visibility = View.VISIBLE
            recordButton.reset()
        }
    }

    override fun setRecordProgress(progress: Float) {
        // progress handled by viewModel
    }

    override fun addProgressSegment(progress: Float) {
        // handled by viewModel
    }

    override fun deleteProgressSegment() {
        // handled by viewModel
    }

    override fun bindSurfaceTexture(surfaceTexture: SurfaceTexture) {
        glRecordView.queueEvent { renderer.bindSurfaceTexture(surfaceTexture) }
    }

    override fun updateTextureSize(width: Int, height: Int) {
        renderer.setTextureSize(width, height)
    }

    override fun onFrameAvailable() {
        glRecordView.requestRender()
    }

    private var progressDialog: Dialog? = null
    override fun showProgressDialog() {
        runOnUiThread { progressDialog = ProgressDialog.show(this, "正在合成", "正在合成") }
    }

    override fun hideProgressDialog() {
        runOnUiThread {
            progressDialog?.dismiss()
            progressDialog = null
        }
    }

    private var toast: Toast? = null
    override fun showToast(tips: String) {
        runOnUiThread {
            toast?.cancel()
            toast = Toast.makeText(this, tips, Toast.LENGTH_SHORT)
            toast?.show()
        }
    }

    private var filterIndex = 0
    private val touchScroller = object : GLRecordView.OnTouchScroller {
        override fun swipeBack() {
            filterIndex++
            if (filterIndex >= FilterHelper.getFilterList().size) {
                filterIndex = 0
            }
            changeDynamicFilter(filterIndex)
        }

        override fun swipeFrontal() {
            filterIndex--
            if (filterIndex < 0) {
                val count = FilterHelper.getFilterList().size
                filterIndex = if (count > 0) count - 1 else 0
            }
            changeDynamicFilter(filterIndex)
        }

        override fun swipeUpper(startInLeft: Boolean, distance: Float) {}
        override fun swipeDown(startInLeft: Boolean, distance: Float) {}
    }

    fun changeDynamicFilter(filterIndex: Int) {
        glRecordView.queueEvent {
            val folderPath = FilterHelper.getFilterDirectory(this) + File.separator +
                    FilterHelper.getFilterList()[filterIndex].unzipFolder
            var color: DynamicColor? = null
            if (!FilterHelper.getFilterList()[filterIndex].unzipFolder.equals("none", true)) {
                try {
                    color = ResourceJsonCodec.decodeFilterData(folderPath)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            renderer.changeDynamicFilter(this, color)
        }
    }
}

@Composable
fun SpeedRecordScreen(
    activity: SpeedRecordActivity,
    viewModel: RecordViewModel,
    renderer: RecordRenderer
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val uiState by viewModel.uiState.collectAsState()

    val glRecordView = remember {
        GLRecordView(context).apply {
            setEGLContextClientVersion(3)
            setRenderer(renderer)
            setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY)
            addOnTouchScroller(activity.touchScroller)
        }.also { activity.glRecordView = it }
    }

    var speed by remember { mutableStateOf(RecordSpeed.SPEED_L2) }

    val recordButton = remember {
        RecordButton(context).apply {
            addRecordStateListener(object : RecordButton.RecordStateListener {
                override fun onRecordStart() { viewModel.startRecord() }
                override fun onRecordStop() { viewModel.stopRecord() }
                override fun onZoom(percent: Float) {}
            })
        }.also { activity.recordButton = it }
    }

    val btnSwitch = remember {
        LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            val size = context.resources.getDimensionPixelSize(R.dimen.top_button_width_height)
            addView(ImageView(context).apply {
                setBackgroundResource(R.drawable.ic_camera_switch_camera_light)
                layoutParams = LinearLayout.LayoutParams(size, size)
            })
            addView(TextView(context).apply {
                setTextColor(context.getColor(R.color.white))
                text = "翻转"
            })
            setOnClickListener(activity)
        }.also { activity.btnSwitch = it }
    }

    val btnDelete = remember {
        Button(context).apply {
            setBackgroundResource(R.drawable.ic_camera_record_delete)
            setOnClickListener(activity)
        }.also { activity.btnDelete = it }
    }

    val btnNext = remember {
        Button(context).apply {
            setBackgroundResource(R.drawable.bg_record_next_button)
            setText(R.string.btn_next)
            setTextColor(context.getColor(R.color.white))
            setOnClickListener(activity)
        }.also { activity.btnNext = it }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        AndroidView(
            factory = { glRecordView },
            update = {
                uiState.surfaceTexture?.let { surface ->
                    it.queueEvent { renderer.bindSurfaceTexture(surface) }
                }
                uiState.textureSize?.let { size ->
                    renderer.setTextureSize(size.first, size.second)
                }
                if (uiState.frameAvailable) it.requestRender()
            },
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(9f / 16f)
                .align(Alignment.TopCenter)
        )

        RecordProgressView(
            progress = uiState.progress,
            progressSegments = uiState.progressSegments,
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = dimensionResource(R.dimen.dp6),
                    end = dimensionResource(R.dimen.dp6),
                    top = dimensionResource(R.dimen.dp6)
                )
                .height(dimensionResource(R.dimen.dp6))
                .align(Alignment.TopCenter)
        )

        AndroidView(
            factory = { btnSwitch },
            update = { view ->
                view.visibility = if (uiState.showViews) View.VISIBLE else View.GONE
            },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(
                    top = dimensionResource(R.dimen.top_button_margin),
                    end = dimensionResource(R.dimen.top_button_margin)
                )
        )

        if (uiState.showViews) {
            RecordSpeedLevelBar(
                currentSpeed = speed,
                onSpeedChanged = {
                    speed = it
                    viewModel.setSpeedMode(SpeedMode.valueOf(it.speed))
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(
                        start = dimensionResource(R.dimen.dp50),
                        end = dimensionResource(R.dimen.dp50),
                        bottom = dimensionResource(R.dimen.dp20)
                    )
            )
        }

        AndroidView(
            factory = { recordButton },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .size(dimensionResource(R.dimen.dp120))
        )

        AndroidView(
            factory = { btnDelete },
            update = { view ->
                val show = uiState.showViews && viewModel.recordVideoSize > 0
                view.visibility = if (show) View.VISIBLE else View.GONE
            },
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(end = dimensionResource(R.dimen.dp20))
        )

        AndroidView(
            factory = { btnNext },
            update = { view ->
                val show = uiState.showViews && viewModel.recordVideoSize > 0
                view.visibility = if (show) View.VISIBLE else View.GONE
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
        )

        if (NotchUtils.hasNotchScreen(activity)) {
            val heightPx = StatusBarUtils.getStatusBarHeight(activity)
            Spacer(modifier = Modifier
                .align(Alignment.TopStart)
                .fillMaxWidth()
                .padding(top = with(density) { heightPx.toDp() }))
        }
        if (uiState.showDialog) {
            CombineVideoDialog(message = "正在合成", dimable = false) {}
        }
        uiState.toast?.let { msg ->
            LaunchedEffect(msg) {
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            }
        }
    }
}
