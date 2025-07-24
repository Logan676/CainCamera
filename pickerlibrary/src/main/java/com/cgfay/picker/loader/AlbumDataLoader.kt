package com.cgfay.picker.loader

import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.database.MatrixCursor
import android.database.MergeCursor
import android.net.Uri
import android.provider.MediaStore
import androidx.annotation.NonNull
import androidx.loader.content.CursorLoader
import com.cgfay.picker.model.AlbumData
import com.cgfay.picker.model.MimeType

class AlbumDataLoader private constructor(
    @NonNull context: Context,
    selection: String,
    selectionArgs: Array<String>,
    order: String = BUCKET_ORDER_BY
) : CursorLoader(context, QUERY_URI, PROJECTION_Q, selection, selectionArgs, order) {

    override fun loadInBackground(): Cursor {
        val albums = super.loadInBackground()
        val allAlbum = MatrixCursor(COLUMNS)
        var totalCount = 0
        var allAlbumCoverUri: Uri? = null

        @Suppress("UseSparseArrays")
        val countMap: MutableMap<Long, Long> = HashMap()
        albums?.let { c ->
            while (c.moveToNext()) {
                val bucketId = c.getLong(c.getColumnIndex(COLUMN_BUCKET_ID))
                val count = countMap[bucketId]
                countMap[bucketId] = count?.plus(1) ?: 1
            }
        }

        val supportAlbums = MatrixCursor(COLUMNS)
        albums?.let { c ->
            if (c.moveToFirst()) {
                allAlbumCoverUri = getUri(c)
                val done: MutableSet<Long> = HashSet()
                do {
                    val bucketId = c.getLong(c.getColumnIndex(COLUMN_BUCKET_ID))
                    if (done.contains(bucketId)) continue
                    val fileId = c.getLong(c.getColumnIndex(MediaStore.Files.FileColumns._ID))
                    val bucketDisplayName = c.getString(c.getColumnIndex(COLUMN_BUCKET_DISPLAY_NAME))
                    val mimeType = c.getString(c.getColumnIndex(MediaStore.MediaColumns.MIME_TYPE))
                    val uri = getUri(c)
                    val count = countMap[bucketId] ?: 0
                    if (count > 1) {
                        supportAlbums.addRow(
                            arrayOf(
                                fileId.toString(),
                                bucketId.toString(),
                                bucketDisplayName,
                                mimeType,
                                uri.toString(),
                                count.toString()
                            )
                        )
                        done.add(bucketId)
                        totalCount += count.toInt()
                    }
                } while (c.moveToNext())
            }
        }

        allAlbum.addRow(
            arrayOf(
                AlbumData.ALBUM_ID_ALL,
                AlbumData.ALBUM_ID_ALL,
                AlbumData.ALBUM_NAME_ALL,
                null,
                allAlbumCoverUri?.toString(),
                totalCount.toString()
            )
        )
        return MergeCursor(arrayOf(allAlbum, supportAlbums))
    }

    companion object {
        const val COLUMN_BUCKET_ID = "bucket_id"
        const val COLUMN_BUCKET_DISPLAY_NAME = "bucket_display_name"
        const val COLUMN_URI = "uri"
        const val COLUMN_COUNT = "count"

        private val QUERY_URI: Uri = MediaStore.Files.getContentUri("external")

        private val COLUMNS = arrayOf(
            MediaStore.Files.FileColumns._ID,
            COLUMN_BUCKET_ID,
            COLUMN_BUCKET_DISPLAY_NAME,
            MediaStore.MediaColumns.MIME_TYPE,
            COLUMN_URI,
            COLUMN_COUNT
        )

        private val PROJECTION_Q = arrayOf(
            MediaStore.Files.FileColumns._ID,
            COLUMN_BUCKET_ID,
            COLUMN_BUCKET_DISPLAY_NAME,
            MediaStore.MediaColumns.MIME_TYPE
        )

        private const val SELECTION_ALL_Q = "(" + MediaStore.Files.FileColumns.MEDIA_TYPE + "=?" +
                " OR " + MediaStore.Files.FileColumns.MEDIA_TYPE + "=?)" +
                " AND " + MediaStore.MediaColumns.SIZE + ">0"

        private val SELECTION_ALL_ARGS = arrayOf(
            MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString(),
            MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO.toString()
        )

        private const val SELECTION_FOR_SINGLE_MEDIA_TYPE_Q =
            MediaStore.Files.FileColumns.MEDIA_TYPE + "=?" +
                    " AND " + MediaStore.MediaColumns.SIZE + ">0"

        private fun getSelectionArgsForSingleMediaType(mediaType: Int): Array<String> =
            arrayOf(mediaType.toString())

        private const val NORMAL_ORDER_BY = "datetaken DESC"

        private const val BUCKET_ORDER_BY =
            "CASE bucket_display_name WHEN 'Camera' THEN 1 ELSE 100 END ASC, datetaken DESC"

        fun getImageLoaderWithoutBucketSort(context: Context): CursorLoader =
            AlbumDataLoader(
                context,
                SELECTION_FOR_SINGLE_MEDIA_TYPE_Q,
                getSelectionArgsForSingleMediaType(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE),
                NORMAL_ORDER_BY
            )

        fun getImageLoader(context: Context): CursorLoader =
            AlbumDataLoader(
                context,
                SELECTION_FOR_SINGLE_MEDIA_TYPE_Q,
                getSelectionArgsForSingleMediaType(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE)
            )

        fun getVideoLoader(context: Context): CursorLoader =
            AlbumDataLoader(
                context,
                SELECTION_FOR_SINGLE_MEDIA_TYPE_Q,
                getSelectionArgsForSingleMediaType(MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO)
            )

        fun getAllLoader(context: Context): CursorLoader =
            AlbumDataLoader(context, SELECTION_ALL_Q, SELECTION_ALL_ARGS)

        private fun getUri(cursor: Cursor): Uri {
            val id = cursor.getLong(cursor.getColumnIndex(MediaStore.Files.FileColumns._ID))
            val mimeType = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.MIME_TYPE))
            val contentUri = when {
                MimeType.isImage(mimeType) -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                MimeType.isVideo(mimeType) -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                else -> MediaStore.Files.getContentUri("external")
            }
            return ContentUris.withAppendedId(contentUri, id)
        }
    }
}
