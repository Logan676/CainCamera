package com.cgfay.media.recorder

import java.nio.ShortBuffer
import java.util.Arrays
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * Sonic audio stream processor rewritten in Kotlin.
 * Based on https://github.com/waywardgeek/sonic.
 */
internal class Sonic(
    private val inputSampleRateHz: Int,
    private val numChannels: Int,
    private val speed: Float,
    private val pitch: Float,
    outputSampleRateHz: Int
) {
    private val rate: Float = inputSampleRateHz.toFloat() / outputSampleRateHz
    private val minPeriod: Int = inputSampleRateHz / MAXIMUM_PITCH
    private val maxPeriod: Int = inputSampleRateHz / MINIMUM_PITCH
    private val maxRequired: Int = 2 * maxPeriod
    private val downSampleBuffer = ShortArray(maxRequired)

    private var inputBufferSize = maxRequired
    private var inputBuffer = ShortArray(maxRequired * numChannels)
    private var outputBufferSize = maxRequired
    private var outputBuffer = ShortArray(maxRequired * numChannels)
    private var pitchBufferSize = maxRequired
    private var pitchBuffer = ShortArray(maxRequired * numChannels)
    private var oldRatePosition = 0
    private var newRatePosition = 0
    private var numInputSamples = 0
    private var numOutputSamples = 0
    private var numPitchSamples = 0
    private var remainingInputToCopy = 0
    private var prevPeriod = 0
    private var prevMinDiff = 0
    private var minDiff = 0
    private var maxDiff = 0

    fun queueInput(buffer: ShortBuffer) {
        val samplesToWrite = buffer.remaining() / numChannels
        val bytesToWrite = samplesToWrite * numChannels * 2
        enlargeInputBufferIfNeeded(samplesToWrite)
        buffer.get(inputBuffer, numInputSamples * numChannels, bytesToWrite / 2)
        numInputSamples += samplesToWrite
        processStreamInput()
    }

    fun getOutput(buffer: ShortBuffer) {
        val samplesToRead = min(buffer.remaining() / numChannels, numOutputSamples)
        buffer.put(outputBuffer, 0, samplesToRead * numChannels)
        numOutputSamples -= samplesToRead
        System.arraycopy(outputBuffer, samplesToRead * numChannels, outputBuffer, 0,
            numOutputSamples * numChannels)
    }

    fun queueEndOfStream() {
        val remainingSamples = numInputSamples
        val s = speed / pitch
        val r = rate * pitch
        val expectedOutputSamples = numOutputSamples + ((remainingSamples / s + numPitchSamples) / r + 0.5f).toInt()
        enlargeInputBufferIfNeeded(remainingSamples + 2 * maxRequired)
        for (x in 0 until 2 * maxRequired * numChannels) {
            inputBuffer[remainingSamples * numChannels + x] = 0
        }
        numInputSamples += 2 * maxRequired
        processStreamInput()
        if (numOutputSamples > expectedOutputSamples) {
            numOutputSamples = expectedOutputSamples
        }
        numInputSamples = 0
        remainingInputToCopy = 0
        numPitchSamples = 0
    }

    fun getSamplesAvailable(): Int = numOutputSamples

    private fun enlargeOutputBufferIfNeeded(numSamples: Int) {
        if (numOutputSamples + numSamples > outputBufferSize) {
            outputBufferSize += outputBufferSize / 2 + numSamples
            outputBuffer = Arrays.copyOf(outputBuffer, outputBufferSize * numChannels)
        }
    }

    private fun enlargeInputBufferIfNeeded(numSamples: Int) {
        if (numInputSamples + numSamples > inputBufferSize) {
            inputBufferSize += inputBufferSize / 2 + numSamples
            inputBuffer = Arrays.copyOf(inputBuffer, inputBufferSize * numChannels)
        }
    }

    private fun removeProcessedInputSamples(position: Int) {
        val remainingSamples = numInputSamples - position
        System.arraycopy(
            inputBuffer,
            position * numChannels,
            inputBuffer,
            0,
            remainingSamples * numChannels
        )
        numInputSamples = remainingSamples
    }

    private fun copyToOutput(samples: ShortArray, position: Int, numSamples: Int) {
        enlargeOutputBufferIfNeeded(numSamples)
        System.arraycopy(samples, position * numChannels, outputBuffer, numOutputSamples * numChannels,
            numSamples * numChannels)
        numOutputSamples += numSamples
    }

    private fun copyInputToOutput(position: Int): Int {
        val numSamples = min(maxRequired, remainingInputToCopy)
        copyToOutput(inputBuffer, position, numSamples)
        remainingInputToCopy -= numSamples
        return numSamples
    }

    private fun downSampleInput(samples: ShortArray, position: Int, skip: Int) {
        val numSamples = maxRequired / skip
        val samplesPerValue = numChannels * skip
        var pos = position * numChannels
        for (i in 0 until numSamples) {
            var value = 0
            for (j in 0 until samplesPerValue) {
                value += samples[pos + i * samplesPerValue + j].toInt()
            }
            value /= samplesPerValue
            downSampleBuffer[i] = value.toShort()
        }
    }

    private fun findPitchPeriodInRange(samples: ShortArray, position: Int, minPeriod: Int, maxPeriod: Int): Int {
        var bestPeriod = 0
        var worstPeriod = 255
        var localMinDiff = 1
        var localMaxDiff = 0
        var pos = position * numChannels
        for (period in minPeriod..maxPeriod) {
            var diff = 0
            for (i in 0 until period) {
                val sVal = samples[pos + i]
                val pVal = samples[pos + period + i]
                diff += abs(sVal.toInt() - pVal.toInt())
            }
            if (diff * bestPeriod < localMinDiff * period) {
                localMinDiff = diff
                bestPeriod = period
            }
            if (diff * worstPeriod > localMaxDiff * period) {
                localMaxDiff = diff
                worstPeriod = period
            }
        }
        this.minDiff = localMinDiff / bestPeriod
        this.maxDiff = localMaxDiff / worstPeriod
        return bestPeriod
    }

    private fun previousPeriodBetter(minDiff: Int, maxDiff: Int, preferNewPeriod: Boolean): Boolean {
        if (minDiff == 0 || prevPeriod == 0) {
            return false
        }
        return if (preferNewPeriod) {
            if (maxDiff > minDiff * 3) {
                false
            } else if (minDiff * 2 <= prevMinDiff * 3) {
                false
            } else {
                true
            }
        } else {
            if (minDiff <= prevMinDiff) {
                false
            } else {
                true
            }
        }
    }

    private fun findPitchPeriod(samples: ShortArray, position: Int, preferNewPeriod: Boolean): Int {
        val skip = if (inputSampleRateHz > AMDF_FREQUENCY) inputSampleRateHz / AMDF_FREQUENCY else 1
        val period: Int
        val retPeriod: Int
        if (numChannels == 1 && skip == 1) {
            period = findPitchPeriodInRange(samples, position, minPeriod, maxPeriod)
        } else {
            downSampleInput(samples, position, skip)
            period = findPitchPeriodInRange(downSampleBuffer, 0, minPeriod / skip, maxPeriod / skip)
            if (skip != 1) {
                var tempPeriod = period * skip
                var minP = tempPeriod - skip * 4
                var maxP = tempPeriod + skip * 4
                if (minP < minPeriod) {
                    minP = minPeriod
                }
                if (maxP > maxPeriod) {
                    maxP = maxPeriod
                }
                tempPeriod = if (numChannels == 1) {
                    findPitchPeriodInRange(samples, position, minP, maxP)
                } else {
                    downSampleInput(samples, position, 1)
                    findPitchPeriodInRange(downSampleBuffer, 0, minP, maxP)
                }
                return adjustPeriod(tempPeriod, preferNewPeriod)
            }
        }
        return adjustPeriod(period, preferNewPeriod)
    }

    private fun adjustPeriod(period: Int, preferNewPeriod: Boolean): Int {
        val retPeriod: Int = if (previousPeriodBetter(minDiff, maxDiff, preferNewPeriod)) {
            prevPeriod
        } else {
            period
        }
        prevMinDiff = minDiff
        prevPeriod = period
        return retPeriod
    }

    private fun moveNewSamplesToPitchBuffer(originalNumOutputSamples: Int) {
        val numSamples = numOutputSamples - originalNumOutputSamples
        if (numPitchSamples + numSamples > pitchBufferSize) {
            pitchBufferSize += pitchBufferSize / 2 + numSamples
            pitchBuffer = Arrays.copyOf(pitchBuffer, pitchBufferSize * numChannels)
        }
        System.arraycopy(outputBuffer, originalNumOutputSamples * numChannels, pitchBuffer,
            numPitchSamples * numChannels, numSamples * numChannels)
        numOutputSamples = originalNumOutputSamples
        numPitchSamples += numSamples
    }

    private fun removePitchSamples(numSamples: Int) {
        if (numSamples == 0) return
        System.arraycopy(
            pitchBuffer,
            numSamples * numChannels,
            pitchBuffer,
            0,
            (numPitchSamples - numSamples) * numChannels
        )
        numPitchSamples -= numSamples
    }

    private fun interpolate(`in`: ShortArray, inPos: Int, oldSampleRate: Int, newSampleRate: Int): Short {
        val left = `in`[inPos]
        val right = `in`[inPos + numChannels]
        val position = newRatePosition * oldSampleRate
        val leftPosition = oldRatePosition * newSampleRate
        val rightPosition = (oldRatePosition + 1) * newSampleRate
        val ratio = rightPosition - position
        val width = rightPosition - leftPosition
        return (((ratio * left + (width - ratio) * right) / width)).toShort()
    }

    private fun adjustRate(rate: Float, originalNumOutputSamples: Int) {
        if (numOutputSamples == originalNumOutputSamples) return
        var newSampleRate = (inputSampleRateHz / rate).toInt()
        var oldSampleRate = inputSampleRateHz
        while (newSampleRate > (1 shl 14) || oldSampleRate > (1 shl 14)) {
            newSampleRate /= 2
            oldSampleRate /= 2
        }
        moveNewSamplesToPitchBuffer(originalNumOutputSamples)
        for (position in 0 until numPitchSamples - 1) {
            while ((oldRatePosition + 1) * newSampleRate > newRatePosition * oldSampleRate) {
                enlargeOutputBufferIfNeeded(1)
                for (i in 0 until numChannels) {
                    outputBuffer[numOutputSamples * numChannels + i] =
                        interpolate(pitchBuffer, position * numChannels + i, oldSampleRate, newSampleRate)
                }
                newRatePosition++
                numOutputSamples++
            }
            oldRatePosition++
            if (oldRatePosition == oldSampleRate) {
                oldRatePosition = 0
                if (newRatePosition != newSampleRate) {
                    throw IllegalStateException()
                }
                newRatePosition = 0
            }
        }
        removePitchSamples(numPitchSamples - 1)
    }

    private fun skipPitchPeriod(samples: ShortArray, position: Int, speed: Float, period: Int): Int {
        val newSamples: Int = if (speed >= 2.0f) {
            (period / (speed - 1.0f)).toInt()
        } else {
            remainingInputToCopy = ((period * (2.0f - speed) / (speed - 1.0f))).toInt()
            period
        }
        enlargeOutputBufferIfNeeded(newSamples)
        overlapAdd(
            newSamples,
            numChannels,
            outputBuffer,
            numOutputSamples,
            samples,
            position,
            samples,
            position + period
        )
        numOutputSamples += newSamples
        return newSamples
    }

    private fun insertPitchPeriod(samples: ShortArray, position: Int, speed: Float, period: Int): Int {
        val newSamples: Int = if (speed < 0.5f) {
            (period * speed / (1.0f - speed)).toInt()
        } else {
            remainingInputToCopy = ((period * (2.0f * speed - 1.0f) / (1.0f - speed))).toInt()
            period
        }
        enlargeOutputBufferIfNeeded(period + newSamples)
        System.arraycopy(samples, position * numChannels, outputBuffer, numOutputSamples * numChannels,
            period * numChannels)
        overlapAdd(
            newSamples,
            numChannels,
            outputBuffer,
            numOutputSamples + period,
            samples,
            position + period,
            samples,
            position
        )
        numOutputSamples += period + newSamples
        return newSamples
    }

    private fun changeSpeed(speed: Float) {
        if (numInputSamples < maxRequired) return
        var numSamples = numInputSamples
        var position = 0
        while (position + maxRequired <= numSamples) {
            if (remainingInputToCopy > 0) {
                position += copyInputToOutput(position)
            } else {
                val period = findPitchPeriod(inputBuffer, position, true)
                position += if (speed > 1.0f) {
                    period + skipPitchPeriod(inputBuffer, position, speed, period)
                } else {
                    insertPitchPeriod(inputBuffer, position, speed, period)
                }
            }
        }
        removeProcessedInputSamples(position)
    }

    private fun processStreamInput() {
        val originalNumOutputSamples = numOutputSamples
        val s = speed / pitch
        val r = rate * pitch
        if (s > 1.00001f || s < 0.99999f) {
            changeSpeed(s)
        } else {
            copyToOutput(inputBuffer, 0, numInputSamples)
            numInputSamples = 0
        }
        if (r != 1.0f) {
            adjustRate(r, originalNumOutputSamples)
        }
    }

    companion object {
        private const val MINIMUM_PITCH = 65
        private const val MAXIMUM_PITCH = 400
        private const val AMDF_FREQUENCY = 4000

        private fun overlapAdd(
            numSamples: Int,
            numChannels: Int,
            out: ShortArray,
            outPos: Int,
            rampDown: ShortArray,
            rampDownPos: Int,
            rampUp: ShortArray,
            rampUpPos: Int
        ) {
            for (i in 0 until numChannels) {
                var o = outPos * numChannels + i
                var u = rampUpPos * numChannels + i
                var d = rampDownPos * numChannels + i
                for (t in 0 until numSamples) {
                    out[o] = (((rampDown[d] * (numSamples - t) + rampUp[u] * t) / numSamples)).toShort()
                    o += numChannels
                    d += numChannels
                    u += numChannels
                }
            }
        }
    }
}

