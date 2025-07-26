package com.cgfay.media.recorder

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMuxer
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import java.nio.ByteBuffer

class AudioEncoder(
    private val bitrate: Int,
    private val sampleRate: Int,
    private val channelCount: Int
) {

    companion object {
        private const val TAG = "AudioEncoder"
        const val BUFFER_SIZE = 8192
        private const val AUDIO_MIME_TYPE = "audio/mp4a-latm"
        private const val ENCODE_TIMEOUT = -1L
    }

    private var mediaFormat: MediaFormat? = null
    private var mediaCodec: MediaCodec? = null
    private var mediaMuxer: MediaMuxer? = null
    private var inputBuffers: Array<ByteBuffer>? = null
    private var outputBuffers: Array<ByteBuffer>? = null
    private var bufferInfo: MediaCodec.BufferInfo? = null

    private var outputPath: String? = null
    private var audioTrackId: Int = 0
    private var totalBytesRead: Int = 0
    private var presentationTimeUs: Long = 0
    private var bufferSize: Int = BUFFER_SIZE

    fun setOutputPath(path: String) {
        outputPath = path
    }

    fun setBufferSize(size: Int) {
        bufferSize = size
    }

    @Throws(Exception::class)
    fun prepare() {
        val path = outputPath ?: throw IllegalStateException("No Output Path found.")
        mediaFormat = MediaFormat.createAudioFormat(AUDIO_MIME_TYPE, sampleRate, channelCount).apply {
            setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC)
            setInteger(MediaFormat.KEY_BIT_RATE, bitrate)
            setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, bufferSize)
        }
        mediaCodec = MediaCodec.createEncoderByType(AUDIO_MIME_TYPE).apply {
            configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            start()
        }
        inputBuffers = mediaCodec!!.inputBuffers
        outputBuffers = mediaCodec!!.outputBuffers
        bufferInfo = MediaCodec.BufferInfo()
        mediaMuxer = MediaMuxer(path, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
        totalBytesRead = 0
        presentationTimeUs = 0
    }

    fun release() {
        try {
            mediaCodec?.run {
                stop()
                release()
            }
            mediaCodec = null
            mediaMuxer?.run {
                stop()
                release()
            }
            mediaMuxer = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun encodePCM(data: ByteArray?, len: Int) {
        val codec = mediaCodec ?: return
        val inputIndex = codec.dequeueInputBuffer(ENCODE_TIMEOUT)
        if (inputIndex >= 0) {
            val buffer = inputBuffers!![inputIndex]
            buffer.clear()
            if (len < 0) {
                codec.queueInputBuffer(inputIndex, 0, 0, presentationTimeUs, 0)
            } else {
                totalBytesRead += len
                buffer.put(data, 0, len)
                codec.queueInputBuffer(inputIndex, 0, len, presentationTimeUs, 0)
                presentationTimeUs = 1_000_000L * (totalBytesRead / channelCount / 2) / sampleRate
                Log.d(TAG, "encodePCM: presentationUs:$presentationTimeUs, s:${presentationTimeUs / 1_000_000f}")
            }
        }

        var outputIndex = 0
        while (outputIndex != MediaCodec.INFO_TRY_AGAIN_LATER) {
            outputIndex = codec.dequeueOutputBuffer(bufferInfo!!, 0)
            when {
                outputIndex >= 0 -> {
                    val encodedData = outputBuffers!![outputIndex]
                    encodedData.position(bufferInfo!!.offset)
                    encodedData.limit(bufferInfo!!.offset + bufferInfo!!.size)
                    if ((bufferInfo!!.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0 && bufferInfo!!.size != 0) {
                        codec.releaseOutputBuffer(outputIndex, false)
                    } else {
                        mediaMuxer?.writeSampleData(audioTrackId, encodedData, bufferInfo!!)
                        codec.releaseOutputBuffer(outputIndex, false)
                    }
                }
                outputIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                    mediaFormat = codec.outputFormat
                    audioTrackId = mediaMuxer!!.addTrack(mediaFormat!!)
                    mediaMuxer!!.start()
                }
            }
        }
    }

    fun getDuration(): Long = presentationTimeUs
}

@Composable
fun rememberAudioEncoder(
    bitrate: Int,
    sampleRate: Int,
    channelCount: Int,
    outputPath: String
): AudioEncoder = remember(bitrate, sampleRate, channelCount, outputPath) {
    AudioEncoder(bitrate, sampleRate, channelCount).apply { setOutputPath(outputPath) }
}

