package com.cgfay.media.command

import android.content.Context
import java.io.File
import java.io.RandomAccessFile
import java.util.Locale
import com.cgfay.uitls.utils.FileUtils

object CommandBuilder {

    fun mergeAudioVideo(videoPath: String, audioPath: String, output: String): Array<String> {
        val cmdList = mutableListOf<String>()
        val duration = AVOperations.getDuration(audioPath) / 1_000_000f
        cmdList.add("ffmpeg")
        cmdList.add("-i")
        cmdList.add(audioPath)
        cmdList.add("-i")
        cmdList.add(videoPath)
        cmdList.add("-ss")
        cmdList.add("0")
        cmdList.add("-t")
        cmdList.add("" + duration)
        cmdList.add("-acodec")
        cmdList.add("copy")
        cmdList.add("-vcodec")
        cmdList.add("copy")
        cmdList.add("-y")
        cmdList.add(output)
        return cmdList.toTypedArray()
    }

    fun concatVideo(context: Context, videos: List<String>, output: String): Array<String> {
        val concatPath = AVOperations.generateConcatPath(context)
        AVOperations.writeConcatToFile(videos, concatPath)
        val cmdList = mutableListOf<String>()
        cmdList.add("ffmpeg")
        cmdList.add("-f")
        cmdList.add("concat")
        cmdList.add("-safe")
        cmdList.add("0")
        cmdList.add("-i")
        cmdList.add(concatPath)
        cmdList.add("-c")
        cmdList.add("copy")
        cmdList.add("-threads")
        cmdList.add("5")
        cmdList.add("-y")
        cmdList.add(output)
        return cmdList.toTypedArray()
    }

    fun audioVideoMix(videoPath: String, audioPath: String, dstPath: String, videoVolume: Float, audioVolume: Float): Array<String> {
        val cmdList = mutableListOf<String>()
        cmdList.add("ffmpeg")
        cmdList.add("-i")
        cmdList.add(videoPath)
        cmdList.add("-i")
        cmdList.add(audioPath)
        cmdList.add("-c:v")
        cmdList.add("copy")
        cmdList.add("-map")
        cmdList.add("0:v:0")
        cmdList.add("-strict")
        cmdList.add("-2")
        when {
            videoVolume == 0.0f -> {
                cmdList.add("-c:a")
                cmdList.add("aac")
                cmdList.add("-map")
                cmdList.add("1:a:0")
                cmdList.add("-shortest")
                if (audioVolume < 0.99f || audioVolume > 1.01f) {
                    cmdList.add("-vol")
                    cmdList.add("${(audioVolume * 100).toInt()}")
                }
            }
            videoVolume > 0.001f && audioVolume > 0.001f -> {
                cmdList.add("-filter_complex")
                cmdList.add(String.format(Locale.getDefault(), "[0:a]aformat=sample_fmts=fltp:sample_rates=48000:channel_layouts=stereo,volume=%f[a0];[1:a]aformat=sample_fmts=fltp:sample_rates=48000:channel_layouts=stereo,volume=%f[a1];[a0][a1]amix=inputs=2:duration=first[aout]", videoVolume, audioVolume))
                cmdList.add("-map")
                cmdList.add("[aout]")
            }
            else -> {
                android.util.Log.w("CommandBuilder", String.format(Locale.getDefault(), "Illigal volume : SrcVideo = %.2f, SrcAudio = %.2f", videoVolume, audioVolume))
            }
        }
        cmdList.add("-f")
        cmdList.add("mp4")
        cmdList.add("-y")
        cmdList.add("-movflags")
        cmdList.add("faststart")
        cmdList.add(dstPath)
        return cmdList.toTypedArray()
    }

    fun audioCut(srcPath: String, dstPath: String, start: Int, duration: Int): Array<String> {
        val cmdList = mutableListOf<String>()
        cmdList.add("ffmpeg")
        cmdList.add("-i")
        cmdList.add(srcPath)
        cmdList.add("-ss")
        cmdList.add("${start / 1000}")
        cmdList.add("-t")
        cmdList.add("${duration / 1000}")
        cmdList.add("-vn")
        cmdList.add("-acodec")
        cmdList.add("copy")
        cmdList.add("-y")
        cmdList.add(dstPath)
        return cmdList.toTypedArray()
    }
}
