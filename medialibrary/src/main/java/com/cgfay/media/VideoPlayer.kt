package com.cgfay.media

import android.content.ContentResolver
import android.content.Context
import android.content.res.AssetFileDescriptor
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

/**
 * Simple video player backed by native implementation.
 */
class VideoPlayer : IMediaPlayer {

    companion object {
        private const val TAG = "VideoPlayer"

        init {
            NativeLibraryLoader.loadLibraries("ffmpeg", "yuv", "videoplayer")
            native_init()
        }

        @JvmStatic private external fun native_init()
        @JvmStatic
        private fun postEventFromNative(ref: Any, what: Int, arg1: Int, arg2: Int, obj: Any?) {
            val mp = (ref as WeakReference<*>).get() as? VideoPlayer ?: return
            mp.mEventHandler?.sendMessage(mp.mEventHandler!!.obtainMessage(what, arg1, arg2, obj))
        }

        private const val MEDIA_NOP = 0
        private const val MEDIA_PREPARED = 1
        private const val MEDIA_STARTED = 2
        private const val MEDIA_PLAYBACK_COMPLETE = 3
        private const val MEDIA_SEEK_COMPLETE = 4
        private const val MEDIA_BUFFERING_UPDATE = 5
        private const val MEDIA_SET_VIDEO_SIZE = 6
        private const val MEDIA_ERROR = 100
        private const val MEDIA_INFO = 200
        private const val MEDIA_CURRENT = 300
    }

    // native methods
    private external fun native_setup(player: Any)
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
    private external fun _setVolume(left: Float, right: Float)
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
    private var mEventHandler: EventHandler? = null
    private var mWakeLock: PowerManager.WakeLock? = null
    private var mScreenOnWhilePlaying = false
    private var mStayAwake = false
    private var mSessionId = 0

    private var mOnPreparedListener: IMediaPlayer.OnPreparedListener? = null
    private var mOnCompletionListener: IMediaPlayer.OnCompletionListener? = null
    private var mOnBufferingUpdateListener: IMediaPlayer.OnBufferingUpdateListener? = null
    private var mOnSeekCompleteListener: IMediaPlayer.OnSeekCompleteListener? = null
    private var mOnVideoSizeChangedListener: IMediaPlayer.OnVideoSizeChangedListener? = null
    private var mOnErrorListener: IMediaPlayer.OnErrorListener? = null
    private var mOnInfoListener: IMediaPlayer.OnInfoListener? = null
    private var mOnCurrentPositionListener: IMediaPlayer.OnCurrentPositionListener? = null

    init {
        val looper = Looper.myLooper() ?: Looper.getMainLooper()
        mEventHandler = looper?.let { EventHandler(this, it) }
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

    companion object {
        fun create(context: Context, uri: Uri): VideoPlayer? = create(context, uri, null)
        fun create(context: Context, uri: Uri, holder: SurfaceHolder?): VideoPlayer? {
            return try {
                val mp = VideoPlayer()
                mp.setDataSource(context, uri)
                holder?.let { mp.setDisplay(it) }
                mp.prepare()
                mp
            } catch (e: Exception) {
                Log.d(TAG, "create failed:", e)
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
            } catch (e: Exception) {
                Log.d(TAG, "create failed:", e)
                null
            }
        }
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
        } catch (_: SecurityException) {
        } catch (_: IOException) {
        } finally {
            fd?.close()
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
    override fun prepareAsync() { /* not implemented */ }
    override fun start() { _start() }
    override fun stop() { _stop() }
    override fun pause() { _pause() }
    override fun resume() { _resume() }
    override fun setWakeMode(context: Context, mode: Int) {
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        if (mWakeLock != null) {
            if (mWakeLock!!.isHeld) {
                mWakeLock!!.release()
            }
            mWakeLock = null
        }
        mWakeLock = pm.newWakeLock(mode or PowerManager.ON_AFTER_RELEASE, TAG)
        mWakeLock?.setReferenceCounted(false)
    }
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

    override fun getRotate(): Int = _getRotate()
    override fun getVideoWidth(): Int = _getVideoWidth()
    override fun getVideoHeight(): Int = _getVideoHeight()
    override fun isPlaying(): Boolean = _isPlaying()
    override fun seekTo(msec: Float) { _seekTo(msec) }
    override fun getCurrentPosition(): Long = _getCurrentPosition()
    override fun getDuration(): Long = _getDuration()

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
    override fun isLooping(): Boolean = _isLooping()
    override fun setVolume(leftVolume: Float, rightVolume: Float) { _setVolume(leftVolume, rightVolume) }
    override fun setAudioSessionId(sessionId: Int) { mSessionId = sessionId }
    override fun getAudioSessionId(): Int = mSessionId
    override fun setMute(mute: Boolean) { _setMute(mute) }

    fun setAudioDecoder(decoder: String) { _setAudioDecoder(decoder) }
    fun setVideoDecoder(decoder: String) { _setVideoDecoder(decoder) }
    fun setSpeed(speed: Float) { _setSpeed(speed) }
    fun setRange(startMs: Float, endMs: Float) { _setRange(startMs, endMs) }
    fun setDecodeOnPause(decodeOnPause: Boolean) { _setDecodeOnPause(decodeOnPause) }

    protected fun finalize() {
        native_finalize()
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
                MEDIA_BUFFERING_UPDATE -> mOnBufferingUpdateListener?.onBufferingUpdate(mVideoPlayer, msg.arg1)
                MEDIA_SET_VIDEO_SIZE -> mOnVideoSizeChangedListener?.onVideoSizeChanged(mVideoPlayer, msg.arg1, msg.arg2)
                MEDIA_ERROR -> {
                    Log.e(TAG, "Error (${msg.arg1},${msg.arg2})")
                    val handled = mOnErrorListener?.onError(mVideoPlayer, msg.arg1, msg.arg2) ?: false
                    if (!handled) {
                        mOnCompletionListener?.onCompletion(mVideoPlayer)
                    }
                }
                MEDIA_INFO -> mOnInfoListener?.onInfo(mVideoPlayer, msg.arg1, msg.arg2)
                MEDIA_CURRENT -> mOnCurrentPositionListener?.onCurrentPosition(mVideoPlayer, msg.arg1.toLong(), msg.arg2.toLong())
                else -> Log.e(TAG, "Unknown message type ${msg.what}")
            }
        }
    }

    override fun setOnPreparedListener(listener: IMediaPlayer.OnPreparedListener?) { mOnPreparedListener = listener }
    override fun setOnCompletionListener(listener: IMediaPlayer.OnCompletionListener?) { mOnCompletionListener = listener }
    override fun setOnBufferingUpdateListener(listener: IMediaPlayer.OnBufferingUpdateListener?) { mOnBufferingUpdateListener = listener }
    override fun setOnSeekCompleteListener(listener: IMediaPlayer.OnSeekCompleteListener?) { mOnSeekCompleteListener = listener }
    override fun setOnVideoSizeChangedListener(listener: IMediaPlayer.OnVideoSizeChangedListener?) { mOnVideoSizeChangedListener = listener }
    override fun setOnErrorListener(listener: IMediaPlayer.OnErrorListener?) { mOnErrorListener = listener }
    override fun setOnInfoListener(listener: IMediaPlayer.OnInfoListener?) { mOnInfoListener = listener }
    override fun setOnCurrentPositionListener(listener: IMediaPlayer.OnCurrentPositionListener?) { mOnCurrentPositionListener = listener }
}
