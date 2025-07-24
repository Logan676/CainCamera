package com.cgfay.caincamera.activity

import android.app.Dialog
import android.app.ProgressDialog
import android.graphics.SurfaceTexture
import android.opengl.GLSurfaceView
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.cgfay.caincamera.R
import com.cgfay.caincamera.presenter.RecordPresenter
import com.cgfay.caincamera.renderer.DuetRecordRenderer
import com.cgfay.caincamera.renderer.DuetType
import com.cgfay.caincamera.widget.GLRecordView
import com.cgfay.camera.widget.RecordButton
import com.cgfay.camera.widget.RecordProgressView
import com.cgfay.camera.widget.RecordSpeedLevelBar
import com.cgfay.picker.model.MediaData
import com.cgfay.filter.glfilter.color.bean.DynamicColor
import com.cgfay.filter.glfilter.resource.FilterHelper
import com.cgfay.filter.glfilter.resource.ResourceJsonCodec
import com.cgfay.media.recorder.SpeedMode
import com.cgfay.uitls.utils.NotchUtils
import com.cgfay.uitls.utils.PermissionUtils
import com.cgfay.uitls.utils.StatusBarUtils
import java.io.File

/**
 * Kotlin version of DuetRecordActivity using Jetpack Compose.
 */
class DuetRecordActivity : BaseRecordActivity(), View.OnClickListener {

    companion object { const val DUET_MEDIA = "DUET_MEDIA" }

    private lateinit var glRecordView: GLRecordView
    private lateinit var progressView: RecordProgressView
    private lateinit var recordSpeedBar: RecordSpeedLevelBar
    private lateinit var recordButton: RecordButton

    private lateinit var renderer: DuetRecordRenderer
    private lateinit var presenter: RecordPresenter

    private lateinit var btnSwitch: View
    private lateinit var btnNext: Button
    private lateinit var btnDelete: Button
    private lateinit var btnDuet: Button
    private lateinit var btnDuetFlip: Button
    private lateinit var layoutDuetType: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter = RecordPresenter(this)
        presenter.setRecordSeconds(15)
        renderer = DuetRecordRenderer(presenter)
        intent.getParcelableExtra<MediaData>(DUET_MEDIA)?.let { renderer.setDuetVideo(it) }
        setContent {
            DuetRecordScreen { view -> setupViews(view) }
        }
    }

    private fun setupViews(root: View) {
        glRecordView = root.findViewById(R.id.gl_record_view)
        glRecordView.setEGLContextClientVersion(3)
        glRecordView.setRenderer(renderer)
        glRecordView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY)

        progressView = root.findViewById(R.id.record_progress_view)
        recordSpeedBar = root.findViewById(R.id.record_speed_bar)
        recordSpeedBar.visibility = View.GONE
        recordSpeedBar.setOnSpeedChangedListener { speed ->
            presenter.setSpeedMode(SpeedMode.valueOf(speed.speed))
        }

        recordButton = root.findViewById(R.id.btn_record)
        recordButton.addRecordStateListener(object : RecordButton.RecordStateListener {
            override fun onRecordStart() {
                presenter.startRecord()
                renderer.playVideo()
            }
            override fun onRecordStop() {
                presenter.stopRecord()
                renderer.stopVideo()
            }
            override fun onZoom(percent: Float) {}
        })

        btnSwitch = root.findViewById(R.id.btn_switch)
        btnSwitch.setOnClickListener(this)
        btnNext = root.findViewById(R.id.btn_next)
        btnNext.setOnClickListener(this)
        btnDelete = root.findViewById(R.id.btn_delete)
        btnDelete.setOnClickListener(this)
        btnDuet = root.findViewById(R.id.btn_next_duet)
        btnDuet.setOnClickListener(this)
        btnDuetFlip = root.findViewById(R.id.btn_duet_flip)
        btnDuetFlip.setOnClickListener(this)
        btnDuetFlip.visibility = if (intent.getParcelableExtra<MediaData>(DUET_MEDIA) != null) View.VISIBLE else View.GONE
        layoutDuetType = root.findViewById(R.id.layout_duet_type)
        layoutDuetType.findViewById<Button>(R.id.btn_duet_left_right).setOnClickListener(this)
        layoutDuetType.findViewById<Button>(R.id.btn_duet_up_down).setOnClickListener(this)
        layoutDuetType.findViewById<Button>(R.id.btn_duet_big_small).setOnClickListener(this)

        if (NotchUtils.hasNotchScreen(this)) {
            val view = root.findViewById<View>(R.id.view_safety_area)
            val params = view.layoutParams as LinearLayout.LayoutParams
            params.height = StatusBarUtils.getStatusBarHeight(this)
            view.layoutParams = params
        }
        showViews()
    }

    override fun onResume() {
        super.onResume()
        handleFullScreen()
        glRecordView.onResume()
        presenter.onResume()
        presenter.setAudioEnable(
            PermissionUtils.permissionChecking(this, android.Manifest.permission.RECORD_AUDIO)
        )
    }

    override fun onPause() {
        super.onPause()
        glRecordView.onPause()
        presenter.onPause()
        renderer.clear()
    }

    override fun onDestroy() {
        presenter.release()
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
            R.id.btn_switch -> presenter.switchCamera()
            R.id.btn_delete -> presenter.deleteLastVideo()
            R.id.btn_next -> presenter.mergeAndEdit()
            R.id.btn_next_duet -> {
                layoutDuetType.visibility = View.VISIBLE
                btnDuet.visibility = View.GONE
                btnDuetFlip.visibility = View.GONE
            }
            R.id.btn_duet_flip -> renderer.flip()
            R.id.btn_duet_left_right -> { renderer.setDuetType(DuetType.DUET_TYPE_LEFT_RIGHT); hideDuetTypeViews() }
            R.id.btn_duet_up_down -> { renderer.setDuetType(DuetType.DUET_TYPE_UP_DOWN); hideDuetTypeViews() }
            R.id.btn_duet_big_small -> { renderer.setDuetType(DuetType.DUET_TYPE_BIG_SMALL); hideDuetTypeViews() }
        }
    }

    private fun hideDuetTypeViews() {
        layoutDuetType.visibility = View.GONE
        btnDuet.visibility = View.VISIBLE
        btnDuetFlip.visibility = View.VISIBLE
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
            val showEditEnable = presenter.recordVideoSize > 0
            btnDelete.visibility = if (showEditEnable) View.VISIBLE else View.GONE
            btnNext.visibility = if (showEditEnable) View.VISIBLE else View.GONE
            btnSwitch.visibility = View.VISIBLE
            recordButton.reset()
        }
    }

    override fun setRecordProgress(progress: Float) {
        runOnUiThread { progressView.progress = progress }
    }

    override fun addProgressSegment(progress: Float) {
        runOnUiThread { progressView.addProgressSegment(progress) }
    }

    override fun deleteProgressSegment() {
        runOnUiThread { progressView.deleteProgressSegment() }
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

    fun changeDynamicFilter(index: Int) {
        glRecordView.queueEvent {
            val folderPath = FilterHelper.getFilterDirectory(this) + File.separator +
                    FilterHelper.getFilterList()[index].unzipFolder
            var color: DynamicColor? = null
            if (!FilterHelper.getFilterList()[index].unzipFolder.equals("none", true)) {
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
fun DuetRecordScreen(onViewCreated: (View) -> Unit) {
    val context = LocalContext.current
    AndroidView(
        factory = { ctx ->
            LayoutInflater.from(ctx).inflate(R.layout.activity_duet_record, null, false).apply {
                onViewCreated(this)
            }
        },
        modifier = Modifier
    )
}
