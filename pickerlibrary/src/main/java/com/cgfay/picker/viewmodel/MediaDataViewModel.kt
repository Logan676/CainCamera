package com.cgfay.picker.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.cgfay.picker.model.MediaData

class MediaDataViewModel : ViewModel() {
    private val _selectedMedia = MutableLiveData<MutableList<MediaData>>(mutableListOf())
    val selectedMedia: LiveData<MutableList<MediaData>> = _selectedMedia

    fun getSelectedIndex(mediaData: MediaData): Int {
        return _selectedMedia.value?.indexOf(mediaData) ?: -1
    }

    fun addSelectedMedia(mediaData: MediaData) {
        val list = _selectedMedia.value ?: mutableListOf()
        list.add(mediaData)
        _selectedMedia.value = list
    }

    fun removeSelectedMedia(mediaData: MediaData) {
        val list = _selectedMedia.value ?: mutableListOf()
        list.remove(mediaData)
        _selectedMedia.value = list
    }

    fun clear() {
        _selectedMedia.value?.clear()
        _selectedMedia.value = _selectedMedia.value
    }

    fun getSelectedMediaDataList(): List<MediaData> {
        return _selectedMedia.value ?: emptyList()
    }
}
