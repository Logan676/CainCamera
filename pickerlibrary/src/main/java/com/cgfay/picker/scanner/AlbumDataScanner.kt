package com.cgfay.picker.scanner

import android.content.Context
import android.database.Cursor
import android.os.Bundle
import androidx.loader.app.LoaderManager
import androidx.loader.content.Loader
import com.cgfay.picker.MediaPickerParam
import com.cgfay.picker.loader.AlbumDataLoader
import com.cgfay.picker.model.AlbumData
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

class AlbumDataScanner(
    private val context: Context,
    private var loaderManager: LoaderManager?,
    private val pickerParam: MediaPickerParam
) : LoaderManager.LoaderCallbacks<Cursor> {

    private var albumDataReceiver: AlbumDataReceiver? = null
    private var pause = false
    private var disposable: Disposable? = null

    private fun getLoaderId(): Int = ALBUM_LOADER_ID

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
        return when {
            pickerParam.showImageOnly() -> AlbumDataLoader.getImageLoader(context)
            pickerParam.showVideoOnly() -> AlbumDataLoader.getVideoLoader(context)
            else -> AlbumDataLoader.getAllLoader(context)
        }
    }

    override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor?) {
        if (disposable == null && data != null) {
            disposable = Observable.just(0)
                .subscribeOn(Schedulers.io())
                .map { scanAllAlbumData(data) }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { albumDataList ->
                    disposable = null
                    albumDataReceiver?.onAlbumDataObserve(albumDataList)
                }
        }
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {
        albumDataReceiver?.onAlbumDataReset()
    }

    fun resume() {
        if (hasRunningLoaders()) {
            pause = false
            return
        }
        if (pause) {
            pause = false
        } else {
            loadAlbumData()
        }
    }

    fun pause() {
        pause = true
        disposable?.dispose()
        disposable = null
    }

    fun destroy() {
        loaderManager?.destroyLoader(getLoaderId())
        loaderManager = null
        albumDataReceiver = null
    }

    fun loadAlbumData() {
        loaderManager?.initLoader(getLoaderId(), null, this)
    }

    private fun hasRunningLoaders(): Boolean {
        return loaderManager?.hasRunningLoaders() ?: false
    }

    private fun isCursorEnable(cursor: Cursor?): Boolean {
        return cursor != null && !cursor.isClosed
    }

    private fun scanAllAlbumData(cursor: Cursor): List<AlbumData> {
        val albumDataList = mutableListOf<AlbumData>()
        while (isCursorEnable(cursor) && cursor.moveToNext()) {
            val albumData = AlbumData.valueOf(cursor)
            albumDataList.add(albumData)
        }
        return albumDataList
    }

    fun setAlbumDataReceiver(receiver: AlbumDataReceiver?) {
        albumDataReceiver = receiver
    }

    interface AlbumDataReceiver {
        fun onAlbumDataObserve(albumDataList: List<AlbumData>)
        fun onAlbumDataReset()
    }

    companion object {
        private const val ALBUM_LOADER_ID = 1
    }
}
