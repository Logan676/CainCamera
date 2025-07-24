package com.cgfay.video.activity

import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.commit
import com.cgfay.video.fragment.MultiVideoFragment

class MultiVideoActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val pathList = intent.getStringArrayListExtra(PATH)
        setContent { MultiVideoScreen(this, pathList) }
    }

    companion object {
        const val PATH = "path"
    }
}

private const val FRAGMENT_MULTI_VIDEO = "fragment_multi_video"

@Composable
fun MultiVideoScreen(activity: FragmentActivity, pathList: ArrayList<String>?) {
    val containerId = remember { View.generateViewId() }
    AndroidView(factory = { context -> FragmentContainerView(context).apply { id = containerId } }, modifier = Modifier)

    LaunchedEffect(pathList) {
        if (pathList.isNullOrEmpty()) {
            activity.finish()
        } else {
            if (activity.supportFragmentManager.findFragmentByTag(FRAGMENT_MULTI_VIDEO) == null) {
                val fragment = MultiVideoFragment.newInstance(pathList)
                activity.supportFragmentManager.commit {
                    replace(containerId, fragment, FRAGMENT_MULTI_VIDEO)
                }
            }
        }
    }
}
