package com.cgfay.media.recorder

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMuxer
import android.os.Build
import android.util.Log
import android.view.Surface

/**
 * Kotlin version of the video encoder.
 */
class VideoEncoder(
    private val videoParams: VideoParams,
    private val listener: OnEncodingListener?
) {

    private val inputSurface: Surface
    private val mediaMuxer: MediaMuxer
    private val mediaCodec: MediaCodec
    private val bufferInfo = MediaCodec.BufferInfo()
    private var trackIndex = -1
    private var muxerStarted = false

    private var startTimeStamp = 0L
    private var lastTimeStamp = 0L
    private var duration = 0L

    init {
        val videoWidth = if (videoParams.videoWidth % 2 == 0) videoParams.videoWidth else videoParams.videoWidth - 1
        val videoHeight = if (videoParams.videoHeight % 2 == 0) videoParams.videoHeight else videoParams.videoHeight - 1
        val format = MediaFormat.createVideoFormat(VideoParams.MIME_TYPE, videoWidth, videoHeight)
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
        format.setInteger(MediaFormat.KEY_BIT_RATE, videoParams.bitRate)
        format.setInteger(MediaFormat.KEY_FRAME_RATE, VideoParams.FRAME_RATE)
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, VideoParams.I_FRAME_INTERVAL)
        if (Build.VERSION.SDK_INT >= 21) {
            var profile = 0
            var level = 0
            when (VideoParams.MIME_TYPE) {
                "video/avc" -> {
                    profile = MediaCodecInfo.CodecProfileLevel.AVCProfileHigh
                    level = if (videoWidth * videoHeight >= 1920 * 1080) {
                        MediaCodecInfo.CodecProfileLevel.AVCLevel4
                    } else {
                        MediaCodecInfo.CodecProfileLevel.AVCLevel31
                    }
                }
                "video/hevc" -> {
                    profile = MediaCodecInfo.CodecProfileLevel.HEVCProfileMain
                    level = if (videoWidth * videoHeight >= 1920 * 1080) {
                        MediaCodecInfo.CodecProfileLevel.HEVCHighTierLevel4
                    } else {
                        MediaCodecInfo.CodecProfileLevel.HEVCHighTierLevel31
                    }
                }
            }
            format.setInteger(MediaFormat.KEY_PROFILE, profile)
            format.setInteger(MediaFormat.KEY_LEVEL, level)
        }
        if (VERBOSE) Log.d(TAG, "format: $format")

        mediaCodec = MediaCodec.createEncoderByType(VideoParams.MIME_TYPE)
        mediaCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        inputSurface = mediaCodec.createInputSurface()
        mediaCodec.start()

        mediaMuxer = MediaMuxer(videoParams.videoPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
    }

    fun getInputSurface(): Surface = inputSurface

    fun release() {
        if (VERBOSE) Log.d(TAG, "releasing encoder objects")
        mediaCodec.stop()
        mediaCodec.release()
        if (muxerStarted) {
            mediaMuxer.stop()
        }
        mediaMuxer.release()
    }

    fun drainEncoder(endOfStream: Boolean) {
        val TIMEOUT_USEC = 10000L
        if (VERBOSE) Log.d(TAG, "drainEncoder($endOfStream)")
        if (endOfStream) mediaCodec.signalEndOfInputStream()
        val encoderOutputBuffers = mediaCodec.outputBuffers
        while (true) {
            val encoderStatus = mediaCodec.dequeueOutputBuffer(bufferInfo, TIMEOUT_USEC)
            when {
                encoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER -> {
                    if (!endOfStream) break
                }
                encoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED -> {
                    // ignored for API < 21
                }
                encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                    if (muxerStarted) throw RuntimeException("format changed twice")
                    val newFormat = mediaCodec.outputFormat
                    if (VERBOSE) Log.d(TAG, "encoder output format changed: ${newFormat.getString(MediaFormat.KEY_MIME)}")
                    trackIndex = mediaMuxer.addTrack(newFormat)
                    mediaMuxer.start()
                    muxerStarted = true
                }
                encoderStatus < 0 -> {
                    Log.w(TAG, "unexpected result from encoder.dequeueOutputBuffer: $encoderStatus")
                }
                else -> {
                    val encodedData = encoderOutputBuffers[encoderStatus]
                        ?: throw RuntimeException("encoderOutputBuffer $encoderStatus was null")

                    if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG != 0) {
                        if (VERBOSE) Log.d(TAG, "ignoring BUFFER_FLAG_CODEC_CONFIG")
                        bufferInfo.size = 0
                    }

                    if (bufferInfo.size != 0) {
                        if (!muxerStarted) throw RuntimeException("muxer hasn't started")
                        if (lastTimeStamp > 0 && bufferInfo.presentationTimeUs < lastTimeStamp) {
                            bufferInfo.presentationTimeUs = lastTimeStamp + 10 * 1000
                        }
                        calculateTimeUs(bufferInfo)
                        encodedData.position(bufferInfo.offset)
                        encodedData.limit(bufferInfo.offset + bufferInfo.size)
                        mediaMuxer.writeSampleData(trackIndex, encodedData, bufferInfo)
                        if (VERBOSE) Log.d(TAG, "sent ${bufferInfo.size} bytes to muxer, ts=${bufferInfo.presentationTimeUs}")
                        listener?.onEncoding(duration)
                    }
                    mediaCodec.releaseOutputBuffer(encoderStatus, false)

                    if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                        if (!endOfStream) Log.w(TAG, "reached end of stream unexpectedly")
                        else if (VERBOSE) Log.d(TAG, "end of stream reached")
                        break
                    }
                }
            }
        }
    }

    private fun calculateTimeUs(info: MediaCodec.BufferInfo) {
        lastTimeStamp = info.presentationTimeUs
        if (startTimeStamp == 0L) {
            startTimeStamp = info.presentationTimeUs
        } else {
            duration = info.presentationTimeUs - startTimeStamp
        }
    }

    fun getDuration(): Long = duration
    fun getVideoParams(): VideoParams = videoParams

    interface OnEncodingListener {
        fun onEncoding(duration: Long)
    }

    companion object {
        private const val TAG = "VideoEncoder"
        private const val VERBOSE = true
    }
}
