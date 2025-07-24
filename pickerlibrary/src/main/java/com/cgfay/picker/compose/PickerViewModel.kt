package com.cgfay.picker.compose

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cgfay.picker.model.AlbumData
import com.cgfay.picker.model.MediaData
import com.cgfay.picker.scanner.ImageDataScanner
import com.cgfay.picker.scanner.IMediaDataReceiver
import com.cgfay.picker.scanner.VideoDataScanner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PickerViewModel(
    imageScannerFactory: (IMediaDataReceiver) -> ImageDataScanner,
    videoScannerFactory: (IMediaDataReceiver) -> VideoDataScanner
) : ViewModel(), IMediaDataReceiver {
    private val imageScanner: ImageDataScanner = imageScannerFactory(this)
    private val videoScanner: VideoDataScanner = videoScannerFactory(this)
    private val _mediaList = MutableStateFlow<List<MediaData>>(emptyList())
    val mediaList: StateFlow<List<MediaData>> = _mediaList.asStateFlow()

    private val _selectedMedia = MutableStateFlow<List<MediaData>>(emptyList())
    val selectedMedia: StateFlow<List<MediaData>> = _selectedMedia.asStateFlow()

    private val _albumList = MutableStateFlow<List<AlbumData>>(emptyList())
    val albumList: StateFlow<List<AlbumData>> = _albumList

    private val _selectedAlbum = MutableStateFlow<AlbumData?>(null)
    val selectedAlbum: StateFlow<AlbumData?> = _selectedAlbum

    init {
        // Compose demo placeholder data using library icons if scanners are not available
        _albumList.value = listOf(
            AlbumData("1", Uri.EMPTY, "All", 0)
        )
        _selectedAlbum.value = _albumList.value.firstOrNull()

        viewModelScope.launch {
            imageScanner.resume()
            videoScanner.resume()
        }
    }

    fun toggle(media: MediaData) {
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

    override fun onMediaDataObserve(mediaDataList: List<MediaData>) {
        _mediaList.value = mediaDataList
    }
}
