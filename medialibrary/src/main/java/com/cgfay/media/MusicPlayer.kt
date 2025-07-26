package com.cgfay.media

import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import com.cgfay.media.annotations.AccessedByNative
import com.cgfay.uitls.utils.NativeLibraryLoader
import java.io.Closeable
import java.io.IOException
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
        @JvmStatic
        private fun postEventFromNative(mediaplayerRef: Any, what: Int, arg1: Int, arg2: Int, obj: Any?) {
            val mp = (mediaplayerRef as WeakReference<*>).get() as? MusicPlayer ?: return
            mp.mEventHandler?.let { handler ->
                val m = handler.obtainMessage(what, arg1, arg2, obj)
                handler.sendMessage(m)
            }
        }
    }

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

    @AccessedByNative
    private var mNativeContext: Long = 0
    private var mEventHandler: EventHandler?

    init {
        val looper = Looper.myLooper() ?: Looper.getMainLooper()
        mEventHandler = if (looper != null) EventHandler(this, looper) else null
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

    @Throws(IllegalStateException::class)
    fun prepare() {
        _prepare()
    }

    @Throws(IllegalStateException::class)
    fun start() {
        _start()
    }

    @Throws(IllegalStateException::class)
    fun pause() {
        _pause()
    }

    @Throws(IllegalStateException::class)
    fun stop() {
        _stop()
    }

    @Throws(IllegalStateException::class)
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
                    val errorWasHandled = mOnErrorListener?.onError(mMusicPlayer, msg.arg1, msg.arg2) ?: false
                    if (!errorWasHandled) {
                        mOnCompletionListener?.onCompletion(mMusicPlayer)
                    }
                }
                MEDIA_INFO -> {}
                MEDIA_CURRENT -> mOnCurrentPositionListener?.onCurrentPosition(mMusicPlayer, msg.arg1.toFloat(), msg.arg2.toFloat())
                else -> Log.e(TAG, "Unknown message type " + msg.what)
            }
        }
    }

    interface OnPreparedListener { fun onPrepared(mp: MusicPlayer) }
    interface OnCompletionListener { fun onCompletion(mp: MusicPlayer) }
    interface OnSeekCompleteListener { fun onSeekComplete(mp: MusicPlayer) }
    interface OnErrorListener { fun onError(mp: MusicPlayer, what: Int, extra: Int): Boolean }
    interface OnCurrentPositionListener { fun onCurrentPosition(mp: MusicPlayer, current: Float, duration: Float) }

    fun setOnPreparedListener(listener: OnPreparedListener?) { mOnPreparedListener = listener }
    fun setOnCompletionListener(listener: OnCompletionListener?) { mOnCompletionListener = listener }
    fun setOnSeekCompleteListener(listener: OnSeekCompleteListener?) { mOnSeekCompleteListener = listener }
    fun setOnErrorListener(listener: OnErrorListener?) { mOnErrorListener = listener }
    fun setOnCurrentPositionListener(listener: OnCurrentPositionListener?) { mOnCurrentPositionListener = listener }

    private var mOnPreparedListener: OnPreparedListener? = null
    private var mOnCompletionListener: OnCompletionListener? = null
    private var mOnSeekCompleteListener: OnSeekCompleteListener? = null
    private var mOnErrorListener: OnErrorListener? = null
    private var mOnCurrentPositionListener: OnCurrentPositionListener? = null

    private companion object {
        const val MEDIA_PREPARED = 1
        const val MEDIA_STARTED = 2
        const val MEDIA_PLAYBACK_COMPLETE = 3
        const val MEDIA_SEEK_COMPLETE = 4
        const val MEDIA_ERROR = 100
        const val MEDIA_INFO = 200
        const val MEDIA_CURRENT = 300
    }
}
