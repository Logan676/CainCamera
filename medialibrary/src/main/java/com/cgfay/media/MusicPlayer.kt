package com.cgfay.media

import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import com.cgfay.uitls.utils.NativeLibraryLoader
import com.cgfay.media.annotations.AccessedByNative
import java.io.Closeable
import java.io.IOException
import java.lang.ref.WeakReference

class MusicPlayer : Closeable {

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

    @Throws(IOException::class, IllegalArgumentException::class, SecurityException::class, IllegalStateException::class)
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

    fun prepare() {
        _prepare()
    }

    fun start() {
        _start()
    }

    fun pause() {
        _pause()
    }

    fun stop() {
        _stop()
    }

    fun seekTo(timeMs: Float) {
        _seekTo(timeMs)
    }

    val duration: Float
        get() = _getDuration()

    val isLooping: Boolean
        get() = _isLooping()

    val isPlaying: Boolean
        get() = _isPlaying()

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
                    Log.e(TAG, "Error (" + msg.arg1 + "," + msg.arg2 + ")")
                    var errorWasHandled = false
                    if (mOnErrorListener != null) {
                        errorWasHandled = mOnErrorListener!!.onError(mMusicPlayer, msg.arg1, msg.arg2)
                    }
                    if (mOnCompletionListener != null && !errorWasHandled) {
                        mOnCompletionListener!!.onCompletion(mMusicPlayer)
                    }
                }
                MEDIA_INFO -> {
                    // ignore
                }
                MEDIA_CURRENT -> mOnCurrentPositionListener?.onCurrentPosition(mMusicPlayer, msg.arg1.toFloat(), msg.arg2.toFloat())
                else -> Log.e(TAG, "Unknown message type " + msg.what)
            }
        }
    }

    interface OnPreparedListener {
        fun onPrepared(mp: MusicPlayer)
    }

    fun setOnPreparedListener(listener: OnPreparedListener?) {
        mOnPreparedListener = listener
    }
    private var mOnPreparedListener: OnPreparedListener? = null

    interface OnCompletionListener {
        fun onCompletion(mp: MusicPlayer)
    }

    fun setOnCompletionListener(listener: OnCompletionListener?) {
        mOnCompletionListener = listener
    }
    private var mOnCompletionListener: OnCompletionListener? = null

    interface OnSeekCompleteListener {
        fun onSeekComplete(mp: MusicPlayer)
    }

    fun setOnSeekCompleteListener(listener: OnSeekCompleteListener?) {
        mOnSeekCompleteListener = listener
    }
    private var mOnSeekCompleteListener: OnSeekCompleteListener? = null

    companion object {
        private const val TAG = "MusicPlayer"
        private external fun native_init()
        init {
            NativeLibraryLoader.loadLibraries(
                "ffmpeg",
                "musicplayer"
            )
            native_init()
        }
        private const val MEDIA_PREPARED = 1
        private const val MEDIA_STARTED = 2
        private const val MEDIA_PLAYBACK_COMPLETE = 3
        private const val MEDIA_SEEK_COMPLETE = 4
        private const val MEDIA_ERROR = 100
        private const val MEDIA_INFO = 200
        private const val MEDIA_CURRENT = 300

        private fun postEventFromNative(mediaplayer_ref: Any, what: Int, arg1: Int, arg2: Int, obj: Any?) {
            val mp = (mediaplayer_ref as WeakReference<*>).get() as? MusicPlayer ?: return
            mp.mEventHandler?.let {
                val m = it.obtainMessage(what, arg1, arg2, obj)
                it.sendMessage(m)
            }
        }
    }

    interface OnErrorListener {
        fun onError(mp: MusicPlayer, what: Int, extra: Int): Boolean
    }

    fun setOnErrorListener(listener: OnErrorListener?) {
        mOnErrorListener = listener
    }
    private var mOnErrorListener: OnErrorListener? = null

    interface OnCurrentPositionListener {
        fun onCurrentPosition(mp: MusicPlayer, current: Float, duration: Float)
    }

    fun setOnCurrentPositionListener(listener: OnCurrentPositionListener?) {
        mOnCurrentPositionListener = listener
    }
    private var mOnCurrentPositionListener: OnCurrentPositionListener? = null

    // Native methods
    private external fun native_setup(mediaplayer_this: Any)
    private external fun native_finalize()
    private external fun _release()
    private external fun _setDataSource(path: String)
    private external fun _setSpeed(speed: Float)
    private external fun _setLooping(looping: Boolean)
    private external fun _setRange(start: Float, end: Float)
    private external fun _setVolume(leftVolume: Float, rightVolume: Float)
    private external fun _prepare()
    private external fun _start()
    private external fun _pause()
    private external fun _stop()
    private external fun _seekTo(timeMs: Float)
    private external fun _getDuration(): Float
    private external fun _isLooping(): Boolean
    private external fun _isPlaying(): Boolean
}
