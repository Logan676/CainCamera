package com.cgfay.caincamera.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.cgfay.caincamera.activity.BaseRecordActivity

class RecordViewModelFactory(private val activity: BaseRecordActivity) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RecordViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RecordViewModel(activity) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
