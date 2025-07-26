package com.badlogic.gdx.math

object NumberUtils {
    fun floatToIntBits(value: Float): Int = java.lang.Float.floatToIntBits(value)
    fun floatToRawIntBits(value: Float): Int = java.lang.Float.floatToRawIntBits(value)
    fun floatToIntColor(value: Float): Int = java.lang.Float.floatToRawIntBits(value)
    /** Encodes the ABGR int color as a float. */
    fun intToFloatColor(value: Int): Float = java.lang.Float.intBitsToFloat(value and 0xfeffffff.toInt())
    fun intBitsToFloat(value: Int): Float = java.lang.Float.intBitsToFloat(value)
    fun doubleToLongBits(value: Double): Long = java.lang.Double.doubleToLongBits(value)
    fun longBitsToDouble(value: Long): Double = java.lang.Double.longBitsToDouble(value)
}
