package com.badlogic.gdx.math

import java.io.Serializable

/** Minimal Kotlin port of BoundingBox supporting basic operations used in this project. */
class BoundingBox() : Serializable {
    val min: Vector3 = Vector3()
    val max: Vector3 = Vector3()
    private val cnt: Vector3 = Vector3()
    private val dim: Vector3 = Vector3()

    constructor(bounds: BoundingBox) : this() {
        set(bounds)
    }

    constructor(minimum: Vector3, maximum: Vector3) : this() {
        set(minimum, maximum)
    }

    fun getCenter(out: Vector3): Vector3 = out.set(cnt)
    fun getDimensions(out: Vector3): Vector3 = out.set(dim)

    fun getCorner000(out: Vector3): Vector3 = out.set(min.x, min.y, min.z)
    fun getCorner001(out: Vector3): Vector3 = out.set(min.x, min.y, max.z)
    fun getCorner010(out: Vector3): Vector3 = out.set(min.x, max.y, min.z)
    fun getCorner011(out: Vector3): Vector3 = out.set(min.x, max.y, max.z)
    fun getCorner100(out: Vector3): Vector3 = out.set(max.x, min.y, min.z)
    fun getCorner101(out: Vector3): Vector3 = out.set(max.x, min.y, max.z)
    fun getCorner110(out: Vector3): Vector3 = out.set(max.x, max.y, min.z)
    fun getCorner111(out: Vector3): Vector3 = out.set(max.x, max.y, max.z)

    fun set(bounds: BoundingBox): BoundingBox = set(bounds.min, bounds.max)

    fun set(minimum: Vector3, maximum: Vector3): BoundingBox {
        min.set(minOf(minimum.x, maximum.x), minOf(minimum.y, maximum.y), minOf(minimum.z, maximum.z))
        max.set(maxOf(minimum.x, maximum.x), maxOf(minimum.y, maximum.y), maxOf(minimum.z, maximum.z))
        cnt.set(min).add(max).scl(0.5f)
        dim.set(max).sub(min)
        return this
    }

    fun inf(): BoundingBox {
        min.set(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
        max.set(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY)
        cnt.set(0f, 0f, 0f)
        dim.set(0f, 0f, 0f)
        return this
    }

    fun ext(point: Vector3): BoundingBox =
        set(min.set(minOf(min.x, point.x), minOf(min.y, point.y), minOf(min.z, point.z)),
            max.set(maxOf(max.x, point.x), maxOf(max.y, point.y), maxOf(max.z, point.z)))

    fun clr(): BoundingBox = set(min.set(0f, 0f, 0f), max.set(0f, 0f, 0f))

    fun isValid(): Boolean = min.x <= max.x && min.y <= max.y && min.z <= max.z

    fun contains(v: Vector3): Boolean =
        min.x <= v.x && max.x >= v.x && min.y <= v.y && max.y >= v.y && min.z <= v.z && max.z >= v.z

    override fun toString(): String = "[" + min + "|" + max + "]"
}
