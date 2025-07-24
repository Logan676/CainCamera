package com.cgfay.picker.scanner

import android.content.Context
import android.database.Cursor
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.loader.app.LoaderManager
import androidx.loader.content.Loader
import com.cgfay.picker.loader.MediaDataLoader
import com.cgfay.picker.model.AlbumData
import com.cgfay.picker.model.MediaData
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.lang.ref.WeakReference
import java.util.ArrayList

abstract class MediaDataScanner(
    protected val context: Context,
    protected var loaderManager: LoaderManager?,
    protected var dataReceiver: IMediaDataReceiver?
) : LoaderManager.LoaderCallbacks<Cursor> {

    private var currentAlbum: AlbumData? = null
    private val mainHandler = Handler(Looper.getMainLooper())
    private var pause = false
    private var loadDisposable: Disposable? = null
    private var preScanDisposable: Disposable? = null
    private var loadFinish = false
    private var userVisible = false

    private var weakCursor: WeakReference<Cursor>? = null
    private val cacheMediaData: MutableList<MediaData> = ArrayList()
    private var pageScan = false

    fun setUserVisible(visible: Boolean) {
        userVisible = visible
        Log.d(TAG, "setUserVisible: $visible${getMimeType(getMediaType())}")
        if (loadFinish) {
            preScanMediaData()
        }
    }

    fun resume() {
        if (pause) {
            pause = false
        } else {
            loadMedia()
        }
    }

    fun pause() {
        pause = true
        loadDisposable?.dispose()
        loadDisposable = null
        preScanDisposable?.dispose()
        preScanDisposable = null
    }

    fun destroy() {
        loaderManager?.destroyLoader(getLoaderId())
        loaderManager = null
        dataReceiver = null
        mainHandler.removeCallbacksAndMessages(null)
    }

    fun loadAlbumMedia(album: AlbumData) {
        if (album == currentAlbum || currentAlbum == null && album.isAll) {
            currentAlbum = album
            return
        }
        loaderManager?.let { manager ->
            val bundle = Bundle()
            bundle.putParcelable(ALBUM_ARGS, album)
            loadFinish = false
            currentAlbum = album
            loadDisposable?.dispose()
            loadDisposable = null
            preScanDisposable?.dispose()
            preScanDisposable = null
            synchronized(LOCK) { cacheMediaData.clear() }
            manager.restartLoader(getLoaderId(), bundle, this)
        }
    }

    private fun loadMedia() {
        loaderManager?.let { manager ->
            if (currentAlbum == null) {
                manager.initLoader(getLoaderId(), null, this)
            } else {
                val bundle = Bundle()
                bundle.putParcelable(ALBUM_ARGS, currentAlbum)
                manager.initLoader(getLoaderId(), bundle, this)
            }
        }
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
        if (args == null) {
            return MediaDataLoader.createMediaDataLoader(context, getMediaType())
        }
        val albumData: AlbumData? = args.getParcelable(ALBUM_ARGS)
        return if (albumData == null) {
            MediaDataLoader.createMediaDataLoader(context, getMediaType())
        } else {
            MediaDataLoader.createMediaDataLoader(context, albumData, getMediaType())
        }
    }

    override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor?) {
        synchronized(LOCK) {
            weakCursor?.clear()
            if (data != null) {
                weakCursor = WeakReference(data)
            }
        }
        if (data != null && isCursorEnable(data) && !loadFinish) {
            loadFinish = true
            if (data.count <= PAGE_SIZE) {
                pageScan = false
                scanAllMedia(data)
            } else {
                pageScan = true
                scanPageMedia(data)
            }
        }
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {
        Log.d(TAG, "onLoaderReset: ${getMimeType(getMediaType())}")
    }

    private fun scanAllMedia(cursor: Cursor) {
        if (loadDisposable == null) {
            loadDisposable = Observable.just(0)
                .subscribeOn(Schedulers.io())
                .map { scanAllMediaData(cursor) }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { medias ->
                    loadDisposable = null
                    dataReceiver?.onMediaDataObserve(medias)
                }
        }
    }

    private fun scanPageMedia(cursor: Cursor) {
        if (loadDisposable == null) {
            loadDisposable = Observable.just(0)
                .subscribeOn(Schedulers.io())
                .map { scanPageMediaData(cursor) }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { loadDisposable = null }
        }
    }

    private fun preScanMediaData() {
        val c = weakCursor?.get()
        if (c != null && isCursorEnable(c)) {
            if (preScanDisposable == null) {
                preScanDisposable = Observable.just(0)
                    .subscribeOn(Schedulers.io())
                    .map { preScanMediaData(c) }
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { preScanDisposable = null }
            }
        } else {
            Log.i(TAG, "Cursor cache is invalid!")
        }
    }

    private fun scanAllMediaData(cursor: Cursor): List<MediaData> {
        val medias = mutableListOf<MediaData>()
        try {
            while (isCursorEnable(cursor) && cursor.moveToNext()) {
                val media = buildMediaData(context, cursor)
                if (media != null) {
                    medias.add(media)
                }
            }
        } catch (t: Throwable) {
            t.printStackTrace()
        }
        return medias
    }

    private fun scanPageMediaData(cursor: Cursor): Int {
        try {
            val mediaList = ArrayList<MediaData>()
            var size = 0
            while (isCursorEnable(cursor) && cursor.moveToNext() && size < PAGE_SIZE) {
                val media = buildMediaData(context, cursor)
                if (media != null) {
                    mediaList.add(media)
                }
                size++
            }
            mainHandler.post {
                dataReceiver?.onMediaDataObserve(mediaList)
            }
            if (userVisible) {
                if (isCursorEnable(cursor) && !cursor.isLast) {
                    preScanMediaData(cursor)
                }
            }
        } catch (t: Throwable) {
            t.printStackTrace()
        }
        return 0
    }

    private fun preScanMediaData(cursor: Cursor): Int {
        val mediaList = ArrayList<MediaData>()
        var size = 0
        while (isCursorEnable(cursor) && cursor.moveToNext() && size < PAGE_SIZE * 2) {
            val media = buildMediaData(context, cursor)
            if (media != null) {
                mediaList.add(media)
            }
            size++
        }
        synchronized(LOCK) {
            cacheMediaData.addAll(mediaList)
        }
        return 0
    }

    private fun buildMediaData(context: Context, cursor: Cursor): MediaData? {
        return MediaData.valueOf(context, cursor)
    }

    val cacheMediaDataList: List<MediaData>
        get() {
            val cacheList: MutableList<MediaData> = ArrayList()
            synchronized(LOCK) {
                if (cacheMediaData.size > PAGE_SIZE) {
                    val subCache: List<MediaData> = cacheMediaData.subList(0, PAGE_SIZE)
                    cacheList.addAll(subCache)
                    cacheMediaData.subList(0, PAGE_SIZE).clear()
                } else {
                    cacheList.addAll(cacheMediaData)
                    cacheMediaData.clear()
                }
            }
            val size: Int = synchronized(LOCK) { cacheMediaData.size }
            if (size < MAX_CACHE_SIZE) {
                preScanMediaData()
            }
            return cacheList
        }

    protected fun isCursorEnable(cursor: Cursor?): Boolean {
        return cursor != null && !cursor.isClosed && !cursor.isLast
    }

    val isPageScan: Boolean
        get() = pageScan

    protected abstract fun getLoaderId(): Int
    protected abstract fun getMediaType(): Int

    protected fun getMimeType(mimeType: Int): String {
        return when (mimeType) {
            MediaDataLoader.LOAD_IMAGE -> "images"
            MediaDataLoader.LOAD_VIDEO -> "video"
            else -> "all image and video"
        }
    }

    companion object {
        private const val TAG = "MediaDataScanner"
        protected const val ALBUM_ARGS = "album_args"
        private val LOCK = Any()
        const val PAGE_SIZE = 50
        protected const val MAX_CACHE_SIZE = PAGE_SIZE * 3
        protected const val VIDEO_LOADER_ID = 2
        protected const val IMAGE_LOADER_ID = 3
    }
}
