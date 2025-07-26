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

class CainMediaPlayer : IMediaPlayer {

    companion object {
        const val TAG = "CainMediaPlayer"

        init {
            NativeLibraryLoader.loadLibraries(
                "ffmpeg",
                "soundtouch",
                "yuv",
                "media_player"
            )
            native_init()
        }

        @JvmStatic
        private external fun native_init()
        @JvmStatic
        private fun postEventFromNative(ref: Any, what: Int, arg1: Int, arg2: Int, obj: Any?) {
            val mp = (ref as WeakReference<*>).get() as? CainMediaPlayer ?: return
            mp.mEventHandler?.let {
                val m = it.obtainMessage(what, arg1, arg2, obj)
                it.sendMessage(m)
            }
        }

        fun create(context: Context, uri: Uri, holder: SurfaceHolder? = null): CainMediaPlayer? {
            return try {
                val mp = CainMediaPlayer()
                mp.setDataSource(context, uri)
                holder?.let { mp.setDisplay(it) }
                mp.prepare()
                mp
            } catch (ex: Exception) {
                Log.d(TAG, "create failed:", ex)
                null
            }
        }

        fun create(context: Context, resid: Int): CainMediaPlayer? {
            return try {
                val afd = context.resources.openRawResourceFd(resid) ?: return null
                val mp = CainMediaPlayer()
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
    private external fun _setRate(rate: Float)
    private external fun _setPitch(pitch: Float)
    private external fun _setLooping(looping: Boolean)
    private external fun _setRange(start: Float, end: Float)
    private external fun _setVolume(leftVolume: Float, rightVolume: Float)
    private external fun _setMute(mute: Boolean)
    private external fun _changeFilter(type: Int, name: String)
    private external fun _changeFilter(type: Int, id: Int)
    private external fun _setOption(category: Int, type: String, option: Long)
    private external fun _setOption(category: Int, type: String, option: String)

    private companion object {
        private const val NODE_FILTER = 4
        private const val NODE_EFFECT = 5
    }
    private external fun _prepare()
    private external fun _start()
    private external fun _pause()
    private external fun _resume()
    private external fun _stop()
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
    private var mEventHandler: CainEventHandler?
    private var mWakeLock: PowerManager.WakeLock? = null
    private var mScreenOnWhilePlaying = false
    private var mStayAwake = false
    private var mSessionId: Int = 0

    init {
        val looper = Looper.myLooper() ?: Looper.getMainLooper()
        mEventHandler = if (looper != null) CainEventHandler(this, looper) else null
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
    override fun setAudioSessionId(sessionId: Int) { mSessionId = sessionId }
    override fun getAudioSessionId(): Int = mSessionId
    override fun setMute(mute: Boolean) { _setMute(mute) }

    fun setRate(rate: Float) { _setRate(rate) }
    fun setPitch(pitch: Float) { _setPitch(pitch) }
    fun changeFilter(name: String) { _changeFilter(NODE_FILTER, name) }
    fun changeFilter(id: Int) { _changeFilter(NODE_FILTER, id) }
    fun changeEffect(name: String) { _changeFilter(NODE_EFFECT, name) }
    fun changeEffect(id: Int) { _changeFilter(NODE_EFFECT, id) }
    fun setOption(category: Int, type: String, option: String) { _setOption(category, type, option) }
    fun setOption(category: Int, type: String, option: Long) { _setOption(category, type, option) }

    override fun setWakeMode(context: Context, mode: Int) {
        var washeld = false
        mWakeLock?.let {
            if (it.isHeld) {
                washeld = true
                it.release()
            }
            mWakeLock = null
        }
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        mWakeLock = pm.newWakeLock(mode or PowerManager.ON_AFTER_RELEASE, CainMediaPlayer::class.java.name)
        mWakeLock?.setReferenceCounted(false)
        if (washeld) {
            mWakeLock?.acquire()
        }
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

    internal fun stayAwake(awake: Boolean) {
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

    private companion object {}
}
