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

class VideoPlayer : IMediaPlayer {
    @AccessedByNative
    private var mNativeContext: Long = 0
    private var mSurfaceHolder: SurfaceHolder? = null
    private var mEventHandler: EventHandler?
    private var mWakeLock: PowerManager.WakeLock? = null
    private var mScreenOnWhilePlaying = false
    private var mStayAwake = false
    private var mSessionId = 0

    init {
        NativeLibraryLoader.loadLibraries("ffmpeg", "yuv", "videoplayer")
        native_init()
        val looper = Looper.myLooper() ?: Looper.getMainLooper()
        mEventHandler = looper?.let { EventHandler(this, it) }
        native_setup(WeakReference(this))
    }

    external fun native_setup(mediaplayer_this: Any)
    external fun native_finalize()
    external fun _release()
    external fun _reset()
    @Throws(IOException::class, IllegalArgumentException::class, SecurityException::class, IllegalStateException::class)
    external fun _setDataSource(path: String)
    @Throws(IOException::class, IllegalArgumentException::class, SecurityException::class, IllegalStateException::class)
    external fun _setDataSource(path: String, keys: Array<String>?, values: Array<String>?)
    @Throws(IOException::class, IllegalArgumentException::class, IllegalStateException::class)
    external fun _setDataSource(fd: FileDescriptor, offset: Long, length: Long)
    external fun _setAudioDecoder(decoder: String)
    external fun _setVideoDecoder(decoder: String)
    external fun _setVideoSurface(surface: Surface?)
    external fun _setSpeed(speed: Float)
    external fun _setLooping(looping: Boolean)
    external fun _setRange(start: Float, end: Float)
    external fun _setVolume(left: Float, right: Float)
    external fun _setMute(mute: Boolean)
    external fun _prepare()
    external fun _start()
    external fun _pause()
    external fun _resume()
    external fun _stop()
    external fun _setDecodeOnPause(decodeOnPause: Boolean)
    external fun _seekTo(timeMs: Float)
    external fun _getCurrentPosition(): Long
    external fun _getDuration(): Long
    external fun _getRotate(): Int
    external fun _getVideoWidth(): Int
    external fun _getVideoHeight(): Int
    external fun _isLooping(): Boolean
    external fun _isPlaying(): Boolean

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
        private const val TAG = "VideoPlayer"
        @JvmStatic external fun native_init()

        @JvmStatic
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

        @JvmStatic
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

    override fun setDataSource(context: Context, uri: Uri) {
        setDataSource(context, uri, null)
    }

    override fun setDataSource(context: Context, uri: Uri, headers: Map<String, String>?) {
        val scheme = uri.scheme
        if (scheme == null || scheme == "file") {
            setDataSource(uri.path ?: "")
            return
        }
        var fd: AssetFileDescriptor? = null
        try {
            val resolver: ContentResolver = context.contentResolver
            fd = resolver.openAssetFileDescriptor(uri, "r")
            if (fd == null) {
                return
            }
            if (fd.declaredLength < 0) {
                setDataSource(fd.fileDescriptor)
            } else {
                setDataSource(fd.fileDescriptor, fd.startOffset, fd.declaredLength)
            }
            return
        } catch (_: Exception) {
        } finally {
            fd?.close()
        }
        Log.d(TAG, "Couldn't open file on client side, trying server side")
        setDataSource(uri.toString(), headers)
    }

    fun setDataSource(path: String) = _setDataSource(path)

    override fun setDataSource(path: String, headers: Map<String, String>?) {
        val keys = headers?.keys?.toTypedArray()
        val values = headers?.values?.toTypedArray()
        _setDataSource(path, keys, values)
    }

    override fun setDataSource(fd: FileDescriptor) {
        setDataSource(fd, 0, 0x7ffffffffffffffL)
    }

    fun setDataSource(fd: FileDescriptor, offset: Long, length: Long) = _setDataSource(fd, offset, length)

    override fun prepare() = _prepare()
    override fun prepareAsync() = _prepare()
    override fun start() { stayAwake(true); _start() }
    override fun stop() { stayAwake(false); _stop() }
    override fun pause() { stayAwake(false); _pause() }
    override fun resume() { stayAwake(true); _resume() }

    override fun setWakeMode(context: Context, mode: Int) {
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        mWakeLock?.release()
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
    override fun seekTo(msec: Float) = _seekTo(msec)
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
    override fun setAudioStreamType(streamtype: Int) {}
    override fun setLooping(looping: Boolean) = _setLooping(looping)
    override fun isLooping(): Boolean = _isLooping()
    override fun setVolume(leftVolume: Float, rightVolume: Float) = _setVolume(leftVolume, rightVolume)
    override fun setAudioSessionId(sessionId: Int) { mSessionId = sessionId }
    override fun getAudioSessionId(): Int = mSessionId
    override fun setMute(mute: Boolean) = _setMute(mute)

    fun setAudioDecoder(decoder: String) = _setAudioDecoder(decoder)
    fun setVideoDecoder(decoder: String) = _setVideoDecoder(decoder)
    fun setSpeed(speed: Float) = _setSpeed(speed)
    fun setRange(startMs: Float, endMs: Float) = _setRange(startMs, endMs)
    fun setDecodeOnPause(decodeOnPause: Boolean) = _setDecodeOnPause(decodeOnPause)

    protected fun finalize() { native_finalize() }

    private inner class EventHandler(private val mVideoPlayer: VideoPlayer, looper: Looper) : Handler(looper) {
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
                    Log.e(TAG, "Error(" + msg.arg1 + "," + msg.arg2 + ")")
                    val handled = mOnErrorListener?.onError(mVideoPlayer, msg.arg1, msg.arg2) ?: false
                    if (!handled) mOnCompletionListener?.onCompletion(mVideoPlayer)
                }
                MEDIA_INFO -> {
                    if (msg.arg1 != MEDIA_INFO_VIDEO_TRACK_LAGGING) {
                        Log.i(TAG, "Info(" + msg.arg1 + "," + msg.arg2 + ")")
                    }
                    mOnInfoListener?.onInfo(mVideoPlayer, msg.arg1, msg.arg2)
                }
                MEDIA_CURRENT -> mOnCurrentPositionListener?.onCurrentPosition(mVideoPlayer, msg.arg1.toLong(), msg.arg2.toLong())
                else -> Log.e(TAG, "Unknown message type " + msg.what)
            }
        }
    }

    private fun postEventFromNative(what: Int, arg1: Int, arg2: Int, obj: Any?) {
        mEventHandler?.let {
            val m = it.obtainMessage(what, arg1, arg2, obj)
            it.sendMessage(m)
        }
    }

    interface OnPreparedListener { fun onPrepared(mp: IMediaPlayer) }
    private var mOnPreparedListener: OnPreparedListener? = null
    override fun setOnPreparedListener(listener: OnPreparedListener?) { mOnPreparedListener = listener }

    interface OnCompletionListener { fun onCompletion(mp: IMediaPlayer) }
    private var mOnCompletionListener: OnCompletionListener? = null
    override fun setOnCompletionListener(listener: OnCompletionListener?) { mOnCompletionListener = listener }

    interface OnBufferingUpdateListener { fun onBufferingUpdate(mp: IMediaPlayer, percent: Int) }
    private var mOnBufferingUpdateListener: OnBufferingUpdateListener? = null
    override fun setOnBufferingUpdateListener(listener: OnBufferingUpdateListener?) { mOnBufferingUpdateListener = listener }

    interface OnSeekCompleteListener { fun onSeekComplete(mp: IMediaPlayer) }
    private var mOnSeekCompleteListener: OnSeekCompleteListener? = null
    override fun setOnSeekCompleteListener(listener: OnSeekCompleteListener?) { mOnSeekCompleteListener = listener }

    interface OnVideoSizeChangedListener { fun onVideoSizeChanged(mediaPlayer: IMediaPlayer, width: Int, height: Int) }
    private var mOnVideoSizeChangedListener: OnVideoSizeChangedListener? = null
    override fun setOnVideoSizeChangedListener(listener: OnVideoSizeChangedListener?) { mOnVideoSizeChangedListener = listener }

    interface OnErrorListener { fun onError(mp: IMediaPlayer, what: Int, extra: Int): Boolean }
    private var mOnErrorListener: OnErrorListener? = null
    override fun setOnErrorListener(listener: OnErrorListener?) { mOnErrorListener = listener }

    interface OnInfoListener { fun onInfo(mp: IMediaPlayer, what: Int, extra: Int): Boolean }
    private var mOnInfoListener: OnInfoListener? = null
    override fun setOnInfoListener(listener: OnInfoListener?) { mOnInfoListener = listener }

    interface OnCurrentPositionListener { fun onCurrentPosition(mp: IMediaPlayer, current: Long, duration: Long) }
    private var mOnCurrentPositionListener: OnCurrentPositionListener? = null
    override fun setOnCurrentPositionListener(listener: OnCurrentPositionListener?) { mOnCurrentPositionListener = listener }


    companion object {
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
}
