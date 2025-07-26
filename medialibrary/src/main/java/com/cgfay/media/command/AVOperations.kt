package com.cgfay.media.command

import android.content.Context
import android.media.MediaExtractor
import android.media.MediaFormat
import android.os.Environment
import android.util.Log
import com.cgfay.uitls.utils.FileUtils
import java.io.File
import java.io.RandomAccessFile

object AVOperations {
    fun writeConcatToFile(content: List<String>, fileName: String) {
        val strContent = buildString {
            content.forEach { append("file $it\r\n") }
        }
        try {
            val file = File(fileName)
            if (file.isFile && file.exists()) {
                file.delete()
            }
            file.parentFile?.mkdirs()
            file.createNewFile()
            RandomAccessFile(file, "rwd").use { raf ->
                raf.seek(file.length())
                raf.write(strContent.toByteArray())
            }
            Log.e("AVOperations", "concat path:$fileName")
        } catch (e: Exception) {
            Log.e("AVOperations", "Error on write File:$e")
        }
    }

    fun generateConcatPath(context: Context): String {
        val directoryPath = if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()) {
            context.externalCacheDir!!.absolutePath
        } else {
            context.cacheDir.absolutePath
        }
        val path = directoryPath + File.separator + "ff_concat.txt"
        FileUtils.deleteFile(path)
        val file = File(path)
        if (!file.parentFile.exists()) {
            file.parentFile.mkdirs()
        }
        return path
    }

    fun getDuration(url: String): Long {
        return try {
            val mediaExtractor = MediaExtractor()
            mediaExtractor.setDataSource(url)
            var videoExt = selectVideoTrack(mediaExtractor)
            if (videoExt == -1) {
                videoExt = selectAudioTrack(mediaExtractor)
                if (videoExt == -1) {
                    return 0
                }
            }
            val mediaFormat = mediaExtractor.getTrackFormat(videoExt)
            val res = if (mediaFormat.containsKey(MediaFormat.KEY_DURATION)) mediaFormat.getLong(MediaFormat.KEY_DURATION) else 0
            mediaExtractor.release()
            res
        } catch (e: Exception) {
            0
        }
    }

    private fun selectVideoTrack(extractor: MediaExtractor): Int {
        val numTracks = extractor.trackCount
        for (i in 0 until numTracks) {
            val format = extractor.getTrackFormat(i)
            val mime = format.getString(MediaFormat.KEY_MIME) ?: continue
            if (mime.startsWith("video/")) return i
        }
        return -1
    }

    private fun selectAudioTrack(extractor: MediaExtractor): Int {
        val numTracks = extractor.trackCount
        for (i in 0 until numTracks) {
            val format = extractor.getTrackFormat(i)
            val mime = format.getString(MediaFormat.KEY_MIME) ?: continue
            if (mime.startsWith("audio/")) return i
        }
        return -1
    }
}
