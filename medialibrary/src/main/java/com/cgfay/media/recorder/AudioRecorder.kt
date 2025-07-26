package com.cgfay.media.recorder

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.SystemClock
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Audio recorder implemented in Kotlin with a small Compose helper UI.
 */
class AudioRecorder : Runnable {
    private var bufferSize = AudioEncoder.BUFFER_SIZE
    private var audioRecord: AudioRecord? = null
    private var audioTranscoder: AudioTranscoder? = null
    private var audioEncoder: AudioEncoder? = null
    private var audioParams: AudioParams? = null
    @Volatile
    private var recording = false
    private var minBufferSize = 0
    private var recordListener: OnRecordListener? = null

    val mediaType: MediaType
        get() = MediaType.AUDIO

    fun setOnRecordListener(listener: OnRecordListener?) {
        recordListener = listener
    }

    fun startRecord() {
        recording = true
        Thread(this, "AudioRecorder").start()
    }

    fun stopRecord() {
        recording = false
    }

    @Throws(Exception::class)
    fun prepare(params: AudioParams) {
        audioParams = params
        if (audioRecord != null) {
            release()
        }
        audioEncoder?.release()

        val speed = params.speedMode.speed
        try {
            minBufferSize = (params.sampleRate * 4 * 0.02f).toInt()
            bufferSize = if (bufferSize < minBufferSize / speed * 2) {
                (minBufferSize / speed * 2).toInt()
            } else {
                AudioEncoder.BUFFER_SIZE
            }
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                params.sampleRate,
                params.channel,
                params.audioFormat,
                minBufferSize
            )
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }

        val channelCount = if (params.channel == AudioFormat.CHANNEL_IN_MONO) 1 else 2

        audioEncoder = AudioEncoder(params.bitRate, params.sampleRate, channelCount).apply {
            setBufferSize(bufferSize)
            setOutputPath(params.audioPath)
            prepare()
        }

        audioTranscoder = AudioTranscoder().apply {
            setSpeed(speed)
            configure(params.sampleRate, channelCount, params.audioFormat)
            outputSampleRateHz = params.sampleRate
            flush()
        }
    }

    @Synchronized
    fun release() {
        audioRecord?.let {
            try {
                it.release()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        audioRecord = null
        audioEncoder?.release()
        audioEncoder = null
    }

    override fun run() {
        var duration = 0L
        try {
            var needToStart = true
            while (recording && needToStart) {
                synchronized(this) {
                    if (audioRecord?.state == AudioRecord.STATE_INITIALIZED) {
                        audioRecord?.startRecording()
                        recordListener?.onRecordStart(MediaType.AUDIO)
                        needToStart = false
                    }
                }
                SystemClock.sleep(10)
            }

            val pcmData = ByteArray(minBufferSize)
            while (recording) {
                val size: Int
                synchronized(this) {
                    val record = audioRecord ?: break
                    size = record.read(pcmData, 0, pcmData.size)
                }
                if (size > 0) {
                    val inBuffer = ByteBuffer.wrap(pcmData, 0, size).order(ByteOrder.LITTLE_ENDIAN)
                    audioTranscoder?.queueInput(inBuffer)
                } else {
                    Thread.sleep(100)
                }

                val output = audioTranscoder?.output
                if (output != null && output.hasRemaining()) {
                    val outData = ByteArray(output.remaining())
                    output.get(outData)
                    synchronized(this) {
                        val encoder = audioEncoder ?: break
                        encoder.encodePCM(outData, outData.size)
                    }
                } else {
                    Thread.sleep(5)
                }
            }

            synchronized(this) {
                audioTranscoder?.let { transcoder ->
                    transcoder.endOfStream()
                    transcoder.output?.takeIf { it.hasRemaining() }?.let { buffer ->
                        val out = ByteArray(buffer.remaining())
                        buffer.get(out)
                        audioEncoder?.encodePCM(out, out.size)
                    }
                }
                audioEncoder?.encodePCM(null, -1)
            }
            duration = audioEncoder?.duration ?: 0L
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                release()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        recordListener?.onRecordFinish(
            RecordInfo(audioParams?.audioPath ?: "", duration, mediaType)
        )
    }
}

/**
 * Simple Compose button that controls audio recording.
 */

