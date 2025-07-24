package com.cgfay.picker.compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.lifecycle.viewmodel.compose.viewModel
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
                val factory = PickerViewModelFactory(this, pickerParam)
                val pickerViewModel: PickerViewModel = viewModel(factory = factory)
                PickerNavHost(pickerViewModel)
            }
        }
    }
}
