package com.cgfay.media

import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import java.lang.ref.WeakReference

object CainMediaPlayerEvents {
    const val MEDIA_NOP = 0
    const val MEDIA_PREPARED = 1
    const val MEDIA_PLAYBACK_COMPLETE = 2
    const val MEDIA_BUFFERING_UPDATE = 3
    const val MEDIA_SEEK_COMPLETE = 4
    const val MEDIA_SET_VIDEO_SIZE = 5
    const val MEDIA_TIMED_TEXT = 99
    const val MEDIA_ERROR = 100
    const val MEDIA_INFO = 200
    const val MEDIA_CURRENT = 300
}

class CainEventHandler(private val mediaPlayer: CainMediaPlayer, looper: Looper) : Handler(looper) {
    override fun handleMessage(msg: Message) {
        if (mediaPlayer.mNativeContext == 0L) {
            Log.w(CainMediaPlayer.TAG, "mediaplayer went away with unhandled events")
            return
        }
        when (msg.what) {
            CainMediaPlayerEvents.MEDIA_PREPARED -> mediaPlayer.mOnPreparedListener?.onPrepared(mediaPlayer)
            CainMediaPlayerEvents.MEDIA_PLAYBACK_COMPLETE -> {
                mediaPlayer.mOnCompletionListener?.onCompletion(mediaPlayer)
                mediaPlayer.stayAwake(false)
            }
            CainMediaPlayerEvents.MEDIA_BUFFERING_UPDATE ->
                mediaPlayer.mOnBufferingUpdateListener?.onBufferingUpdate(mediaPlayer, msg.arg1)
            CainMediaPlayerEvents.MEDIA_SEEK_COMPLETE ->
                mediaPlayer.mOnSeekCompleteListener?.onSeekComplete(mediaPlayer)
            CainMediaPlayerEvents.MEDIA_SET_VIDEO_SIZE ->
                mediaPlayer.mOnVideoSizeChangedListener?.onVideoSizeChanged(mediaPlayer, msg.arg1, msg.arg2)
            CainMediaPlayerEvents.MEDIA_ERROR -> {
                Log.e(CainMediaPlayer.TAG, "Error (${msg.arg1},${msg.arg2})")
                var handled = false
                if (mediaPlayer.mOnErrorListener != null) {
                    handled = mediaPlayer.mOnErrorListener!!.onError(mediaPlayer, msg.arg1, msg.arg2)
                }
                if (mediaPlayer.mOnCompletionListener != null && !handled) {
                    mediaPlayer.mOnCompletionListener!!.onCompletion(mediaPlayer)
                }
                mediaPlayer.stayAwake(false)
            }
            CainMediaPlayerEvents.MEDIA_INFO -> {
                if (msg.arg1 != android.media.MediaPlayer.MEDIA_INFO_VIDEO_TRACK_LAGGING) {
                    Log.i(CainMediaPlayer.TAG, "Info (${msg.arg1},${msg.arg2})")
                }
                mediaPlayer.mOnInfoListener?.onInfo(mediaPlayer, msg.arg1, msg.arg2)
            }
            CainMediaPlayerEvents.MEDIA_CURRENT ->
                mediaPlayer.mOnCurrentPositionListener?.onCurrentPosition(mediaPlayer, msg.arg1, msg.arg2)
            CainMediaPlayerEvents.MEDIA_TIMED_TEXT, CainMediaPlayerEvents.MEDIA_NOP -> {}
            else -> Log.e(CainMediaPlayer.TAG, "Unknown message type ${msg.what}")
        }
    }
}

fun CainMediaPlayer.postEventFromNative(ref: Any, what: Int, arg1: Int, arg2: Int, obj: Any?) {
    val mp = (ref as WeakReference<*>).get() as? CainMediaPlayer ?: return
    mp.mEventHandler?.let {
        val m = it.obtainMessage(what, arg1, arg2, obj)
        it.sendMessage(m)
    }
}
