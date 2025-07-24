package com.cgfay.picker.compose

import android.net.Uri
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.cgfay.scan.R
import com.cgfay.picker.model.AlbumData

class PickerViewModel : ViewModel() {
    private val _mediaList = MutableStateFlow<List<Int>>(emptyList())
    val mediaList: StateFlow<List<Int>> = _mediaList

    private val _selectedMedia = MutableStateFlow<List<Int>>(emptyList())
    val selectedMedia: StateFlow<List<Int>> = _selectedMedia

    private val _albumList = MutableStateFlow<List<AlbumData>>(emptyList())
    val albumList: StateFlow<List<AlbumData>> = _albumList

    private val _selectedAlbum = MutableStateFlow<AlbumData?>(null)
    val selectedAlbum: StateFlow<AlbumData?> = _selectedAlbum

    init {
        // Compose demo placeholder data using library icons
        _mediaList.value = List(30) { R.drawable.ic_media_picker_preview }
        _albumList.value = listOf(
            AlbumData("1", Uri.EMPTY, "All", 30),
            AlbumData("2", Uri.EMPTY, "Favorites", 15)
        )
        _selectedAlbum.value = _albumList.value.firstOrNull()
    }

    fun toggle(media: Int) {
        val list = _selectedMedia.value.toMutableList()
        if (list.contains(media)) list.remove(media) else list.add(media)
        _selectedMedia.value = list
    }

    fun confirmSelection() {
        // TODO send selection result
    }

    fun selectAlbum(album: AlbumData) {
        _selectedAlbum.value = album
        // TODO load album media
    }

    fun finish() {
        // TODO finish host activity via callback
    }
}
