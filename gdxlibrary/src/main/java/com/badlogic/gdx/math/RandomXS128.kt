package com.badlogic.gdx.math

import java.util.Random

class RandomXS128 : Random {
    private var seed0: Long = 0
    private var seed1: Long = 0

    constructor() : super() {
        setSeed(Random().nextLong())
    }

    constructor(seed: Long) : super() {
        setSeed(seed)
    }

    constructor(seed0: Long, seed1: Long) : super() {
        setState(seed0, seed1)
    }

    override fun nextLong(): Long {
        var s1 = seed0
        val s0 = seed1
        seed0 = s0
        s1 = s1 xor (s1 shl 23)
        val result = s1 xor s0 xor (s1 ushr 17) xor (s0 ushr 26)
        seed1 = result
        return result + s0
    }

    override fun next(bits: Int): Int {
        return (nextLong() and ((1L shl bits) - 1)).toInt()
    }

    override fun nextInt(): Int = nextLong().toInt()

    override fun nextInt(n: Int): Int = nextLong(n.toLong()).toInt()

    fun nextLong(n: Long): Long {
        require(n > 0) { "n must be positive" }
        while (true) {
            val bits = nextLong() ushr 1
            val value = bits % n
            if (bits - value + (n - 1) >= 0) return value
        }
    }

    override fun nextDouble(): Double {
        return (nextLong() ushr 11) * (1.0 / (1L shl 53))
    }

    override fun nextFloat(): Float {
        return ((nextLong() ushr 40) * (1.0 / (1L shl 24))).toFloat()
    }

    override fun nextBoolean(): Boolean {
        return (nextLong() and 1L) != 0L
    }

    override fun nextBytes(bytes: ByteArray) {
        var i = bytes.size
        while (i != 0) {
            var n = if (i < 8) i else 8
            var bits = nextLong()
            while (n-- != 0) {
                bytes[--i] = bits.toByte()
                bits = bits shr 8
            }
        }
    }

    override fun setSeed(seed: Long) {
        var s0 = murmurHash3(if (seed == 0L) Long.MIN_VALUE else seed)
        setState(s0, murmurHash3(s0))
    }

    fun setState(seed0: Long, seed1: Long) {
        this.seed0 = seed0
        this.seed1 = seed1
    }

    fun getState(seed: Int): Long {
        return if (seed == 0) seed0 else seed1
    }

    companion object {
        private fun murmurHash3(xIn: Long): Long {
            var x = xIn
            x = x xor (x ushr 33)
            x *= -0x00850af3b2d3b4f1L
            x = x xor (x ushr 33)
            x *= -0x3d4d51cb7f6c6f7bL
            x = x xor (x ushr 33)
            return x
        }
    }
}
