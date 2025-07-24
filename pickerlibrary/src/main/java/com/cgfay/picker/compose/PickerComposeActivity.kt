package com.cgfay.picker.compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import com.cgfay.picker.MediaPicker
import com.cgfay.picker.MediaPickerParam

class PickerComposeActivity : ComponentActivity() {
    private var pickerParam: MediaPickerParam = MediaPickerParam()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        intent.getSerializableExtra(MediaPicker.PICKER_PARAMS)?.let {
            pickerParam = it as MediaPickerParam
        }
        setContent {
            MaterialTheme {
                PickerNavHost()
            }
        }
    }
}
