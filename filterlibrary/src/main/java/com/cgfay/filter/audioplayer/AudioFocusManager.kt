package com.cgfay.filter.audioplayer

import android.content.Context
import android.media.AudioManager

/**
 * 音频对焦管理器
 */
class AudioFocusManager private constructor() {

    // 音频管理器
    private var audioManager: AudioManager? = null

    // 音频管理器状态
    private var state: Int = AudioManager.AUDIOFOCUS_LOSS_TRANSIENT

    // 监听器集合
    private val listenerSet: MutableSet<AudioFocusChangeListener> = HashSet()

    companion object {
        val instance: AudioFocusManager by lazy { AudioFocusManager() }
    }

    /**
     * 初始化管理器
     */
    fun init(context: Context) {
        audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

    /**
     * 释放管理器
     */
    fun release() {
        state = AudioManager.AUDIOFOCUS_GAIN_TRANSIENT
        listenerSet.clear()
        audioManager?.abandonAudioFocus(focusChangeListener)
        audioManager = null
    }

    /**
     * 添加音频状态监听器
     */
    fun addAudioFocusChangeListener(listener: AudioFocusChangeListener): AudioFocusManager {
        listenerSet.add(listener)
        return this
    }

    /**
     * 移除音频状态监听器
     */
    fun removeAudioFocusChangeListener(listener: AudioFocusChangeListener): AudioFocusManager {
        listenerSet.remove(listener)
        return this
    }

    /**
     * 是否处于对焦状态
     */
    val isFocused: Boolean
        get() = state == AudioManager.AUDIOFOCUS_GAIN

    /**
     * 请求音频对焦
     */
    @Synchronized
    fun requestAudioFocus(): AudioFocusManager {
        if (state != 1) {
            audioManager?.let {
                state = it.requestAudioFocus(
                    focusChangeListener,
                    AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN
                )
            }
        }
        onFocusChanged()
        return this
    }

    /**
     * 对焦状态发生变化时调用
     */
    private fun onFocusChanged() {
        val hashSet: HashSet<AudioFocusChangeListener>
        synchronized(this) {
            hashSet = HashSet(listenerSet)
        }
        for (listener in hashSet) {
            listener.onFocusChange(state)
        }
    }

    /**
     * 对焦状态监听器
     */
    private val focusChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
            audioManager?.abandonAudioFocus(focusChangeListener)
        }
        state = focusChange
        onFocusChanged()
    }
}
