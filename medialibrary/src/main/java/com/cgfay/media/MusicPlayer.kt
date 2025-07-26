package com.cgfay.media

import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import com.cgfay.uitls.utils.NativeLibraryLoader
import com.cgfay.media.annotations.AccessedByNative
import java.io.Closeable
import java.lang.ref.WeakReference

class MusicPlayer : Closeable {

    companion object {
        private const val TAG = "MusicPlayer"
        init {
            NativeLibraryLoader.loadLibraries(
                "ffmpeg",
                "musicplayer"
            )
            native_init()
        }
        @JvmStatic
        private external fun native_init()
        private const val MEDIA_PREPARED = 1
        private const val MEDIA_STARTED = 2
        private const val MEDIA_PLAYBACK_COMPLETE = 3
        private const val MEDIA_SEEK_COMPLETE = 4
        private const val MEDIA_ERROR = 100
        private const val MEDIA_INFO = 200
        private const val MEDIA_CURRENT = 300
        @JvmStatic
        private fun postEventFromNative(mediaplayer_ref: Any, what: Int, arg1: Int, arg2: Int, obj: Any?) {
            val mp = (mediaplayer_ref as WeakReference<*>).get() as? MusicPlayer ?: return
            mp.mEventHandler?.let {
                val m = it.obtainMessage(what, arg1, arg2, obj)
                it.sendMessage(m)
            }
        }

    }

    // 初始化
    private external fun native_setup(mediaplayer_this: Any)
    private external fun native_finalize()
    // 释放资源
    private external fun _release()
    // 设置音乐路径
    private external fun _setDataSource(path: String)
    // 设置音乐播放速度
    private external fun _setSpeed(speed: Float)
    // 设置是否重新播放
    private external fun _setLooping(looping: Boolean)
    // 设置播放区间
    private external fun _setRange(start: Float, end: Float)
    // 设置播放声音
    private external fun _setVolume(leftVolume: Float, rightVolume: Float)
    // 准备播放器
    private external fun _prepare()
    // 开始播放
    private external fun _start()
    // 暂停播放
    private external fun _pause()
    // 停止播放
    private external fun _stop()
    // 定位
    private external fun _seekTo(timeMs: Float)
    // 获取时长
    private external fun _getDuration(): Float
    // 是否循环播放
    private external fun _isLooping(): Boolean
    // 是否正在播放中
    private external fun _isPlaying(): Boolean

    @AccessedByNative
    private var mNativeContext: Long = 0
    private var mEventHandler: EventHandler?

    init {
        val looper = Looper.myLooper() ?: Looper.getMainLooper()
        mEventHandler = looper?.let { EventHandler(this, it) }

        native_setup(WeakReference(this))
    }

    fun release() {
        mOnPreparedListener = null
        mOnCompletionListener = null
        mOnSeekCompleteListener = null
        mOnErrorListener = null
        mOnCurrentPositionListener = null
        _release()
    }

    override fun close() {
        release()
    }

    fun setDataSource(path: String) {
        _setDataSource(path)
    }

    fun setSpeed(speed: Float) {
        _setSpeed(speed)
    }

    fun setLooping(looping: Boolean) {
        _setLooping(looping)
    }

    fun setRange(startMs: Float, endMs: Float) {
        _setRange(startMs, endMs)
    }

    fun setVolume(leftVolume: Float, rightVolume: Float) {
        _setVolume(leftVolume, rightVolume)
    }

    fun prepare() { _prepare() }
    fun start() { _start() }
    fun pause() { _pause() }
    fun stop() { _stop() }
    fun seekTo(timeMs: Float) { _seekTo(timeMs) }
    fun getDuration(): Float = _getDuration()
    fun isLooping(): Boolean = _isLooping()
    fun isPlaying(): Boolean = _isPlaying()

    private inner class EventHandler(mp: MusicPlayer, looper: Looper) : Handler(looper) {
        private val mMusicPlayer: MusicPlayer = mp
        override fun handleMessage(msg: Message) {
            if (mMusicPlayer.mNativeContext == 0L) {
                Log.w(TAG, "musicplayer went away with unhandled events")
                return
            }
            when (msg.what) {
                MEDIA_PREPARED -> mOnPreparedListener?.onPrepared(mMusicPlayer)
                MEDIA_PLAYBACK_COMPLETE -> mOnCompletionListener?.onCompletion(mMusicPlayer)
                MEDIA_STARTED -> Log.d(TAG, "music player is started!")
                MEDIA_SEEK_COMPLETE -> mOnSeekCompleteListener?.onSeekComplete(mMusicPlayer)
                MEDIA_ERROR -> {
                    Log.e(TAG, "Error(" + msg.arg1 + "," + msg.arg2 + ")")
                    var handled = mOnErrorListener?.onError(mMusicPlayer, msg.arg1, msg.arg2) ?: false
                    if (!handled) mOnCompletionListener?.onCompletion(mMusicPlayer)
                }
                MEDIA_INFO -> { /* ignore */ }
                MEDIA_CURRENT -> mOnCurrentPositionListener?.onCurrentPosition(mMusicPlayer, msg.arg1.toFloat(), msg.arg2.toFloat())
                else -> Log.e(TAG, "Unknown message type ${msg.what}")

    interface OnPreparedListener {
        fun onPrepared(mp: MusicPlayer)
    }
    private var mOnPreparedListener: OnPreparedListener? = null
    fun setOnPreparedListener(listener: OnPreparedListener?) {
        mOnPreparedListener = listener
    }

    interface OnCompletionListener {
        fun onCompletion(mp: MusicPlayer)
    }
    private var mOnCompletionListener: OnCompletionListener? = null
    fun setOnCompletionListener(listener: OnCompletionListener?) {
        mOnCompletionListener = listener
    }

    interface OnSeekCompleteListener {
        fun onSeekComplete(mp: MusicPlayer)
    }
    private var mOnSeekCompleteListener: OnSeekCompleteListener? = null
    fun setOnSeekCompleteListener(listener: OnSeekCompleteListener?) {
        mOnSeekCompleteListener = listener
    }

    interface OnErrorListener {
        fun onError(mp: MusicPlayer, what: Int, extra: Int): Boolean
    }
    private var mOnErrorListener: OnErrorListener? = null
    fun setOnErrorListener(listener: OnErrorListener?) {
        mOnErrorListener = listener
    }

    interface OnCurrentPositionListener {
        fun onCurrentPosition(mp: MusicPlayer, current: Float, duration: Float)
    }
    private var mOnCurrentPositionListener: OnCurrentPositionListener? = null
    fun setOnCurrentPositionListener(listener: OnCurrentPositionListener?) {
        mOnCurrentPositionListener = listener
    }

    // Error codes
    object Errors {
        const val MEDIA_ERROR_UNKNOWN = 1
        const val MEDIA_ERROR_SERVER_DIED = 100
        const val MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK = 200
    }
}
