package com.cgfay.media

import com.cgfay.uitls.utils.NativeLibraryLoader

object FFmpegUtils {
    private const val TAG = "FFmpegUtils"

    init {
        NativeLibraryLoader.loadLibraries(
            "ffmpeg",
            "ffcommand"
        )
    }

    private external fun _execute(command: Array<String>): Int

    fun execute(commands: Array<String>): Int {
        return _execute(commands)
    }
}
