package com.badlogic.gdx.math

import java.io.Serializable

class Rectangle() : Serializable, Shape2D {
    var x: Float = 0f
    var y: Float = 0f
    var width: Float = 0f
    var height: Float = 0f

    constructor(x: Float, y: Float, width: Float, height: Float) : this() {
        set(x, y, width, height)
    }

    constructor(rect: Rectangle) : this(rect.x, rect.y, rect.width, rect.height)

    fun set(x: Float, y: Float, width: Float, height: Float): Rectangle {
        this.x = x
        this.y = y
        this.width = width
        this.height = height
        return this
    }

    fun set(rect: Rectangle): Rectangle = set(rect.x, rect.y, rect.width, rect.height)

    fun setPosition(x: Float, y: Float): Rectangle {
        this.x = x
        this.y = y
        return this
    }

    fun setPosition(position: Vector2): Rectangle = setPosition(position.x, position.y)

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

    fun getPosition(out: Vector2): Vector2 = out.set(x, y)
    fun getSize(out: Vector2): Vector2 = out.set(width, height)

    override fun contains(x: Float, y: Float): Boolean {
        return this.x <= x && this.x + width >= x && this.y <= y && this.y + height >= y
    }

    override fun contains(point: Vector2): Boolean = contains(point.x, point.y)

    fun contains(circle: Circle): Boolean {
        return circle.x - circle.radius >= x &&
            circle.x + circle.radius <= x + width &&
            circle.y - circle.radius >= y &&
            circle.y + circle.radius <= y + height
    }

    fun contains(rectangle: Rectangle): Boolean {
        val xmin = rectangle.x
        val xmax = xmin + rectangle.width
        val ymin = rectangle.y
        val ymax = ymin + rectangle.height
        return (xmin > x && xmax < x + width && ymin > y && ymax < y + height)
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
        val minY = kotlin.math.min(this.y, y)
        val maxY = kotlin.math.max(this.y + height, y)
        this.x = minX
        this.width = maxX - minX
        this.y = minY
        this.height = maxY - minY
        return this
    }

    fun merge(vec: Vector2): Rectangle = merge(vec.x, vec.y)

    fun getAspectRatio(): Float = if (height == 0f) Float.NaN else width / height

    fun getCenter(out: Vector2): Vector2 {
        out.x = x + width / 2f
        out.y = y + height / 2f
        return out
    }

    fun setCenter(x: Float, y: Float): Rectangle {
        setPosition(x - width / 2f, y - height / 2f)
        return this
    }

    fun setCenter(position: Vector2): Rectangle = setCenter(position.x, position.y)

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

    override fun toString(): String = "[$x,$y,$width,$height]"

    fun fromString(v: String): Rectangle {
        val s0 = v.indexOf(',', 1)
        val s1 = v.indexOf(',', s0 + 1)
        val s2 = v.indexOf(',', s1 + 1)
        if (s0 != -1 && s1 != -1 && s2 != -1 && v[0] == '[' && v.last() == ']') {
            try {
                val x = v.substring(1, s0).toFloat()
                val y = v.substring(s0 + 1, s1).toFloat()
                val width = v.substring(s1 + 1, s2).toFloat()
                val height = v.substring(s2 + 1, v.length - 1).toFloat()
                return set(x, y, width, height)
            } catch (ex: NumberFormatException) {
                // ignore
            }
        }
        throw GdxRuntimeException("Malformed Rectangle: $v")
    }

    fun area(): Float = width * height
    fun perimeter(): Float = 2 * (width + height)

    override fun hashCode(): Int {
        var result = height.toBits()
        result = 31 * result + width.toBits()
        result = 31 * result + x.toBits()
        result = 31 * result + y.toBits()
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Rectangle) return false
        return height.toBits() == other.height.toBits() &&
            width.toBits() == other.width.toBits() &&
            x.toBits() == other.x.toBits() &&
            y.toBits() == other.y.toBits()
    }
}
