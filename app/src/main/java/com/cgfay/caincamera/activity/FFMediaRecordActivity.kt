package com.cgfay.caincamera.activity

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
import com.cgfay.caincamera.fragment.FFMediaRecordFragment

class FFMediaRecordActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FFMediaRecordScreen(this)
        }
    }
}

private const val FRAGMENT_FFMEDIA_RECORD = "FRAGMENT_FFMEDIA_RECORD"

@Composable
fun FFMediaRecordScreen(activity: FragmentActivity) {
    val containerId = remember { View.generateViewId() }
    AndroidView(
        factory = { context ->
            FragmentContainerView(context).apply { id = containerId }
        },
        modifier = Modifier
    )

    LaunchedEffect(Unit) {
        if (activity.supportFragmentManager.findFragmentByTag(FRAGMENT_FFMEDIA_RECORD) == null) {
            activity.supportFragmentManager.commit {
                add(containerId, FFMediaRecordFragment(), FRAGMENT_FFMEDIA_RECORD)
            }
        }
    }
}
