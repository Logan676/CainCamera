package com.cgfay.picker.loader

import android.content.Context
import android.os.Build
import android.provider.MediaStore
import android.text.TextUtils
import androidx.annotation.IntDef
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.annotation.RequiresApi
import androidx.annotation.RestrictTo
import androidx.loader.content.CursorLoader
import com.cgfay.picker.model.AlbumData
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.util.ArrayList
import java.util.Collections
import java.util.List

/**
 * 媒体数据加载器
 */
class MediaDataLoader private constructor(
    @NonNull context: Context,
    @Nullable projection: Array<String>?,
    @NonNull selection: String,
    @NonNull selectionArgs: Array<String>
) : CursorLoader(
    context,
    QUERY_URI,
    projection,
    selection,
    selectionArgs,
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) MEDIA_ORDER_Q else MEDIA_ORDER
) {

    companion object {
        private val QUERY_URI = MediaStore.Files.getContentUri("external")

        private val PROJECTION_ALL = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.MediaColumns.MIME_TYPE,
            MediaStore.MediaColumns.WIDTH,
            MediaStore.MediaColumns.HEIGHT,
            MediaStore.MediaColumns.SIZE
        )

        private val PROJECTION_IMAGE = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.MediaColumns.MIME_TYPE,
            MediaStore.MediaColumns.WIDTH,
            MediaStore.MediaColumns.HEIGHT,
            MediaStore.MediaColumns.SIZE
        )

        private val PROJECTION_VIDEO = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.MediaColumns.MIME_TYPE,
            MediaStore.MediaColumns.WIDTH,
            MediaStore.MediaColumns.HEIGHT,
            MediaStore.MediaColumns.SIZE,
            "duration"
        )

        @RequiresApi(Build.VERSION_CODES.Q)
        private val PROJECTION_VIDEO_Q = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.MediaColumns.MIME_TYPE,
            MediaStore.MediaColumns.WIDTH,
            MediaStore.MediaColumns.HEIGHT,
            MediaStore.MediaColumns.SIZE,
            MediaStore.MediaColumns.DURATION
        )

        @RequiresApi(Build.VERSION_CODES.Q)
        private const val MEDIA_ORDER_Q = MediaStore.MediaColumns.DATE_TAKEN + " DESC"
        private const val MEDIA_ORDER = "datetaken DESC"

        private const val MEDIA_SIZE = MediaStore.MediaColumns.SIZE + ">0"

        private val SELECTION_ALL_TYPE_ARGS = arrayOf(
            MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString(),
            MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO.toString()
        )

        private val SELECTION_IMAGE_TYPE_ARGS = arrayOf(
            "image/jpeg", "image/jpg", "image/bmp", "image/png"
        )

        private val SELECTION_VIDEO_TYPE_ARGS = arrayOf(
            "video/mpeg", "video/mp4", "video/m4v", "video/3gpp", "video/x-matroska", "video/avi"
        )

        const val LOAD_ALL = 0
        const val LOAD_VIDEO = 1
        const val LOAD_IMAGE = 2

        @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
        @IntDef(value = [LOAD_ALL, LOAD_VIDEO, LOAD_IMAGE])
        @Retention(RetentionPolicy.SOURCE)
        annotation class LoadMimeType

        fun createMediaDataLoader(context: Context, @LoadMimeType mimeType: Int): CursorLoader {
            val projection = getProjection(mimeType)
            val selection: String
            val selectionArgs: Array<String>
            when (mimeType) {
                LOAD_IMAGE -> {
                    selection = getSelectionMimeType(LOAD_IMAGE, SELECTION_IMAGE_TYPE_ARGS)
                    selectionArgs = getAlbumSelectionImageType(AlbumData.ALBUM_ID_ALL)
                }
                LOAD_VIDEO -> {
                    selection = getSelectionMimeType(LOAD_VIDEO, SELECTION_VIDEO_TYPE_ARGS)
                    selectionArgs = getAlbumSelectionVideoType(AlbumData.ALBUM_ID_ALL)
                }
                else -> {
                    selection = getSelectionMimeType(LOAD_ALL, null)
                    selectionArgs = getAlbumSelectionImageAndVideoType(AlbumData.ALBUM_ID_ALL)
                }
            }
            return MediaDataLoader(context, projection, selection, selectionArgs)
        }

        fun createMediaDataLoader(
            context: Context,
            album: AlbumData,
            @LoadMimeType mimeType: Int
        ): CursorLoader {
            val projection = getProjection(mimeType)
            val selection: String
            val selectionArgs: Array<String>
            when (mimeType) {
                LOAD_IMAGE -> {
                    selection = getSelectionMimeType(LOAD_IMAGE, SELECTION_IMAGE_TYPE_ARGS, album.id)
                    selectionArgs = getAlbumSelectionImageType(album.id)
                }
                LOAD_VIDEO -> {
                    selection = getSelectionMimeType(LOAD_VIDEO, SELECTION_VIDEO_TYPE_ARGS, album.id)
                    selectionArgs = getAlbumSelectionVideoType(album.id)
                }
                else -> {
                    selection = getSelectionMimeType(LOAD_ALL, null, album.id)
                    selectionArgs = getAlbumSelectionImageAndVideoType(album.id)
                }
            }
            return MediaDataLoader(context, projection, selection, selectionArgs)
        }

        private fun getProjection(@LoadMimeType mimeType: Int): Array<String> = when (mimeType) {
            LOAD_IMAGE -> PROJECTION_IMAGE
            LOAD_VIDEO -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) PROJECTION_VIDEO_Q else PROJECTION_VIDEO
            else -> PROJECTION_ALL
        }

        private fun getAlbumSelectionImageType(bucketId: String): Array<String> {
            if (bucketId == AlbumData.ALBUM_ID_ALL) {
                return SELECTION_IMAGE_TYPE_ARGS
            }
            val selectionType: MutableList<String> = ArrayList()
            Collections.addAll(selectionType, *SELECTION_IMAGE_TYPE_ARGS)
            selectionType.add(bucketId)
            return selectionType.toTypedArray()
        }

        private fun getAlbumSelectionVideoType(bucketId: String): Array<String> {
            if (bucketId == AlbumData.ALBUM_ID_ALL) {
                return SELECTION_VIDEO_TYPE_ARGS
            }
            val selectionType: MutableList<String> = ArrayList()
            Collections.addAll(selectionType, *SELECTION_VIDEO_TYPE_ARGS)
            selectionType.add(bucketId)
            return selectionType.toTypedArray()
        }

        fun getAlbumSelectionImageAndVideoType(bucketId: String): Array<String> {
            return if (bucketId == AlbumData.ALBUM_ID_ALL) {
                SELECTION_ALL_TYPE_ARGS
            } else arrayOf(
                MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString(),
                MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO.toString(),
                bucketId
            )
        }

        private fun getSelectionMimeType(@LoadMimeType mediaType: Int, mimeTypeArgs: Array<String>?): String {
            return getSelectionMimeType(mediaType, mimeTypeArgs, "")
        }

        private fun getSelectionMimeType(
            @LoadMimeType mediaType: Int,
            mimeTypeArgs: Array<String>?,
            bucketId: String?
        ): String {
            val builder = StringBuilder(MEDIA_SIZE)
            builder.append(" and ")
            if (mediaType == LOAD_ALL) {
                builder.append("(")
                builder.append(MediaStore.Files.FileColumns.MEDIA_TYPE + "=?")
                builder.append(" or ")
                builder.append(MediaStore.Files.FileColumns.MEDIA_TYPE + "=?")
                builder.append(")")
            } else {
                builder.append(MediaStore.Files.FileColumns.MEDIA_TYPE + "=")
                builder.append(
                    if (mediaType == LOAD_IMAGE) MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
                    else MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO
                )
            }
            if (mimeTypeArgs != null && mimeTypeArgs.isNotEmpty()) {
                builder.append(" and (")
                for (i in mimeTypeArgs.indices) {
                    if (i != 0) builder.append(" or ")
                    builder.append(MediaStore.MediaColumns.MIME_TYPE + "=?")
                }
                builder.append(")")
            }
            if (!TextUtils.isEmpty(bucketId) && bucketId != AlbumData.ALBUM_ID_ALL) {
                builder.append(" and bucket_id=?")
            }
            return builder.toString()
        }
    }
}
