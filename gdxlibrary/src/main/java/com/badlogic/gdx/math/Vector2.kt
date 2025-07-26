package com.badlogic.gdx.math

import androidx.compose.runtime.Stable
import java.io.Serializable

@Stable
class Vector2(var x: Float = 0f, var y: Float = 0f) : Vector<Vector2>, Serializable {
    companion object {
        @JvmField val X = Vector2(1f, 0f)
        @JvmField val Y = Vector2(0f, 1f)
        @JvmField val Zero = Vector2(0f, 0f)
    }

    override fun cpy(): Vector2 = Vector2(x, y)

    override fun len(): Float = kotlin.math.sqrt(len2())

    override fun len2(): Float = x * x + y * y

    override fun limit(limit: Float): Vector2 {
        val l2 = len2()
        if (l2 > limit * limit) setLength(limit)
        return this
    }

    override fun limit2(limit2: Float): Vector2 {
        val l2 = len2()
        if (l2 > limit2) setLength2(limit2)
        return this
    }

    override fun setLength(len: Float): Vector2 {
        val oldLen = len()
        if (oldLen != 0f) {
            val scale = len / oldLen
            x *= scale
            y *= scale
        }
        return this
    }

    override fun setLength2(len2: Float): Vector2 {
        val oldLen2 = len2()
        if (oldLen2 != 0f) {
            val scale = kotlin.math.sqrt(len2 / oldLen2)
            x *= scale
            y *= scale
        }
        return this
    }

    override fun clamp(min: Float, max: Float): Vector2 {
        val l2 = len2()
        if (l2 == 0f) return this
        if (l2 > max * max) setLength(max)
        else if (l2 < min * min) setLength(min)
        return this
    }

    override fun set(v: Vector2): Vector2 { x = v.x; y = v.y; return this }
    fun set(x: Float, y: Float): Vector2 { this.x = x; this.y = y; return this }

    override fun sub(v: Vector2): Vector2 { x -= v.x; y -= v.y; return this }

    override fun nor(): Vector2 {
        val len = len()
        if (len != 0f) { x /= len; y /= len }
        return this
    }

    override fun add(v: Vector2): Vector2 { x += v.x; y += v.y; return this }
    fun add(x: Float, y: Float): Vector2 { this.x += x; this.y += y; return this }

    override fun dot(v: Vector2): Float = x * v.x + y * v.y

    override fun scl(scalar: Float): Vector2 { x *= scalar; y *= scalar; return this }
    override fun scl(v: Vector2): Vector2 { x *= v.x; y *= v.y; return this }

    override fun dst(v: Vector2): Float = kotlin.math.sqrt(dst2(v))
    override fun dst2(v: Vector2): Float { val dx = v.x - x; val dy = v.y - y; return dx * dx + dy * dy }

    override fun lerp(target: Vector2, alpha: Float): Vector2 {
        x += (target.x - x) * alpha
        y += (target.y - y) * alpha
        return this
    }

    override fun setToRandomDirection(): Vector2 {
        val angle = MathUtils.random(0f, MathUtils.PI2)
        x = kotlin.math.cos(angle)
        y = kotlin.math.sin(angle)
        return this
    }

    override fun isUnit(): Boolean = kotlin.math.abs(len2() - 1f) < MathUtils.FLOAT_ROUNDING_ERROR
    override fun isUnit(margin: Float): Boolean = kotlin.math.abs(len() - 1f) < margin
    override fun isZero(): Boolean = x == 0f && y == 0f
    override fun isZero(margin: Float): Boolean = len2() < margin * margin

    override fun isOnLine(other: Vector2, epsilon: Float): Boolean = kotlin.math.abs(x * other.y - y * other.x) <= epsilon
    override fun isOnLine(other: Vector2): Boolean = isOnLine(other, MathUtils.FLOAT_ROUNDING_ERROR)

    override fun isCollinear(other: Vector2, epsilon: Float): Boolean = isOnLine(other, epsilon) && dot(other) > 0
    override fun isCollinear(other: Vector2): Boolean = isCollinear(other, MathUtils.FLOAT_ROUNDING_ERROR)

    override fun isCollinearOpposite(other: Vector2, epsilon: Float): Boolean = isOnLine(other, epsilon) && dot(other) < 0
    override fun isCollinearOpposite(other: Vector2): Boolean = isCollinearOpposite(other, MathUtils.FLOAT_ROUNDING_ERROR)

    override fun isPerpendicular(other: Vector2): Boolean = kotlin.math.abs(dot(other)) <= MathUtils.FLOAT_ROUNDING_ERROR
    override fun isPerpendicular(other: Vector2, epsilon: Float): Boolean = kotlin.math.abs(dot(other)) <= epsilon

    override fun hasSameDirection(other: Vector2): Boolean = dot(other) > 0
    override fun hasOppositeDirection(other: Vector2): Boolean = dot(other) < 0

    override fun epsilonEquals(other: Vector2, epsilon: Float): Boolean = kotlin.math.abs(other.x - x) <= epsilon && kotlin.math.abs(other.y - y) <= epsilon

    override fun mulAdd(v: Vector2, scalar: Float): Vector2 { x += v.x * scalar; y += v.y * scalar; return this }
    override fun mulAdd(v: Vector2, mulVec: Vector2): Vector2 { x += v.x * mulVec.x; y += v.y * mulVec.y; return this }

    override fun setZero(): Vector2 { x = 0f; y = 0f; return this }

    fun mul(mat: Matrix3): Vector2 {
        val xx = x * mat.`val`[Matrix3.M00] + y * mat.`val`[Matrix3.M01] + mat.`val`[Matrix3.M02]
        val yy = x * mat.`val`[Matrix3.M10] + y * mat.`val`[Matrix3.M11] + mat.`val`[Matrix3.M12]
        x = xx; y = yy
        return this
    }

    override fun toString(): String = "($x,$y)"
}
