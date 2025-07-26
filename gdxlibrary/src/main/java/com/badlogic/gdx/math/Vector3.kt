package com.badlogic.gdx.math

import androidx.compose.runtime.Stable
import java.io.Serializable

@Stable
class Vector3(var x: Float = 0f, var y: Float = 0f, var z: Float = 0f) : Vector<Vector3>, Serializable {
    companion object {
        @JvmField val X = Vector3(1f, 0f, 0f)
        @JvmField val Y = Vector3(0f, 1f, 0f)
        @JvmField val Z = Vector3(0f, 0f, 1f)
        @JvmField val Zero = Vector3(0f, 0f, 0f)
    }

    override fun cpy(): Vector3 = Vector3(x, y, z)

    override fun len(): Float = kotlin.math.sqrt(len2())

    override fun len2(): Float = x * x + y * y + z * z

    override fun limit(limit: Float): Vector3 {
        val l2 = len2()
        if (l2 > limit * limit) setLength(limit)
        return this
    }

    override fun limit2(limit2: Float): Vector3 {
        val l2 = len2()
        if (l2 > limit2) setLength2(limit2)
        return this
    }

    override fun setLength(len: Float): Vector3 {
        val old = len()
        if (old != 0f) {
            val scale = len / old
            x *= scale; y *= scale; z *= scale
        }
        return this
    }

    override fun setLength2(len2: Float): Vector3 {
        val old = len2()
        if (old != 0f) {
            val scale = kotlin.math.sqrt(len2 / old)
            x *= scale; y *= scale; z *= scale
        }
        return this
    }

    override fun clamp(min: Float, max: Float): Vector3 {
        val l2 = len2()
        if (l2 == 0f) return this
        if (l2 > max * max) setLength(max)
        else if (l2 < min * min) setLength(min)
        return this
    }

    override fun set(v: Vector3): Vector3 { x = v.x; y = v.y; z = v.z; return this }
    fun set(x: Float, y: Float, z: Float): Vector3 { this.x = x; this.y = y; this.z = z; return this }

    override fun sub(v: Vector3): Vector3 { x -= v.x; y -= v.y; z -= v.z; return this }

    override fun nor(): Vector3 {
        val len = len()
        if (len != 0f) { x /= len; y /= len; z /= len }
        return this
    }

    override fun add(v: Vector3): Vector3 { x += v.x; y += v.y; z += v.z; return this }
    fun add(x: Float, y: Float, z: Float): Vector3 { this.x += x; this.y += y; this.z += z; return this }

    override fun dot(v: Vector3): Float = x * v.x + y * v.y + z * v.z

    override fun scl(scalar: Float): Vector3 { x *= scalar; y *= scalar; z *= scalar; return this }
    override fun scl(v: Vector3): Vector3 { x *= v.x; y *= v.y; z *= v.z; return this }

    override fun dst(v: Vector3): Float = kotlin.math.sqrt(dst2(v))
    override fun dst2(v: Vector3): Float { val a=v.x-x; val b=v.y-y; val c=v.z-z; return a*a+b*b+c*c }

    override fun lerp(target: Vector3, alpha: Float): Vector3 {
        x += (target.x - x) * alpha
        y += (target.y - y) * alpha
        z += (target.z - z) * alpha
        return this
    }

    override fun setToRandomDirection(): Vector3 {
        val theta = MathUtils.random(0f, MathUtils.PI2)
        val phi = MathUtils.random(0f, MathUtils.PI)
        x = kotlin.math.cos(theta) * kotlin.math.sin(phi)
        y = kotlin.math.sin(theta) * kotlin.math.sin(phi)
        z = kotlin.math.cos(phi)
        return this
    }

    override fun isUnit(): Boolean = kotlin.math.abs(len2() - 1f) < MathUtils.FLOAT_ROUNDING_ERROR
    override fun isUnit(margin: Float): Boolean = kotlin.math.abs(len() - 1f) < margin
    override fun isZero(): Boolean = x == 0f && y == 0f && z == 0f
    override fun isZero(margin: Float): Boolean = len2() < margin * margin

    override fun isOnLine(other: Vector3, epsilon: Float): Boolean = cpy().crs(other).len2() <= epsilon
    override fun isOnLine(other: Vector3): Boolean = isOnLine(other, MathUtils.FLOAT_ROUNDING_ERROR)

    private fun crs(other: Vector3): Vector3 {
        val cx = y * other.z - z * other.y
        val cy = z * other.x - x * other.z
        val cz = x * other.y - y * other.x
        x = cx; y = cy; z = cz
        return this
    }

    override fun isCollinear(other: Vector3, epsilon: Float): Boolean = isOnLine(other, epsilon) && dot(other) > 0
    override fun isCollinear(other: Vector3): Boolean = isCollinear(other, MathUtils.FLOAT_ROUNDING_ERROR)

    override fun isCollinearOpposite(other: Vector3, epsilon: Float): Boolean = isOnLine(other, epsilon) && dot(other) < 0
    override fun isCollinearOpposite(other: Vector3): Boolean = isCollinearOpposite(other, MathUtils.FLOAT_ROUNDING_ERROR)

    override fun isPerpendicular(other: Vector3): Boolean = kotlin.math.abs(dot(other)) <= MathUtils.FLOAT_ROUNDING_ERROR
    override fun isPerpendicular(other: Vector3, epsilon: Float): Boolean = kotlin.math.abs(dot(other)) <= epsilon

    override fun hasSameDirection(other: Vector3): Boolean = dot(other) > 0
    override fun hasOppositeDirection(other: Vector3): Boolean = dot(other) < 0

    override fun epsilonEquals(other: Vector3, epsilon: Float): Boolean =
        kotlin.math.abs(other.x - x) <= epsilon && kotlin.math.abs(other.y - y) <= epsilon && kotlin.math.abs(other.z - z) <= epsilon

    override fun mulAdd(v: Vector3, scalar: Float): Vector3 { x += v.x * scalar; y += v.y * scalar; z += v.z * scalar; return this }
    override fun mulAdd(v: Vector3, mulVec: Vector3): Vector3 { x += v.x * mulVec.x; y += v.y * mulVec.y; z += v.z * mulVec.z; return this }

    override fun setZero(): Vector3 { x = 0f; y = 0f; z = 0f; return this }

    fun prj(matrix: Matrix4): Vector3 {
        val m = matrix.`val`
        val w = x * m[Matrix4.M30] + y * m[Matrix4.M31] + z * m[Matrix4.M32] + m[Matrix4.M33]
        val nx = (x * m[Matrix4.M00] + y * m[Matrix4.M01] + z * m[Matrix4.M02] + m[Matrix4.M03]) / w
        val ny = (x * m[Matrix4.M10] + y * m[Matrix4.M11] + z * m[Matrix4.M12] + m[Matrix4.M13]) / w
        val nz = (x * m[Matrix4.M20] + y * m[Matrix4.M21] + z * m[Matrix4.M22] + m[Matrix4.M23]) / w
        x = nx; y = ny; z = nz
        return this
    }

    fun rot(matrix: Matrix4): Vector3 {
        val m = matrix.`val`
        val nx = x * m[Matrix4.M00] + y * m[Matrix4.M01] + z * m[Matrix4.M02]
        val ny = x * m[Matrix4.M10] + y * m[Matrix4.M11] + z * m[Matrix4.M12]
        val nz = x * m[Matrix4.M20] + y * m[Matrix4.M21] + z * m[Matrix4.M22]
        x = nx; y = ny; z = nz
        return this
    }

    fun mul(matrix: Matrix4): Vector3 {
        val m = matrix.`val`
        val nx = x * m[Matrix4.M00] + y * m[Matrix4.M01] + z * m[Matrix4.M02] + m[Matrix4.M03]
        val ny = x * m[Matrix4.M10] + y * m[Matrix4.M11] + z * m[Matrix4.M12] + m[Matrix4.M13]
        val nz = x * m[Matrix4.M20] + y * m[Matrix4.M21] + z * m[Matrix4.M22] + m[Matrix4.M23]
        x = nx; y = ny; z = nz
        return this
    }

    override fun toString(): String = "($x,$y,$z)"
}
