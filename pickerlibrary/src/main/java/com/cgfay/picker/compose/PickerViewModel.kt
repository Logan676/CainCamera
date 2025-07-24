package com.cgfay.picker.compose

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.cgfay.picker.MediaPickerParam
import com.cgfay.picker.model.AlbumData
import com.cgfay.picker.model.MediaData
import com.cgfay.picker.scanner.AlbumDataScanner
import com.cgfay.picker.scanner.ImageDataScanner
import com.cgfay.picker.scanner.IMediaDataReceiver
import com.cgfay.picker.scanner.VideoDataScanner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class PickerTab { IMAGE, VIDEO }

class PickerViewModel(
    private val pickerParam: MediaPickerParam,
    imageScannerFactory: (IMediaDataReceiver) -> ImageDataScanner,
    videoScannerFactory: (IMediaDataReceiver) -> VideoDataScanner,
    albumScannerFactory: () -> AlbumDataScanner
) : ViewModel(), IMediaDataReceiver, AlbumDataScanner.AlbumDataReceiver {
    private val imageScanner: ImageDataScanner = imageScannerFactory(this)
    private val videoScanner: VideoDataScanner = videoScannerFactory(this)
    private val albumScanner: AlbumDataScanner = albumScannerFactory().apply {
        setAlbumDataReceiver(this@PickerViewModel)
    }
    private val _selectedTab = MutableStateFlow(
        when {
            pickerParam.showVideoOnly() -> PickerTab.VIDEO
            else -> PickerTab.IMAGE
        }
    )
    val selectedTab: StateFlow<PickerTab> = _selectedTab.asStateFlow()

    private val _mediaList = MutableStateFlow<List<MediaData>>(emptyList())
    val mediaList: StateFlow<List<MediaData>> = _mediaList.asStateFlow()

    private val _selectedMedia = MutableStateFlow<List<MediaData>>(emptyList())
    val selectedMedia: StateFlow<List<MediaData>> = _selectedMedia.asStateFlow()

    private val _albumList = MutableStateFlow<List<AlbumData>>(emptyList())
    val albumList: StateFlow<List<AlbumData>> = _albumList

    private val _selectedAlbum = MutableStateFlow<AlbumData?>(null)
    val selectedAlbum: StateFlow<AlbumData?> = _selectedAlbum

    private fun loadCurrentMedia() {
        val album = _selectedAlbum.value ?: return
        when (_selectedTab.value) {
            PickerTab.IMAGE -> imageScanner.loadAlbumMedia(album)
            PickerTab.VIDEO -> videoScanner.loadAlbumMedia(album)
        }
    }

    init {
        albumScanner.resume()
        imageScanner.resume()
        videoScanner.resume()
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
        loadCurrentMedia()
    }

    fun selectTab(tab: PickerTab) {
        if (_selectedTab.value != tab) {
            _selectedTab.value = tab
            loadCurrentMedia()
        }
    }

    fun finish() {
        // TODO finish host activity via callback
    }

    override fun onMediaDataObserve(mediaDataList: List<MediaData>) {
        _mediaList.value = mediaDataList
    }

    override fun onAlbumDataObserve(albumDataList: List<AlbumData>) {
        _albumList.value = albumDataList
        if (_selectedAlbum.value == null && albumDataList.isNotEmpty()) {
            _selectedAlbum.value = albumDataList.first()
            loadCurrentMedia()
        }
    }

    override fun onAlbumDataReset() {
        _albumList.value = emptyList()
        _mediaList.value = emptyList()
    }

    override fun onCleared() {
        super.onCleared()
        imageScanner.destroy()
        videoScanner.destroy()
        albumScanner.destroy()
    }
}
