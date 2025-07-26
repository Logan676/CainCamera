package com.cgfay.media.recorder

import android.media.AudioFormat
import androidx.compose.runtime.Stable
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.ShortBuffer

/**
 * Audio speed and pitch processor, based on ExoPlayer's SonicAudioProcessor.
 */
@Stable
class AudioTranscoder {

    companion object {
        /** Value indicating unknown or inapplicable fields. */
        const val NO_VALUE = -1

        /** Empty direct [ByteBuffer]. */
        val EMPTY_BUFFER: ByteBuffer = ByteBuffer.allocateDirect(0).order(ByteOrder.nativeOrder())

        /** Maximum and minimum allowed speed and pitch values. */
        const val MAXIMUM_SPEED = 8.0f
        const val MINIMUM_SPEED = 0.1f
        const val MAXIMUM_PITCH = 8.0f
        const val MINIMUM_PITCH = 0.1f

        /** Indicates that output sample rate should equal the input. */
        const val SAMPLE_RATE_NO_CHANGE = -1

        /** Threshold below which speed/pitch difference is negligible. */
        private const val CLOSE_THRESHOLD = 0.01f

        /** Minimum output bytes before speedup is calculated from byte counts. */
        private const val MIN_BYTES_FOR_SPEEDUP_CALCULATION = 1024

        private fun constrainValue(value: Float, min: Float, max: Float): Float =
            maxOf(min, minOf(value, max))

        private fun scaleLargeTimestamp(timestamp: Long, multiplier: Long, divisor: Long): Long {
            return when {
                divisor >= multiplier && divisor % multiplier == 0L -> {
                    val divisionFactor = divisor / multiplier
                    timestamp / divisionFactor
                }
                divisor < multiplier && multiplier % divisor == 0L -> {
                    val multiplicationFactor = multiplier / divisor
                    timestamp * multiplicationFactor
                }
                else -> {
                    val multiplicationFactor = multiplier.toDouble() / divisor.toDouble()
                    (timestamp * multiplicationFactor).toLong()
                }
            }
        }
    }

    private var pendingOutputSampleRateHz = SAMPLE_RATE_NO_CHANGE
    private var channelCount = NO_VALUE
    private var sampleRateHz = NO_VALUE

    private var sonic: Sonic? = null
    private var speed = 1f
    private var pitch = 1f
    private var outputSampleRateHz = NO_VALUE

    private var buffer: ByteBuffer = EMPTY_BUFFER
    private var shortBuffer: ShortBuffer = buffer.asShortBuffer()
    private var outputBuffer: ByteBuffer = EMPTY_BUFFER
    private var inputBytes: Long = 0
    private var outputBytes: Long = 0
    private var inputEnded = false

    /** Sets playback speed and returns the clamped value. */
    fun setSpeed(speed: Float): Float {
        this.speed = constrainValue(speed, MINIMUM_SPEED, MAXIMUM_SPEED)
        return this.speed
    }

    /** Sets playback pitch and returns the clamped value. */
    fun setPitch(pitch: Float): Float {
        this.pitch = constrainValue(pitch, MINIMUM_PITCH, MAXIMUM_PITCH)
        return this.pitch
    }

    /** Sets desired output sample rate. */
    fun setOutputSampleRateHz(sampleRateHz: Int) {
        pendingOutputSampleRateHz = sampleRateHz
    }

    /** Returns [duration] scaled by current speed. */
    fun scaleDurationForSpeedup(duration: Long): Long {
        return if (outputBytes >= MIN_BYTES_FOR_SPEEDUP_CALCULATION) {
            if (outputSampleRateHz == sampleRateHz) {
                scaleLargeTimestamp(duration, inputBytes, outputBytes)
            } else {
                scaleLargeTimestamp(duration, inputBytes * outputSampleRateHz, outputBytes * sampleRateHz)
            }
        } else {
            (speed.toDouble() * duration).toLong()
        }
    }

    /** Configures the processor to handle input audio. */
    @Throws(UnhandledFormatException::class)
    fun configure(sampleRateHz: Int, channelCount: Int, encoding: Int): Boolean {
        if (encoding != AudioFormat.ENCODING_PCM_16BIT) {
            throw UnhandledFormatException(sampleRateHz, channelCount, encoding)
        }
        val outputSampleRateHz = if (pendingOutputSampleRateHz == SAMPLE_RATE_NO_CHANGE) {
            sampleRateHz
        } else {
            pendingOutputSampleRateHz
        }
        return if (this.sampleRateHz == sampleRateHz &&
            this.channelCount == channelCount &&
            this.outputSampleRateHz == outputSampleRateHz
        ) {
            false
        } else {
            this.sampleRateHz = sampleRateHz
            this.channelCount = channelCount
            this.outputSampleRateHz = outputSampleRateHz
            true
        }
    }

    /** Returns whether the processor is active. */
    fun isActive(): Boolean {
        return kotlin.math.abs(speed - 1f) >= CLOSE_THRESHOLD ||
                kotlin.math.abs(pitch - 1f) >= CLOSE_THRESHOLD ||
                outputSampleRateHz != sampleRateHz
    }

    /** Number of audio channels in output data. */
    fun getOutputChannelCount(): Int = channelCount

    /** Encoding used for output data. */
    fun getOutputEncoding(): Int = AudioFormat.ENCODING_PCM_16BIT

    /** Sample rate of audio output in hertz. */
    fun getOutputSampleRateHz(): Int = outputSampleRateHz

    /** Queues audio data for processing. */
    fun queueInput(inputBuffer: ByteBuffer) {
        if (inputBuffer.hasRemaining()) {
            val shortBuffer = inputBuffer.asShortBuffer()
            val inputSize = inputBuffer.remaining()
            inputBytes += inputSize.toLong()
            sonic?.queueInput(shortBuffer)
            inputBuffer.position(inputBuffer.position() + inputSize)
        }
        val outputSize = (sonic?.getSamplesAvailable() ?: 0) * channelCount * 2
        if (outputSize > 0) {
            if (buffer.capacity() < outputSize) {
                buffer = ByteBuffer.allocateDirect(outputSize).order(ByteOrder.nativeOrder())
                shortBuffer = buffer.asShortBuffer()
            } else {
                buffer.clear()
                shortBuffer.clear()
            }
            sonic?.getOutput(shortBuffer)
            outputBytes += outputSize.toLong()
            buffer.limit(outputSize)
            outputBuffer = buffer
        }
    }

    /** Signals end of input stream. */
    fun endOfStream() {
        sonic?.queueEndOfStream()
        inputEnded = true
    }

    /** Returns processed output data. */
    fun getOutput(): ByteBuffer {
        val output = outputBuffer
        outputBuffer = EMPTY_BUFFER
        return output
    }

    /** Whether no more output will be produced until flushed. */
    fun isEnded(): Boolean {
        return inputEnded && (sonic == null || sonic?.getSamplesAvailable() == 0)
    }

    /** Clears state for new input stream. */
    fun flush() {
        sonic = Sonic(sampleRateHz, channelCount, speed, pitch, outputSampleRateHz)
        outputBuffer = EMPTY_BUFFER
        inputBytes = 0
        outputBytes = 0
        inputEnded = false
    }

    /** Resets processor to unconfigured state. */
    fun reset() {
        sonic = null
        buffer = EMPTY_BUFFER
        shortBuffer = buffer.asShortBuffer()
        outputBuffer = EMPTY_BUFFER
        channelCount = NO_VALUE
        sampleRateHz = NO_VALUE
        outputSampleRateHz = NO_VALUE
        inputBytes = 0
        outputBytes = 0
        inputEnded = false
        pendingOutputSampleRateHz = SAMPLE_RATE_NO_CHANGE
    }

    /** Exception thrown when an input audio format isn't supported. */
    class UnhandledFormatException(
        sampleRateHz: Int,
        channelCount: Int,
        encoding: Int
    ) : Exception("Unhandled format: $sampleRateHz Hz, $channelCount channels in encoding $encoding")
}
