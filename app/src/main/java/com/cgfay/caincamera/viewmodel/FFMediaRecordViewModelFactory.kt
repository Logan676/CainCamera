package com.cgfay.caincamera.viewmodel

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class FFMediaRecordViewModelFactory(private val activity: FragmentActivity) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FFMediaRecordViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FFMediaRecordViewModel(activity) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
