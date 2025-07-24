package com.cgfay.camera.compose

import android.view.View
import android.widget.FrameLayout
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.FragmentActivity
import com.cgfay.camera.fragment.CameraPreviewFragment
import com.cgfay.camera.presenter.CameraPreviewPresenter

@Composable
fun CameraPreviewScreen(
    presenter: CameraPreviewPresenter,
    fragmentTag: String = "camera_compose"
) {
    val context = LocalContext.current
    AndroidView(factory = {
        FrameLayout(it).apply {
            id = View.generateViewId()
            if (context is FragmentActivity) {
                context.supportFragmentManager.beginTransaction()
                    .replace(id, CameraPreviewFragment(presenter), fragmentTag)
                    .commitNowAllowingStateLoss()
            }
        }
    })
}
