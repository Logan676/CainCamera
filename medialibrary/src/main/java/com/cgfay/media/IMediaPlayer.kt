package com.cgfay.media

import android.annotation.TargetApi
import android.content.Context
import android.graphics.SurfaceTexture
import android.media.AudioManager
import android.net.Uri
import android.view.Surface
import android.view.SurfaceHolder
import androidx.annotation.NonNull
import java.io.FileDescriptor
import java.io.IOException

interface IMediaPlayer {
    fun setDisplay(sh: SurfaceHolder?)
    fun setSurface(surface: Surface?)

    @Throws(IOException::class, IllegalArgumentException::class, SecurityException::class, IllegalStateException::class)
    fun setDataSource(context: Context, uri: Uri)

    @TargetApi(14)
    @Throws(IOException::class, IllegalArgumentException::class, SecurityException::class, IllegalStateException::class)
    fun setDataSource(context: Context, uri: Uri, headers: Map<String, String>?)

    @Throws(IOException::class, IllegalArgumentException::class, SecurityException::class, IllegalStateException::class)
    fun setDataSource(path: String)

    @Throws(IOException::class, IllegalArgumentException::class, SecurityException::class, IllegalStateException::class)
    fun setDataSource(path: String, headers: Map<String, String>?)

    @Throws(IOException::class, IllegalArgumentException::class, IllegalStateException::class)
    fun setDataSource(fd: FileDescriptor)

    @Throws(IOException::class, IllegalArgumentException::class, IllegalStateException::class)
    fun setDataSource(fd: FileDescriptor, offset: Long, length: Long)

    @Throws(IOException::class, IllegalStateException::class)
    fun prepare()

    @Throws(IllegalStateException::class)
    fun prepareAsync()

    @Throws(IllegalStateException::class)
    fun start()

    @Throws(IllegalStateException::class)
    fun stop()

    @Throws(IllegalStateException::class)
    fun pause()

    @Throws(IllegalStateException::class)
    fun resume()

    fun setWakeMode(context: Context, mode: Int)
    fun setScreenOnWhilePlaying(screenOn: Boolean)

    fun getRotate(): Int
    fun getVideoWidth(): Int
    fun getVideoHeight(): Int
    fun isPlaying(): Boolean
    fun seekTo(msec: Float)
    fun getCurrentPosition(): Long
    fun getDuration(): Long
    fun release()
    fun reset()
    fun setAudioStreamType(streamtype: Int)
    fun setLooping(looping: Boolean)
    fun isLooping(): Boolean
    fun setVolume(leftVolume: Float, rightVolume: Float)
    fun setAudioSessionId(sessionId: Int)
    fun getAudioSessionId(): Int
    fun setMute(mute: Boolean)

    interface OnPreparedListener { fun onPrepared(mp: IMediaPlayer) }
    fun setOnPreparedListener(listener: OnPreparedListener?)

    interface OnCompletionListener { fun onCompletion(mp: IMediaPlayer) }
    fun setOnCompletionListener(listener: OnCompletionListener?)

    interface OnBufferingUpdateListener { fun onBufferingUpdate(mp: IMediaPlayer, percent: Int) }
    fun setOnBufferingUpdateListener(listener: OnBufferingUpdateListener?)

    interface OnSeekCompleteListener { fun onSeekComplete(mp: IMediaPlayer) }
    fun setOnSeekCompleteListener(listener: OnSeekCompleteListener?)

    interface OnVideoSizeChangedListener { fun onVideoSizeChanged(mediaPlayer: IMediaPlayer, width: Int, height: Int) }
    fun setOnVideoSizeChangedListener(listener: OnVideoSizeChangedListener?)

    interface OnErrorListener { fun onError(mp: IMediaPlayer, what: Int, extra: Int): Boolean }
    fun setOnErrorListener(listener: OnErrorListener?)

    interface OnInfoListener { fun onInfo(mp: IMediaPlayer, what: Int, extra: Int): Boolean }
    fun setOnInfoListener(listener: OnInfoListener?)

    interface OnCurrentPositionListener { fun onCurrentPosition(mp: IMediaPlayer, current: Long, duration: Long) }
    fun setOnCurrentPositionListener(listener: OnCurrentPositionListener?)

    companion object {
        const val MEDIA_ERROR_UNKNOWN = 1
        const val MEDIA_ERROR_SERVER_DIED = 100
        const val MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK = 200

        const val MEDIA_INFO_UNKNOWN = 1
        const val MEDIA_INFO_VIDEO_TRACK_LAGGING = 700
        const val MEDIA_INFO_BUFFERING_START = 701
        const val MEDIA_INFO_BUFFERING_END = 702
        const val MEDIA_INFO_BAD_INTERLEAVING = 800
        const val MEDIA_INFO_NOT_SEEKABLE = 801
        const val MEDIA_INFO_METADATA_UPDATE = 802
    }
}
