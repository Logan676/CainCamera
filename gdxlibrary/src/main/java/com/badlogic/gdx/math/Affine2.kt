package com.badlogic.gdx.math

import java.io.Serializable

/** Simplified Kotlin version of Affine2 containing only the fields required by this project. */
class Affine2() : Serializable {
    var m00: Float = 1f
    var m01: Float = 0f
    var m02: Float = 0f
    var m10: Float = 0f
    var m11: Float = 1f
    var m12: Float = 0f

    constructor(other: Affine2) : this() {
        set(other)
    }

    fun idt(): Affine2 {
        m00 = 1f
        m01 = 0f
        m02 = 0f
        m10 = 0f
        m11 = 1f
        m12 = 0f
        return this
    }

    fun set(other: Affine2): Affine2 {
        m00 = other.m00
        m01 = other.m01
        m02 = other.m02
        m10 = other.m10
        m11 = other.m11
        m12 = other.m12
        return this
    }
}
