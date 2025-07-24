package com.cgfay.picker

import com.cgfay.picker.loader.MediaLoader

/**
 * Manager for picker settings and loader.
 */
class MediaPickerManager private constructor() {
    var mediaLoader: MediaLoader = PickerMediaLoader()

    fun setMediaLoader(loader: MediaLoader): MediaPickerManager {
        mediaLoader = loader
        return this
    }

    fun reset() {
        mediaLoader = PickerMediaLoader()
    }

    companion object {
        @Volatile
        private var instance: MediaPickerManager? = null

        fun getInstance(): MediaPickerManager {
            return instance ?: synchronized(this) {
                instance ?: MediaPickerManager().also { instance = it }
            }
        }
    }
}
