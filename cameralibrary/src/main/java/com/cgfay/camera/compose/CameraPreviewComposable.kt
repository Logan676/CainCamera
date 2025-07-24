package com.cgfay.camera.compose

import android.view.View
import android.widget.FrameLayout
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.FragmentActivity
import com.cgfay.camera.fragment.CameraPreviewFragment

@Composable
fun CameraPreviewScreen(fragmentTag: String = "camera_compose") {
    val context = LocalContext.current
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
        exit = fadeOut() + slideOutVertically(targetOffsetY = { -it })
    ) {
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
}
