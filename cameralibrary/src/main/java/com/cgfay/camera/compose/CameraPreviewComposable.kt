package com.cgfay.camera.compose

import android.view.View
import android.widget.FrameLayout
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.FragmentActivity
import com.cgfay.camera.fragment.CameraPreviewFragment

@Composable
fun CameraPreviewScreen(fragmentTag: String = "camera_compose") {
    val context = LocalContext.current
    AndroidView(factory = {
        FrameLayout(it).apply {
            id = View.generateViewId()
            if (context is FragmentActivity) {
                context.supportFragmentManager.beginTransaction()
                    .replace(id, CameraPreviewFragment(), fragmentTag)
                    .commitNowAllowingStateLoss()
            }
        }
    })
}
