package com.cgfay.picker.scanner

import android.content.Context
import androidx.loader.app.LoaderManager
import com.cgfay.picker.loader.MediaDataLoader

class ImageDataScanner(
    context: Context,
    manager: LoaderManager,
    dataReceiver: IMediaDataReceiver
) : MediaDataScanner(context, manager, dataReceiver) {

    override fun getLoaderId(): Int = IMAGE_LOADER_ID

    override fun getMediaType(): Int = MediaDataLoader.LOAD_IMAGE
}
