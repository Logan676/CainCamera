package com.cgfay.picker.viewmodel

import androidx.lifecycle.ViewModel
import com.cgfay.picker.model.MediaData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MediaDataViewModel : ViewModel() {
    private val _selectedMedia = MutableStateFlow<List<MediaData>>(emptyList())
    val selectedMedia: StateFlow<List<MediaData>> = _selectedMedia

    fun getSelectedIndex(media: MediaData): Int {
        return _selectedMedia.value.indexOf(media)
    }

    fun addSelectedMedia(media: MediaData) {
        val list = _selectedMedia.value.toMutableList()
        list.add(media)
        _selectedMedia.value = list
    }

    fun removeSelectedMedia(media: MediaData) {
        val list = _selectedMedia.value.toMutableList()
        list.remove(media)
        _selectedMedia.value = list
    }

    fun clear() {
        _selectedMedia.value = emptyList()
    }
}
