package com.cgfay.media.command

import android.os.Handler
import android.os.HandlerThread
import com.cgfay.media.FFmpegUtils

class CommandExecutor {
    private val handler: Handler

    init {
        val thread = HandlerThread("cain_command_executor")
        thread.start()
        handler = Handler(thread.looper)
    }

    fun release() {
        handler.looper.quitSafely()
    }

    fun execCommand(cmd: Array<String>, callback: CommandProcessCallback?) {
        if (cmd.isEmpty()) {
            callback?.onProcessResult(-1)
            return
        }
        handler.post {
            val ret = FFmpegUtils.execute(cmd)
            callback?.onProcessResult(ret)
        }
    }

    fun interface CommandProcessCallback {
        fun onProcessResult(result: Int)
    }
}
