package com.badlogic.gdx.math

import java.util.Random

/** Kotlin version of the RandomXS128 class implementing the xorshift128+ algorithm. */
class RandomXS128 : Random {
    companion object {
        private const val NORM_DOUBLE = 1.0 / (1L shl 53)
        private const val NORM_FLOAT = 1.0 / (1L shl 24)
        private fun murmurHash3(x0: Long): Long {
            var x = x0
            x = x xor (x ushr 33)
            x *= -0x44c88b7febf11ce5L
            x = x xor (x ushr 33)
            x *= -0x4498517a7b3558c5L
            x = x xor (x ushr 33)
            return x
        }
    }

    private var seed0: Long = 0
    private var seed1: Long = 0

    constructor() : super(murmurHash3(Random().nextLong())) {
        setSeed(Random().nextLong())
    }

    constructor(seed: Long) {
        setSeed(seed)
    }

    constructor(seed0: Long, seed1: Long) {
        setState(seed0, seed1)
    }

    override fun nextLong(): Long {
        var s1 = seed0
        val s0 = seed1
        seed0 = s0
        s1 = s1 xor (s1 shl 23)
        seed1 = s1 xor s0 xor (s1 ushr 17) xor (s0 ushr 26)
        return seed1 + s0
    }

    override fun next(bits: Int): Int {
        return (nextLong() and ((1L shl bits) - 1)).toInt()
    }

    override fun nextInt(): Int {
        return nextLong().toInt()
    }

    override fun nextInt(n: Int): Int {
        return nextLong(n.toLong()).toInt()
    }

    fun nextLong(n: Long): Long {
        require(n > 0) { "n must be positive" }
        while (true) {
            val bits = nextLong() ushr 1
            val value = bits % n
            if (bits - value + (n - 1) >= 0) return value
        }
    }

    override fun nextDouble(): Double {
        return (nextLong() ushr 11) * NORM_DOUBLE
    }

    override fun nextFloat(): Float {
        return ((nextLong() ushr 40) * NORM_FLOAT).toFloat()
    }

    override fun nextBoolean(): Boolean {
        return (nextLong() and 1L) != 0L
    }

    override fun nextBytes(bytes: ByteArray) {
        var n: Int
        var i = bytes.size
        while (i != 0) {
            n = if (i < 8) i else 8
            var bits = nextLong()
            while (n-- != 0) {
                bytes[--i] = bits.toByte()
                bits = bits shr 8
            }
        }
    }

    override fun setSeed(seed: Long) {
        var seed0 = murmurHash3(if (seed == 0L) Long.MIN_VALUE else seed)
        setState(seed0, murmurHash3(seed0))
    }

    fun setState(seed0: Long, seed1: Long) {
        this.seed0 = seed0
        this.seed1 = seed1
    }

    fun getState(which: Int): Long {
        return if (which == 0) seed0 else seed1
    }
}
