package com.cgfay.image.activity

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
import com.cgfay.image.fragment.ImageEditedFragment

class ImageEditActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val imagePath = intent.getStringExtra(IMAGE_PATH)
        val deleteInput = intent.getBooleanExtra(DELETE_INPUT_FILE, false)
        setContent {
            ImageEditScreen(this, imagePath, deleteInput)
        }
    }

    override fun onBackPressed() {
        val count = supportFragmentManager.backStackEntryCount
        if (count > 1) {
            supportFragmentManager.popBackStack()
        } else if (count == 1) {
            val fragment = supportFragmentManager.findFragmentByTag(FRAGMENT_IMAGE) as? ImageEditedFragment
            if (fragment != null) {
                fragment.onBackPressed()
            } else {
                super.onBackPressed()
            }
        } else {
            super.onBackPressed()
        }
    }

    companion object {
        private const val FRAGMENT_IMAGE = "fragment_image"
        const val IMAGE_PATH = "image_path"
        const val DELETE_INPUT_FILE = "delete_input_file"
    }
}

@Composable
private fun ImageEditScreen(activity: FragmentActivity, imagePath: String?, deleteInput: Boolean) {
    val containerId = remember { View.generateViewId() }
    AndroidView(
        factory = { context -> FragmentContainerView(context).apply { id = containerId } },
        modifier = Modifier
    )

    LaunchedEffect(Unit) {
        if (activity.supportFragmentManager.findFragmentByTag(FRAGMENT_IMAGE) == null) {
            val fragment = ImageEditedFragment().apply {
                setImagePath(imagePath, deleteInput)
            }
            activity.supportFragmentManager.commit {
                replace(containerId, fragment, FRAGMENT_IMAGE)
                addToBackStack(FRAGMENT_IMAGE)
            }
        }
    }
}
