package com.cgfay.picker.compose

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.cgfay.scan.R

class PickerViewModel : ViewModel() {
    private val _mediaList = MutableStateFlow<List<Int>>(emptyList())
    val mediaList: StateFlow<List<Int>> = _mediaList

    private val _selectedMedia = MutableStateFlow<List<Int>>(emptyList())
    val selectedMedia: StateFlow<List<Int>> = _selectedMedia

    init {
        // Compose demo placeholder data using library icons
        _mediaList.value = List(30) { R.drawable.ic_media_picker_preview }
    }

    fun toggle(media: Int) {
        val list = _selectedMedia.value.toMutableList()
        if (list.contains(media)) list.remove(media) else list.add(media)
        _selectedMedia.value = list
    }

    fun confirmSelection() {
        // TODO send selection result
    }

    fun finish() {
        // TODO finish host activity via callback
    }
}
