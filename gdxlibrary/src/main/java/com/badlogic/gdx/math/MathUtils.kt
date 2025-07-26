package com.badlogic.gdx.math

import java.util.Random

/** Kotlin version of MathUtils with commonly used utilities. */
object MathUtils {
    const val nanoToSec = 1 / 1000000000f
    const val FLOAT_ROUNDING_ERROR = 0.000001f
    const val PI = 3.1415927f
    const val PI2 = PI * 2
    const val E = 2.7182818f
    const val radiansToDegrees = 180f / PI
    const val radDeg = radiansToDegrees
    const val degreesToRadians = PI / 180
    const val degRad = degreesToRadians

    private const val SIN_BITS = 14
    private const val SIN_MASK = (-1 shl SIN_BITS).inv()
    private const val SIN_COUNT = SIN_MASK + 1
    private const val radFull = PI * 2
    private const val radToIndex = SIN_COUNT / radFull
    private val sinTable = FloatArray(SIN_COUNT) { i -> kotlin.math.sin((i + 0.5f) / SIN_COUNT * radFull) }

    init {
        for (i in 0 until 360 step 90) {
            sinTable[(i * radToIndex / PI).toInt() and SIN_MASK] = kotlin.math.sin(i * degreesToRadians)
        }
    }

    fun sin(radians: Float): Float = sinTable[(radians * radToIndex).toInt() and SIN_MASK]
    fun cos(radians: Float): Float = sinTable[((radians + PI / 2) * radToIndex).toInt() and SIN_MASK]
    fun sinDeg(degrees: Float): Float = sinTable[(degrees / 180f * PI * radToIndex).toInt() and SIN_MASK]
    fun cosDeg(degrees: Float): Float = sinTable[(((degrees + 90) / 180f * PI) * radToIndex).toInt() and SIN_MASK]

    fun atan2(y: Float, x: Float): Float = kotlin.math.atan2(y, x)

    val random: Random = Random()

    fun random(range: Int): Int = random.nextInt(range + 1)
    fun random(start: Int, end: Int): Int = start + random.nextInt(end - start + 1)
    fun random(): Float = random.nextFloat()
    fun random(range: Float): Float = random.nextFloat() * range
    fun random(start: Float, end: Float): Float = start + random.nextFloat() * (end - start)

    fun clamp(value: Int, min: Int, max: Int): Int = when {
        value < min -> min
        value > max -> max
        else -> value
    }
    fun clamp(value: Float, min: Float, max: Float): Float = when {
        value < min -> min
        value > max -> max
        else -> value
    }

    fun lerp(fromValue: Float, toValue: Float, progress: Float): Float = fromValue + (toValue - fromValue) * progress
    fun lerpAngle(fromRadians: Float, toRadians: Float, progress: Float): Float {
        val delta = ((toRadians - fromRadians + PI2 + PI) % PI2) - PI
        return (fromRadians + delta * progress + PI2) % PI2
    }
    fun lerpAngleDeg(fromDegrees: Float, toDegrees: Float, progress: Float): Float {
        val delta = ((toDegrees - fromDegrees + 360 + 180) % 360) - 180
        return (fromDegrees + delta * progress + 360) % 360
    }

    fun isZero(value: Float, tolerance: Float = FLOAT_ROUNDING_ERROR): Boolean = kotlin.math.abs(value) <= tolerance
    fun isEqual(a: Float, b: Float, tolerance: Float = FLOAT_ROUNDING_ERROR): Boolean = kotlin.math.abs(a - b) <= tolerance

    fun log(a: Float, value: Float): Float = (kotlin.math.ln(value.toDouble()) / kotlin.math.ln(a.toDouble())).toFloat()
    fun log2(value: Float): Float = log(2f, value)
}
