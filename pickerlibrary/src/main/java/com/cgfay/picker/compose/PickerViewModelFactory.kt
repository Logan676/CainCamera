package com.cgfay.picker.compose

import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.loader.app.LoaderManager
import com.cgfay.picker.MediaPickerParam
import com.cgfay.picker.scanner.AlbumDataScanner
import com.cgfay.picker.scanner.ImageDataScanner
import com.cgfay.picker.scanner.VideoDataScanner

class PickerViewModelFactory(
    private val activity: ComponentActivity,
    private val param: MediaPickerParam
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PickerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PickerViewModel(
                param,
                { receiver -> ImageDataScanner(activity, LoaderManager.getInstance(activity), receiver) },
                { receiver -> VideoDataScanner(activity, LoaderManager.getInstance(activity), receiver) },
                { AlbumDataScanner(activity, LoaderManager.getInstance(activity), param) }
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
