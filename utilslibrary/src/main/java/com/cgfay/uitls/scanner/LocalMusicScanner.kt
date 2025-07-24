package com.cgfay.uitls.scanner

import android.content.Context
import android.provider.MediaStore
import com.cgfay.uitls.bean.MusicData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

class LocalMusicScanner(private val context: Context) {
    private val _musicFlow = MutableStateFlow<List<MusicData>>(emptyList())
    val musicFlow: StateFlow<List<MusicData>> = _musicFlow.asStateFlow()

    suspend fun scan() = withContext(Dispatchers.IO) {
        val uri = MediaStore.Files.getContentUri("external")
        val projection = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.DURATION
        )
        val selection = MediaStore.Files.FileColumns.MEDIA_TYPE + "=?" +
                " AND " + MediaStore.Audio.Media.SIZE + ">0"
        val selectionArgs = arrayOf(MediaStore.Files.FileColumns.MEDIA_TYPE_AUDIO.toString())
        val orderBy = "datetaken DESC"
        context.contentResolver.query(uri, projection, selection, selectionArgs, orderBy)?.use { cursor ->
            val list = mutableListOf<MusicData>()
            while (cursor.moveToNext()) {
                list.add(MusicData.valueof(cursor))
            }
            _musicFlow.value = list
        } ?: run { _musicFlow.value = emptyList() }
    }
}
