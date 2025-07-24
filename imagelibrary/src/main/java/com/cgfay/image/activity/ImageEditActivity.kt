package com.cgfay.image.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.cgfay.image.compose.ImageNavGraph

class ImageEditActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val imagePath = intent.getStringExtra(IMAGE_PATH)
        val deleteInput = intent.getBooleanExtra(DELETE_INPUT_FILE, false)
        setContent {
            ImageNavGraph(imagePath, deleteInput)
        }
    }

    companion object {
        const val IMAGE_PATH = "image_path"
        const val DELETE_INPUT_FILE = "delete_input_file"
    }
}
