package com.cgfay.filter.audioplayer

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer

/**
 * 带自动对焦的播放器
 */
open class AutoFocusPlayer(context: Context) : MediaPlayer() {

    // 音频对焦管理器
    private val focusManager: AudioFocusManager = AudioFocusManager.instance.apply {
        init(context)
        addAudioFocusChangeListener(focusChangeListener)
    }

    override fun start() {
        if (focusManager.isFocused) {
            super.start()
            onStart()
        } else {
            focusManager.requestAudioFocus()
        }
    }

    override fun release() {
        super.release()
        focusManager.removeAudioFocusChangeListener(focusChangeListener)
        onRelease()
    }

    /** 开始播放 */
    protected open fun onStart() {}

    /** 释放资源 */
    protected open fun onRelease() {}

    /** 开始对焦 */
    protected open fun startFocus() {
        start()
    }

    /** 失去对焦 */
    protected open fun lossFocus() {
        if (isPlaying) {
            pause()
        }
    }

    /** 音频对焦监听器 */
    private val focusChangeListener = AudioFocusChangeListener { state ->
        when (state) {
            AudioManager.AUDIOFOCUS_GAIN -> startFocus()
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT, AudioManager.AUDIOFOCUS_LOSS -> lossFocus()
            else -> {}
        }
    }
}
