package com.badlogic.gdx.math

import java.io.Serializable
import kotlin.math.acos
import kotlin.math.sqrt

class Quaternion() : Serializable {
    var x: Float = 0f
    var y: Float = 0f
    var z: Float = 0f
    var w: Float = 1f

    constructor(x: Float, y: Float, z: Float, w: Float) : this() {
        set(x, y, z, w)
    }

    constructor(quaternion: Quaternion) : this(quaternion.x, quaternion.y, quaternion.z, quaternion.w)

    constructor(axis: Vector3, angle: Float) : this() {
        set(axis, angle)
    }

    fun set(x: Float, y: Float, z: Float, w: Float): Quaternion {
        this.x = x
        this.y = y
        this.z = z
        this.w = w
        return this
    }

    fun set(quaternion: Quaternion): Quaternion = set(quaternion.x, quaternion.y, quaternion.z, quaternion.w)

    fun set(axis: Vector3, angle: Float): Quaternion {
        return setFromAxis(axis.x, axis.y, axis.z, angle)
    }

    fun setFromAxis(x: Float, y: Float, z: Float, degrees: Float): Quaternion {
        val rad = degrees * MathUtils.degreesToRadians * 0.5f
        val sin = kotlin.math.sin(rad)
        val cos = kotlin.math.cos(rad)
        this.x = x * sin
        this.y = y * sin
        this.z = z * sin
        this.w = cos
        return this
    }

    fun idt(): Quaternion {
        return this.set(0f, 0f, 0f, 1f)
    }

    fun len(): Float = sqrt(len2())

    fun len2(): Float = x * x + y * y + z * z + w * w

    fun nor(): Quaternion {
        val len = len2()
        if (len != 0f && len != 1f) {
            val l = sqrt(len)
            x /= l
            y /= l
            z /= l
            w /= l
        }
        return this
    }

    fun conjugate(): Quaternion {
        x = -x
        y = -y
        z = -z
        return this
    }

    fun mul(q: Quaternion): Quaternion {
        set(
            w * q.x + x * q.w + y * q.z - z * q.y,
            w * q.y + y * q.w + z * q.x - x * q.z,
            w * q.z + z * q.w + x * q.y - y * q.x,
            w * q.w - x * q.x - y * q.y - z * q.z
        )
        return this
    }

    fun transform(vec: Vector3): Vector3 {
        val u = Vector3(x, y, z)
        val uv = Vector3(u).crs(vec)
        val uuv = Vector3(u).crs(uv)
        uv.scl(2f * w)
        uuv.scl(2f)
        return vec.add(uv).add(uuv)
    }

    fun dot(other: Quaternion): Float = x * other.x + y * other.y + z * other.z + w * other.w

    fun slerp(end: Quaternion, alpha: Float): Quaternion {
        var dot = dot(end)
        var endQ = end
        if (dot < 0f) {
            endQ = Quaternion(-end.x, -end.y, -end.z, -end.w)
            dot = -dot
        }
        if (dot > 0.9995f) {
            return set(
                x + alpha * (endQ.x - x),
                y + alpha * (endQ.y - y),
                z + alpha * (endQ.z - z),
                w + alpha * (endQ.w - w)
            ).nor()
        }
        val theta0 = acos(dot)
        val theta = theta0 * alpha
        val sinTheta = kotlin.math.sin(theta)
        val sinTheta0 = kotlin.math.sin(theta0)
        val s0 = kotlin.math.cos(theta) - dot * sinTheta / sinTheta0
        val s1 = sinTheta / sinTheta0
        return set(
            (s0 * x) + (s1 * endQ.x),
            (s0 * y) + (s1 * endQ.y),
            (s0 * z) + (s1 * endQ.z),
            (s0 * w) + (s1 * endQ.w)
        )
    }

    override fun toString(): String = "($x,$y,$z,$w)"
}
