package com.cgfay.camera.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.fragment.app.FragmentActivity
import com.cgfay.cameralibrary.R
import com.cgfay.camera.compose.CameraPreviewScreen
import com.cgfay.camera.fragment.CameraPreviewFragment
import com.cgfay.facedetect.engine.FaceTracker
import com.cgfay.uitls.utils.NotchUtils

class CameraActivity : ComponentActivity() {

    private val homePressReceiver = object : BroadcastReceiver() {
        private val SYSTEM_DIALOG_REASON_KEY = "reason"
        private val SYSTEM_DIALOG_REASON_HOME_KEY = "homekey"
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == Intent.ACTION_CLOSE_SYSTEM_DIALOGS) {
                val reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY)
                if (TextUtils.isEmpty(reason)) return
                if (reason == SYSTEM_DIALOG_REASON_HOME_KEY) {
                    val fragment = (context as? FragmentActivity)?.supportFragmentManager
                        ?.findFragmentByTag(FRAGMENT_CAMERA) as? CameraPreviewFragment
                    fragment?.cancelRecordIfNeeded()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CameraPreviewScreen(fragmentTag = FRAGMENT_CAMERA)
        }
        faceTrackerRequestNetwork()
    }

    private fun faceTrackerRequestNetwork() {
        Thread { FaceTracker.requestFaceNetwork(this) }.start()
    }

    override fun onResume() {
        super.onResume()
        handleFullScreen()
        registerHomeReceiver()
    }

    override fun onPause() {
        super.onPause()
        unRegisterHomeReceiver()
    }

    private fun handleFullScreen() {
        if (NotchUtils.hasNotchScreen(this)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val lp = window.attributes
                lp.layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
                window.attributes = lp
            }
        }
    }

    override fun onBackPressed() {
        val fragment = supportFragmentManager.findFragmentByTag(FRAGMENT_CAMERA) as? CameraPreviewFragment
        if (fragment == null || !fragment.onBackPressed()) {
            super.onBackPressed()
            overridePendingTransition(0, R.anim.anim_slide_down)
        }
    }

    private fun registerHomeReceiver() {
        val homeFilter = IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)
        registerReceiver(homePressReceiver, homeFilter)
    }

    private fun unRegisterHomeReceiver() {
        unregisterReceiver(homePressReceiver)
    }

    companion object {
        private const val FRAGMENT_CAMERA = "fragment_camera"
    }
}
