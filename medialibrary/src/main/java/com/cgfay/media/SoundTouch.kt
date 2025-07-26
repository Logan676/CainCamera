package com.cgfay.media

import com.cgfay.uitls.utils.NativeLibraryLoader
import java.io.Closeable

/**
 * SoundTouch wrapper
 */
class SoundTouch : Closeable {

    private external fun nativeRelease(handle: Long)
    private external fun setRate(handle: Long, rate: Double)
    private external fun setTempo(handle: Long, tempo: Float)
    private external fun setRateChange(handle: Long, rate: Double)
    private external fun setTempoChange(handle: Long, tempo: Float)
    private external fun setPitch(handle: Long, pitch: Float)
    private external fun setPitchOctaves(handle: Long, pitch: Float)
    private external fun setPitchSemiTones(handle: Long, pitch: Float)
    private external fun setChannels(handle: Long, channels: Int)
    private external fun setSampleRate(handle: Long, sampleRate: Int)
    private external fun flush(handle: Long)
    private external fun putSamples(handle: Long, input: ByteArray, offset: Int, length: Int)
    private external fun receiveSamples(handle: Long, output: ByteArray, length: Int): Int

    private var handle: Long = nativeInit()

    init {
        // ensure native libs are loaded
    }

    fun setRate(rate: Double) = setRate(handle, rate)
    fun setTempo(tempo: Float) = setTempo(handle, tempo)
    fun setRateChange(rate: Double) = setRateChange(handle, rate)
    fun setTempoChange(tempo: Float) = setTempoChange(handle, tempo)
    fun setPitch(pitch: Float) = setPitch(handle, pitch)
    fun setPitchOctaves(pitch: Float) = setPitchOctaves(handle, pitch)
    fun setPitchSemiTones(pitch: Float) = setPitchSemiTones(handle, pitch)
    fun setChannels(channels: Int) = setChannels(handle, channels)
    fun setSampleRate(sampleRate: Int) = setSampleRate(handle, sampleRate)
    fun flush() = flush(handle)
    fun putSamples(input: ByteArray) = putSamples(handle, input, 0, input.size)
    fun receiveSamples(output: ByteArray): Int = receiveSamples(handle, output, output.size)

    fun release() {
        nativeRelease(handle)
        handle = 0
    }

    override fun close() = release()

    private companion object {
        init {
            NativeLibraryLoader.loadLibraries("soundtouch")
        }

        @JvmStatic
        private external fun nativeInit(): Long
    }
}
