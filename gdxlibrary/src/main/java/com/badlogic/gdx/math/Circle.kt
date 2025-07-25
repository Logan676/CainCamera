package com.badlogic.gdx.math

import java.io.Serializable

/** Kotlin version of Circle with functionality required by the project. */
class Circle() : Shape2D, Serializable {
    var x: Float = 0f
    var y: Float = 0f
    var radius: Float = 0f

    constructor(x: Float, y: Float, radius: Float) : this() {
        this.x = x
        this.y = y
        this.radius = radius
    }

    constructor(position: Vector2, radius: Float) : this(position.x, position.y, radius)
    constructor(circle: Circle) : this(circle.x, circle.y, circle.radius)
    constructor(center: Vector2, edge: Vector2) : this(center.x, center.y, Vector2.len(center.x - edge.x, center.y - edge.y))

    fun set(x: Float, y: Float, radius: Float) {
        this.x = x
        this.y = y
        this.radius = radius
    }

    fun set(position: Vector2, radius: Float) = set(position.x, position.y, radius)
    fun set(circle: Circle) = set(circle.x, circle.y, circle.radius)
    fun set(center: Vector2, edge: Vector2) {
        x = center.x
        y = center.y
        radius = Vector2.len(center.x - edge.x, center.y - edge.y)
    }

    fun setPosition(position: Vector2) { x = position.x; y = position.y }
    fun setPosition(x: Float, y: Float) { this.x = x; this.y = y }
    fun setX(x: Float) { this.x = x }
    fun setY(y: Float) { this.y = y }
    fun setRadius(radius: Float) { this.radius = radius }

    fun contains(x: Float, y: Float): Boolean {
        val dx = this.x - x
        val dy = this.y - y
        return dx * dx + dy * dy <= radius * radius
    }

    fun contains(point: Vector2): Boolean = contains(point.x, point.y)

    fun contains(c: Circle): Boolean {
        val radiusDiff = radius - c.radius
        if (radiusDiff < 0f) return false
        val dx = x - c.x
        val dy = y - c.y
        val dst = dx * dx + dy * dy
        val radiusSum = radius + c.radius
        return !(radiusDiff * radiusDiff < dst) && dst < radiusSum * radiusSum
    }

    fun overlaps(c: Circle): Boolean {
        val dx = x - c.x
        val dy = y - c.y
        val distance = dx * dx + dy * dy
        val radiusSum = radius + c.radius
        return distance < radiusSum * radiusSum
    }

    override fun toString(): String = "$x,$y,$radius"

    fun circumference(): Float = radius * MathUtils.PI2
    fun area(): Float = radius * radius * MathUtils.PI

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Circle) return false
        return x == other.x && y == other.y && radius == other.radius
    }

    override fun hashCode(): Int {
        var result = NumberUtils.floatToRawIntBits(radius)
        result = 41 * result + NumberUtils.floatToRawIntBits(x)
        result = 41 * result + NumberUtils.floatToRawIntBits(y)
        return result
    }
}
