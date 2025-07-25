package com.badlogic.gdx.math

import java.io.Serializable
import kotlin.math.*

class Quaternion(
    var x: Float = 0f,
    var y: Float = 0f,
    var z: Float = 0f,
    var w: Float = 1f
) : Serializable {
    constructor(quaternion: Quaternion) : this(quaternion.x, quaternion.y, quaternion.z, quaternion.w)
    constructor(axis: Vector3, angle: Float) : this() { set(axis, angle) }

    fun set(x: Float, y: Float, z: Float, w: Float): Quaternion {
        this.x = x; this.y = y; this.z = z; this.w = w
        return this
    }

    fun set(quaternion: Quaternion): Quaternion = set(quaternion.x, quaternion.y, quaternion.z, quaternion.w)

    fun set(axis: Vector3, angle: Float): Quaternion = setFromAxis(axis.x, axis.y, axis.z, angle)

    fun setFromAxis(axis: Vector3, degrees: Float): Quaternion = setFromAxis(axis.x, axis.y, axis.z, degrees)

    fun setFromAxisRad(axis: Vector3, radians: Float): Quaternion = setFromAxisRad(axis.x, axis.y, axis.z, radians)

    fun setFromAxis(x: Float, y: Float, z: Float, degrees: Float): Quaternion {
        return setFromAxisRad(x, y, z, degrees * MathUtils.degreesToRadians)
    }

    fun setFromAxisRad(x: Float, y: Float, z: Float, radians: Float): Quaternion {
        val d = sqrt(x * x + y * y + z * z)
        if (d == 0f) return idt()
        val l = 1f / d
        val angle = radians * 0.5f
        val sin = sin(angle)
        this.w = cos(angle)
        this.x = x * l * sin
        this.y = y * l * sin
        this.z = z * l * sin
        return this
    }

    fun setFromCross(v1: Vector3, v2: Vector3): Quaternion = setFromCross(v1.x, v1.y, v1.z, v2.x, v2.y, v2.z)

    fun setFromCross(x1: Float, y1: Float, z1: Float, x2: Float, y2: Float, z2: Float): Quaternion {
        val tmp1 = Vector3(y1 * z2 - z1 * y2, z1 * x2 - x1 * z2, x1 * y2 - y1 * x2)
        val dot = x1 * x2 + y1 * y2 + z1 * z2
        val w = sqrt((x1 * x1 + y1 * y1 + z1 * z1) * (x2 * x2 + y2 * y2 + z2 * z2)) + dot
        this.x = tmp1.x
        this.y = tmp1.y
        this.z = tmp1.z
        this.w = w
        return nor()
    }

    fun setEulerAngles(yaw: Float, pitch: Float, roll: Float): Quaternion =
        setEulerAnglesRad(yaw * MathUtils.degreesToRadians, pitch * MathUtils.degreesToRadians, roll * MathUtils.degreesToRadians)

    fun setEulerAnglesRad(yaw: Float, pitch: Float, roll: Float): Quaternion {
        val hr = roll * 0.5f
        val shr = sin(hr)
        val chr = cos(hr)
        val hp = pitch * 0.5f
        val shp = sin(hp)
        val chp = cos(hp)
        val hy = yaw * 0.5f
        val shy = sin(hy)
        val chy = cos(hy)
        val chy_shp = chy * shp
        val shy_chp = shy * chp
        val chy_chp = chy * chp
        val shy_shp = shy * shp
        x = chy_shp * chr + shy_chp * shr
        y = shy_chp * chr - chy_shp * shr
        z = chy_chp * shr - shy_shp * chr
        w = chy_chp * chr + shy_shp * shr
        return this
    }

    fun idt(): Quaternion { x = 0f; y = 0f; z = 0f; w = 1f; return this }

    fun len(): Float = sqrt(x * x + y * y + z * z + w * w)
    fun len2(): Float = x * x + y * y + z * z + w * w

    fun nor(): Quaternion {
        val len = len()
        if (len != 0f) {
            val l = 1f / len
            x *= l; y *= l; z *= l; w *= l
        }
        return this
    }

    fun conjugate(): Quaternion { x = -x; y = -y; z = -z; return this }

    fun mul(other: Quaternion): Quaternion {
        val newX = w * other.x + x * other.w + y * other.z - z * other.y
        val newY = w * other.y + y * other.w + z * other.x - x * other.z
        val newZ = w * other.z + z * other.w + x * other.y - y * other.x
        val newW = w * other.w - x * other.x - y * other.y - z * other.z
        x = newX; y = newY; z = newZ; w = newW
        return this
    }

    fun mulLeft(other: Quaternion): Quaternion {
        val newX = other.w * x + other.x * w + other.y * z - other.z * y
        val newY = other.w * y + other.y * w + other.z * x - other.x * z
        val newZ = other.w * z + other.z * w + other.x * y - other.y * x
        val newW = other.w * w - other.x * x - other.y * y - other.z * z
        x = newX; y = newY; z = newZ; w = newW
        return this
    }

    fun add(quaternion: Quaternion): Quaternion {
        x += quaternion.x
        y += quaternion.y
        z += quaternion.z
        w += quaternion.w
        return this
    }

    fun transform(v: Vector3): Vector3 {
        val tmp1 = Quaternion(v.x, v.y, v.z, 0f)
        val tmp2 = Quaternion(this).conjugate().mulLeft(tmp1).mulLeft(this)
        v.x = tmp2.x
        v.y = tmp2.y
        v.z = tmp2.z
        return v
    }

    fun slerp(end: Quaternion, alpha: Float): Quaternion {
        var dot = x * end.x + y * end.y + z * end.z + w * end.w
        var endX = end.x
        var endY = end.y
        var endZ = end.z
        var endW = end.w
        if (dot < 0f) {
            dot = -dot
            endX = -endX
            endY = -endY
            endZ = -endZ
            endW = -endW
        }
        val scale0: Float
        val scale1: Float
        if (1f - dot > 0.1f) {
            val theta = acos(dot)
            val invSinTheta = 1f / sin(theta)
            scale0 = sin((1f - alpha) * theta) * invSinTheta
            scale1 = sin(alpha * theta) * invSinTheta
        } else {
            scale0 = 1f - alpha
            scale1 = alpha
        }
        x = scale0 * x + scale1 * endX
        y = scale0 * y + scale1 * endY
        z = scale0 * z + scale1 * endZ
        w = scale0 * w + scale1 * endW
        return this
    }

    fun exp(alpha: Float): Quaternion {
        val norm = len()
        val normExp = norm.pow(alpha)
        val theta = acos(w / norm)
        val coeff = if (abs(theta) < 0.001f) normExp * alpha / norm else (normExp * sin(alpha * theta) / (norm * sin(theta)))
        w = (normExp * cos(alpha * theta)).toFloat()
        x *= coeff; y *= coeff; z *= coeff
        return nor()
    }

    override fun toString(): String = "[$x|$y|$z|$w]"
}
