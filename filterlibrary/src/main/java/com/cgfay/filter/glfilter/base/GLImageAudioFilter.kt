package com.cgfay.filter.glfilter.base

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import com.cgfay.filter.audioplayer.AutoFocusPlayer
import java.io.IOException
import java.util.HashSet
import java.util.Set

/**
 * Filter with music playback support.
 */
open class GLImageAudioFilter : GLImageFilter {
    private var mAudioUri: Uri? = null
    private var mLooping = false
    private var mPlayerInit = false
    private var mPlayerStatus = PlayerStatus.RELEASE
    private var mAudioPlayer: MediaPlayer? = null
    private val mPlayerSet: MutableSet<MediaPlayer> = HashSet()

    constructor(context: Context) : super(context)

    constructor(context: Context, vertexShader: String, fragmentShader: String) :
        super(context, vertexShader, fragmentShader)

    override fun release() {
        super.release()
        destroyPlayer()
    }

    fun setAudioPath(path: String) {
        setAudioPath(Uri.parse(path))
    }

    fun setAudioPath(uri: Uri) {
        mAudioUri = uri
    }

    fun setLooping(looping: Boolean) {
        mLooping = looping
    }

    fun isLooping(): Boolean = mLooping

    fun startPlayer() {
        if (mAudioUri == null) return
        when (mPlayerStatus) {
            PlayerStatus.RELEASE -> initPlayer()
            PlayerStatus.PREPARED -> {
                mAudioPlayer?.start()
                mAudioPlayer?.seekTo(0)
                mPlayerStatus = PlayerStatus.PLAYING
            }
            PlayerStatus.INIT -> mPlayerInit = true
            else -> {}
        }
    }

    fun stopPlayer() {
        if (mAudioPlayer != null && mPlayerStatus == PlayerStatus.PLAYING) {
            mAudioPlayer?.pause()
            mPlayerStatus = PlayerStatus.PREPARED
        }
        mPlayerInit = false
    }

    fun restartPlayer() {
        if (mAudioPlayer != null && mPlayerStatus == PlayerStatus.PLAYING) {
            mAudioPlayer?.seekTo(0)
        }
    }

    fun destroyPlayer() {
        stopPlayer()
        if (mAudioPlayer != null && mPlayerStatus == PlayerStatus.PREPARED) {
            mAudioPlayer?.stop()
            mAudioPlayer?.release()
            mPlayerSet.remove(mAudioPlayer)
        }
        mAudioPlayer = null
        mPlayerStatus = PlayerStatus.RELEASE
    }

    fun initPlayer() {
        mAudioPlayer = AudioPlayer(mContext, this)
        try {
            mAudioPlayer?.setDataSource(mContext, mAudioUri!!)
            mAudioPlayer?.setOnPreparedListener(mPreparedListener)
            mPlayerSet.add(mAudioPlayer!!)
            mAudioPlayer?.prepareAsync()
            mAudioPlayer?.isLooping = mLooping
            mPlayerStatus = PlayerStatus.INIT
            mPlayerInit = true
        } catch (e: IOException) {
            Log.e(TAG, "initPlayer: ", e)
        }
    }

    private val mPreparedListener = MediaPlayer.OnPreparedListener { player ->
        runOnDraw {
            if (mPlayerInit && mPlayerStatus == PlayerStatus.INIT && mAudioPlayer != null) {
                mAudioPlayer!!.start()
                mPlayerStatus = PlayerStatus.PLAYING
            } else if (mPlayerStatus == PlayerStatus.INIT) {
                mPlayerStatus = PlayerStatus.PREPARED
            }
            if (mAudioPlayer !== player && mPlayerSet.contains(player)) {
                player.stop()
                player.release()
            }
        }
    }

    private inner class AudioPlayer(context: Context, private val filter: GLImageAudioFilter) :
        AutoFocusPlayer(context) {
        override fun lossFocus() {
            super.lossFocus()
            if (isPlaying) {
                filter.stopPlayer()
            }
        }
    }

    private enum class PlayerStatus(val statusName: String, val index: Int) {
        RELEASE("release", 0),
        INIT("init", 1),
        PREPARED("prepared", 2),
        PLAYING("playing", 3)
    }
}
