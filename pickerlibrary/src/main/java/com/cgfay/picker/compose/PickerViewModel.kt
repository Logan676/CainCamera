package com.cgfay.picker.compose

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.loader.app.LoaderManager
import com.cgfay.picker.MediaPickerManager
import com.cgfay.picker.MediaPickerParam
import com.cgfay.picker.model.AlbumData
import com.cgfay.picker.model.MediaData
import com.cgfay.picker.presenter.MediaDataPresenter
import com.cgfay.picker.scanner.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class PickerViewModel(
    private val activity: FragmentActivity,
    val pickerParam: MediaPickerParam
) : ViewModel(), AlbumDataScanner.AlbumDataReceiver, IMediaDataReceiver {

    private val albumScanner: AlbumDataScanner =
        AlbumDataScanner(activity, LoaderManager.getInstance(activity), pickerParam)

    private val imageScanner: ImageDataScanner? =
        if (!pickerParam.showVideoOnly()) ImageDataScanner(activity, LoaderManager.getInstance(activity), this) else null
    private val videoScanner: VideoDataScanner? =
        if (!pickerParam.showImageOnly()) VideoDataScanner(activity, LoaderManager.getInstance(activity), this) else null

    private var currentScanner: MediaDataScanner? = null

    private val presenter = MediaDataPresenter()

    private val _albumList = MutableStateFlow<List<AlbumData>>(emptyList())
    val albumList: StateFlow<List<AlbumData>> = _albumList

    private val _selectedAlbum = MutableStateFlow<AlbumData?>(null)
    val selectedAlbum: StateFlow<AlbumData?> = _selectedAlbum

    private val _mediaList = MutableStateFlow<List<MediaData>>(emptyList())
    val mediaList: StateFlow<List<MediaData>> = _mediaList

    private val _selectedMedia = MutableStateFlow<List<MediaData>>(emptyList())
    val selectedMedia: StateFlow<List<MediaData>> = _selectedMedia

    private val _currentTab = MutableStateFlow(0)
    val currentTab: StateFlow<Int> = _currentTab

    init {
        albumScanner.setAlbumDataReceiver(this)
        albumScanner.loadAlbumData()
        selectTab(0)
    }

    override fun onAlbumDataObserve(albumDataList: List<AlbumData>) {
        _albumList.value = albumDataList
        if (_selectedAlbum.value == null && albumDataList.isNotEmpty()) {
            _selectedAlbum.value = albumDataList[0]
            loadAlbum(albumDataList[0])
        }
    }

    override fun onAlbumDataReset() {
        _albumList.value = emptyList()
    }

    override fun onMediaDataObserve(mediaDataList: List<MediaData>) {
        _mediaList.value = mediaDataList
    }

    fun selectTab(index: Int) {
        _currentTab.value = index
        currentScanner = if (index == 0) {
            imageScanner
        } else {
            videoScanner
        }
        _selectedAlbum.value?.let { currentScanner?.loadAlbumMedia(it) }
    }

    fun selectAlbum(album: AlbumData) {
        _selectedAlbum.value = album
        loadAlbum(album)
    }

    private fun loadAlbum(album: AlbumData) {
        currentScanner?.loadAlbumMedia(album)
    }

    fun toggle(media: MediaData) {
        val list = presenter.getSelectedMediaDataList().toMutableList()
        if (list.contains(media)) {
            list.remove(media)
            presenter.removeSelectedMedia(media)
        } else {
            list.add(media)
            presenter.addSelectedMedia(media)
        }
        _selectedMedia.value = list
    }

    fun confirmSelection() {
        val selector = MediaPickerManager.getInstance().mediaSelector
        selector?.onMediaSelect(activity, presenter.getSelectedMediaDataList())
        if (pickerParam.autoDismiss) {
            activity.finish()
        }
    }

    fun finish() {
        activity.finish()
    }

    override fun onCleared() {
        super.onCleared()
        albumScanner.destroy()
        imageScanner?.destroy()
        videoScanner?.destroy()
    }

    class Factory(
        private val activity: FragmentActivity,
        private val pickerParam: MediaPickerParam
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PickerViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return PickerViewModel(activity, pickerParam) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
