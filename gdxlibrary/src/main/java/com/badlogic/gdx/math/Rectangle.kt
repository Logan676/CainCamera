package com.badlogic.gdx.math

import java.io.Serializable

/** Simplified Kotlin version of the Rectangle class. */
class Rectangle() : Serializable, Shape2D {
    var x: Float = 0f
    var y: Float = 0f
    var width: Float = 0f
    var height: Float = 0f

    constructor(x: Float, y: Float, width: Float, height: Float) : this() {
        this.x = x
        this.y = y
        this.width = width
        this.height = height
    }

    constructor(rect: Rectangle) : this(rect.x, rect.y, rect.width, rect.height)

    fun set(x: Float, y: Float, width: Float, height: Float): Rectangle {
        this.x = x
        this.y = y
        this.width = width
        this.height = height
        return this
    }

    fun set(rect: Rectangle): Rectangle {
        return set(rect.x, rect.y, rect.width, rect.height)
    }

    fun setPosition(position: Vector2): Rectangle {
        x = position.x
        y = position.y
        return this
    }

    fun setPosition(x: Float, y: Float): Rectangle {
        this.x = x
        this.y = y
        return this
    }

    fun setSize(width: Float, height: Float): Rectangle {
        this.width = width
        this.height = height
        return this
    }

    fun setSize(size: Float): Rectangle {
        width = size
        height = size
        return this
    }

    fun contains(x: Float, y: Float): Boolean {
        return this.x <= x && this.x + this.width >= x && this.y <= y && this.y + this.height >= y
    }

    fun contains(point: Vector2): Boolean = contains(point.x, point.y)

    fun contains(circle: Circle): Boolean {
        return circle.x - circle.radius >= x && circle.x + circle.radius <= x + width &&
                circle.y - circle.radius >= y && circle.y + circle.radius <= y + height
    }

    fun contains(rectangle: Rectangle): Boolean {
        val xmin = rectangle.x
        val xmax = xmin + rectangle.width
        val ymin = rectangle.y
        val ymax = ymin + rectangle.height
        return xmin > x && xmin < x + width && xmax > x && xmax < x + width &&
                ymin > y && ymin < y + height && ymax > y && ymax < y + height
    }

    fun overlaps(r: Rectangle): Boolean {
        return x < r.x + r.width && x + width > r.x && y < r.y + r.height && y + height > r.y
    }

    fun merge(rect: Rectangle): Rectangle {
        val minX = kotlin.math.min(x, rect.x)
        val maxX = kotlin.math.max(x + width, rect.x + rect.width)
        val minY = kotlin.math.min(y, rect.y)
        val maxY = kotlin.math.max(y + height, rect.y + rect.height)
        x = minX
        width = maxX - minX
        y = minY
        height = maxY - minY
        return this
    }

    fun merge(x: Float, y: Float): Rectangle {
        val minX = kotlin.math.min(this.x, x)
        val maxX = kotlin.math.max(this.x + width, x)
        this.x = minX
        this.width = maxX - minX
        val minY = kotlin.math.min(this.y, y)
        val maxY = kotlin.math.max(this.y + height, y)
        this.y = minY
        this.height = maxY - minY
        return this
    }

    fun merge(vec: Vector2): Rectangle = merge(vec.x, vec.y)

    fun getAspectRatio(): Float = if (height == 0f) Float.NaN else width / height

    fun getCenter(vector: Vector2): Vector2 {
        vector.x = x + width / 2
        vector.y = y + height / 2
        return vector
    }

    fun setCenter(x: Float, y: Float): Rectangle {
        setPosition(x - width / 2, y - height / 2)
        return this
    }

    fun setCenter(position: Vector2): Rectangle {
        setPosition(position.x - width / 2, position.y - height / 2)
        return this
    }

    fun fitOutside(rect: Rectangle): Rectangle {
        val ratio = getAspectRatio()
        if (ratio > rect.getAspectRatio()) {
            setSize(rect.height * ratio, rect.height)
        } else {
            setSize(rect.width, rect.width / ratio)
        }
        setPosition(rect.x + rect.width / 2 - width / 2, rect.y + rect.height / 2 - height / 2)
        return this
    }

    fun fitInside(rect: Rectangle): Rectangle {
        val ratio = getAspectRatio()
        if (ratio < rect.getAspectRatio()) {
            setSize(rect.height * ratio, rect.height)
        } else {
            setSize(rect.width, rect.width / ratio)
        }
        setPosition(rect.x + rect.width / 2 - width / 2, rect.y + rect.height / 2 - height / 2)
        return this
    }

    fun fromString(v: String): Rectangle {
        val s0 = v.indexOf(',', 1)
        val s1 = v.indexOf(',', s0 + 1)
        val s2 = v.indexOf(',', s1 + 1)
        if (s0 != -1 && s1 != -1 && s2 != -1 && v.startsWith("[") && v.endsWith("]")) {
            val x = v.substring(1, s0).toFloat()
            val y = v.substring(s0 + 1, s1).toFloat()
            val width = v.substring(s1 + 1, s2).toFloat()
            val height = v.substring(s2 + 1, v.length - 1).toFloat()
            return set(x, y, width, height)
        }
        throw GdxRuntimeException("Malformed Rectangle: $v")
    }

    fun area(): Float = width * height
    fun perimeter(): Float = 2 * (width + height)

    override fun toString(): String = "[$x,$y,$width,$height]"

    override fun hashCode(): Int {
        var result = NumberUtils.floatToRawIntBits(height)
        result = 31 * result + NumberUtils.floatToRawIntBits(width)
        result = 31 * result + NumberUtils.floatToRawIntBits(x)
        result = 31 * result + NumberUtils.floatToRawIntBits(y)
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Rectangle) return false
        return NumberUtils.floatToRawIntBits(height) == NumberUtils.floatToRawIntBits(other.height) &&
                NumberUtils.floatToRawIntBits(width) == NumberUtils.floatToRawIntBits(other.width) &&
                NumberUtils.floatToRawIntBits(x) == NumberUtils.floatToRawIntBits(other.x) &&
                NumberUtils.floatToRawIntBits(y) == NumberUtils.floatToRawIntBits(other.y)
    }

    companion object {
        @JvmField val tmp = Rectangle()
        @JvmField val tmp2 = Rectangle()
    }
}
