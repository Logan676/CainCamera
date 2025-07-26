package com.cgfay.media

import android.content.ContentResolver
import android.content.Context
import android.content.res.AssetFileDescriptor
import android.graphics.SurfaceTexture
import android.media.AudioManager
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.os.PowerManager
import android.util.Log
import android.view.Surface
import android.view.SurfaceHolder
import com.cgfay.media.annotations.AccessedByNative
import com.cgfay.uitls.utils.NativeLibraryLoader
import java.io.FileDescriptor
import java.io.IOException
import java.lang.ref.WeakReference
import java.util.Map

class VideoPlayer : IMediaPlayer {

    companion object {
        private const val TAG = "VideoPlayer"

        init {
            NativeLibraryLoader.loadLibraries(
                "ffmpeg",
                "yuv",
                "videoplayer"
            )
            native_init()
        }

        @JvmStatic
        private external fun native_init()
        @JvmStatic
        private fun postEventFromNative(ref: Any, what: Int, arg1: Int, arg2: Int, obj: Any?) {
            val mp = (ref as WeakReference<*>).get() as? VideoPlayer ?: return
            mp.mEventHandler?.let {
                val m = it.obtainMessage(what, arg1, arg2, obj)
                it.sendMessage(m)
            }
        }

        fun create(context: Context, uri: Uri, holder: SurfaceHolder? = null): VideoPlayer? {
            return try {
                val mp = VideoPlayer()
                mp.setDataSource(context, uri)
                holder?.let { mp.setDisplay(it) }
                mp.prepare()
                mp
            } catch (ex: Exception) {
                Log.d(TAG, "create failed:", ex)
                null
            }
        }

        fun create(context: Context, resid: Int): VideoPlayer? {
            return try {
                val afd = context.resources.openRawResourceFd(resid) ?: return null
                val mp = VideoPlayer()
                mp.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                afd.close()
                mp.prepare()
                mp
            } catch (ex: Exception) {
                Log.d(TAG, "create failed:", ex)
                null
            }
        }
    }

    private external fun native_setup(thiz: Any)
    private external fun native_finalize()
    private external fun _release()
    private external fun _reset()
    private external fun _setDataSource(path: String)
    private external fun _setDataSource(path: String, keys: Array<String>?, values: Array<String>?)
    private external fun _setDataSource(fd: FileDescriptor, offset: Long, length: Long)
    private external fun _setAudioDecoder(decoder: String)
    private external fun _setVideoDecoder(decoder: String)
    private external fun _setVideoSurface(surface: Surface?)
    private external fun _setSpeed(speed: Float)
    private external fun _setLooping(looping: Boolean)
    private external fun _setRange(start: Float, end: Float)
    private external fun _setVolume(leftVolume: Float, rightVolume: Float)
    private external fun _setMute(mute: Boolean)
    private external fun _prepare()
    private external fun _start()
    private external fun _pause()
    private external fun _resume()
    private external fun _stop()
    private external fun _setDecodeOnPause(decodeOnPause: Boolean)
    private external fun _seekTo(timeMs: Float)
    private external fun _getCurrentPosition(): Long
    private external fun _getDuration(): Long
    private external fun _getRotate(): Int
    private external fun _getVideoWidth(): Int
    private external fun _getVideoHeight(): Int
    private external fun _isLooping(): Boolean
    private external fun _isPlaying(): Boolean

    @AccessedByNative
    private var mNativeContext: Long = 0
    private var mSurfaceHolder: SurfaceHolder? = null
    private var mEventHandler: EventHandler?
    private var mWakeLock: PowerManager.WakeLock? = null
    private var mScreenOnWhilePlaying = false
    private var mStayAwake = false

    init {
        val looper = Looper.myLooper() ?: Looper.getMainLooper()
        mEventHandler = if (looper != null) EventHandler(this, looper) else null
        native_setup(WeakReference(this))
    }

    override fun setDisplay(sh: SurfaceHolder?) {
        mSurfaceHolder = sh
        val surface = sh?.surface
        _setVideoSurface(surface)
        updateSurfaceScreenOn()
    }

    override fun setSurface(surface: Surface?) {
        if (mScreenOnWhilePlaying && surface != null) {
            Log.w(TAG, "setScreenOnWhilePlaying(true) is ineffective for Surface")
        }
        mSurfaceHolder = null
        _setVideoSurface(surface)
        updateSurfaceScreenOn()
    }

    override fun setDataSource(context: Context, uri: Uri) {
        setDataSource(context, uri, null)
    }

    override fun setDataSource(context: Context, uri: Uri, headers: Map<String, String>?) {
        val scheme = uri.scheme
        if (scheme == null || scheme == "file") {
            setDataSource(uri.path!!)
            return
        }
        var fd: AssetFileDescriptor? = null
        try {
            val resolver: ContentResolver = context.contentResolver
            fd = resolver.openAssetFileDescriptor(uri, "r")
            if (fd == null) return
            if (fd.declaredLength < 0) {
                setDataSource(fd.fileDescriptor)
            } else {
                setDataSource(fd.fileDescriptor, fd.startOffset, fd.declaredLength)
            }
            return
        } catch (_: Exception) {
        } finally {
            try { fd?.close() } catch (_: IOException) {}
        }
        Log.d(TAG, "Couldn't open file on client side, trying server side")
        setDataSource(uri.toString(), headers)
    }

    fun setDataSource(path: String) {
        _setDataSource(path)
    }

    override fun setDataSource(path: String, headers: Map<String, String>?) {
        var keys: Array<String>? = null
        var values: Array<String>? = null
        if (headers != null) {
            keys = Array(headers.size) { "" }
            values = Array(headers.size) { "" }
            var i = 0
            for ((k, v) in headers) {
                keys[i] = k
                values[i] = v
                i++
            }
        }
        _setDataSource(path, keys, values)
    }

    override fun setDataSource(fd: FileDescriptor) {
        setDataSource(fd, 0, 0x7ffffffffffffffL)
    }

    fun setDataSource(fd: FileDescriptor, offset: Long, length: Long) {
        _setDataSource(fd, offset, length)
    }

    override fun prepare() { _prepare() }
    fun prepareAsync() { /* not implemented previously */ }
    fun start() { _start() }
    fun pause() { _pause() }
    fun resume() { _resume() }
    fun stop() { _stop() }
    fun setDecodeOnPause(decodeOnPause: Boolean) { _setDecodeOnPause(decodeOnPause) }
    override fun seekTo(msec: Float) { _seekTo(msec) }

    override fun getCurrentPosition(): Long = _getCurrentPosition()
    override fun getDuration(): Long = _getDuration()
    override fun getRotate(): Int = _getRotate()
    override fun getVideoWidth(): Int = _getVideoWidth()
    override fun getVideoHeight(): Int = _getVideoHeight()
    override fun isPlaying(): Boolean = _isPlaying()
    override fun isLooping(): Boolean = _isLooping()

    override fun release() {
        stayAwake(false)
        updateSurfaceScreenOn()
        mOnPreparedListener = null
        mOnBufferingUpdateListener = null
        mOnCompletionListener = null
        mOnSeekCompleteListener = null
        mOnErrorListener = null
        mOnInfoListener = null
        mOnVideoSizeChangedListener = null
        mOnCurrentPositionListener = null
        _release()
    }

    override fun reset() {
        stayAwake(false)
        _reset()
        mEventHandler?.removeCallbacksAndMessages(null)
    }

    override fun setAudioStreamType(streamtype: Int) { /* no-op */ }
    override fun setLooping(looping: Boolean) { _setLooping(looping) }
    override fun setVolume(leftVolume: Float, rightVolume: Float) { _setVolume(leftVolume, rightVolume) }
    override fun setAudioSessionId(sessionId: Int) {}
    override fun getAudioSessionId(): Int = 0
    override fun setMute(mute: Boolean) { _setMute(mute) }

    override fun setScreenOnWhilePlaying(screenOn: Boolean) {
        if (mScreenOnWhilePlaying != screenOn) {
            if (screenOn && mSurfaceHolder == null) {
                Log.w(TAG, "setScreenOnWhilePlaying(true) is ineffective without a SurfaceHolder")
            }
            mScreenOnWhilePlaying = screenOn
            updateSurfaceScreenOn()
        }
    }

    private fun stayAwake(awake: Boolean) {
        mWakeLock?.let {
            if (awake && !it.isHeld) {
                it.acquire()
            } else if (!awake && it.isHeld) {
                it.release()
            }
        }
        mStayAwake = awake
        updateSurfaceScreenOn()
    }

    private fun updateSurfaceScreenOn() {
        mSurfaceHolder?.setKeepScreenOn(mScreenOnWhilePlaying && mStayAwake)
    }

    private inner class EventHandler(mp: VideoPlayer, looper: Looper) : Handler(looper) {
        private val mVideoPlayer = mp
        override fun handleMessage(msg: Message) {
            if (mVideoPlayer.mNativeContext == 0L) {
                Log.w(TAG, "videoplayer went away with unhandled events")
                return
            }
            when (msg.what) {
                MEDIA_PREPARED -> mOnPreparedListener?.onPrepared(mVideoPlayer)
                MEDIA_PLAYBACK_COMPLETE -> mOnCompletionListener?.onCompletion(mVideoPlayer)
                MEDIA_STARTED -> Log.d(TAG, "music player is started!")
                MEDIA_SEEK_COMPLETE -> mOnSeekCompleteListener?.onSeekComplete(mVideoPlayer)
                MEDIA_BUFFERING_UPDATE -> {
                    mOnBufferingUpdateListener?.onBufferingUpdate(mVideoPlayer, msg.arg1)
                    return
                }
                MEDIA_SET_VIDEO_SIZE -> {
                    mOnVideoSizeChangedListener?.onVideoSizeChanged(mVideoPlayer, msg.arg1, msg.arg2)
                    return
                }
                MEDIA_ERROR -> {
                    Log.e(TAG, "Error (" + msg.arg1 + "," + msg.arg2 + ")")
                    val errorWasHandled = mOnErrorListener?.onError(mVideoPlayer, msg.arg1, msg.arg2) ?: false
                    if (!errorWasHandled) {
                        mOnCompletionListener?.onCompletion(mVideoPlayer)
                    }
                }
                MEDIA_INFO -> {
                    if (msg.arg1 != MEDIA_INFO_VIDEO_TRACK_LAGGING) {
                        Log.i(TAG, "Info (" + msg.arg1 + "," + msg.arg2 + ")")
                    }
                    mOnInfoListener?.onInfo(mVideoPlayer, msg.arg1, msg.arg2)
                    return
                }
                MEDIA_CURRENT -> mOnCurrentPositionListener?.onCurrentPosition(mVideoPlayer, msg.arg1.toLong(), msg.arg2.toLong())
                else -> Log.e(TAG, "Unknown message type " + msg.what)
            }
        }
    }

    override fun setOnPreparedListener(listener: IMediaPlayer.OnPreparedListener?) {
        mOnPreparedListener = listener
    }
    override fun setOnCompletionListener(listener: IMediaPlayer.OnCompletionListener?) {
        mOnCompletionListener = listener
    }
    override fun setOnBufferingUpdateListener(listener: IMediaPlayer.OnBufferingUpdateListener?) {
        mOnBufferingUpdateListener = listener
    }
    override fun setOnSeekCompleteListener(listener: IMediaPlayer.OnSeekCompleteListener?) {
        mOnSeekCompleteListener = listener
    }
    override fun setOnVideoSizeChangedListener(listener: IMediaPlayer.OnVideoSizeChangedListener?) {
        mOnVideoSizeChangedListener = listener
    }
    override fun setOnErrorListener(listener: IMediaPlayer.OnErrorListener?) {
        mOnErrorListener = listener
    }
    override fun setOnInfoListener(listener: IMediaPlayer.OnInfoListener?) {
        mOnInfoListener = listener
    }
    override fun setOnCurrentPositionListener(listener: IMediaPlayer.OnCurrentPositionListener?) {
        mOnCurrentPositionListener = listener
    }

    private var mOnPreparedListener: IMediaPlayer.OnPreparedListener? = null
    private var mOnBufferingUpdateListener: IMediaPlayer.OnBufferingUpdateListener? = null
    private var mOnCompletionListener: IMediaPlayer.OnCompletionListener? = null
    private var mOnSeekCompleteListener: IMediaPlayer.OnSeekCompleteListener? = null
    private var mOnErrorListener: IMediaPlayer.OnErrorListener? = null
    private var mOnInfoListener: IMediaPlayer.OnInfoListener? = null
    private var mOnVideoSizeChangedListener: IMediaPlayer.OnVideoSizeChangedListener? = null
    private var mOnCurrentPositionListener: IMediaPlayer.OnCurrentPositionListener? = null

    private companion object {
        const val MEDIA_PREPARED = 1
        const val MEDIA_STARTED = 2
        const val MEDIA_PLAYBACK_COMPLETE = 3
        const val MEDIA_SEEK_COMPLETE = 4
        const val MEDIA_BUFFERING_UPDATE = 5
        const val MEDIA_SET_VIDEO_SIZE = 6
        const val MEDIA_ERROR = 100
        const val MEDIA_INFO = 200
        const val MEDIA_CURRENT = 300
    }
}
