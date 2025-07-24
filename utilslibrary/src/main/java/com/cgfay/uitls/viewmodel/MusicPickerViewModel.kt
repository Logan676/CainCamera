package com.cgfay.uitls.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cgfay.uitls.bean.MusicData
import com.cgfay.uitls.scanner.LocalMusicScanner
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MusicPickerViewModel(application: Application) : AndroidViewModel(application) {
    private val scanner = LocalMusicScanner(application)
    val musicList: StateFlow<List<MusicData>> = scanner.musicFlow

    init {
        viewModelScope.launch {
            scanner.scan()
        }
    }
}
