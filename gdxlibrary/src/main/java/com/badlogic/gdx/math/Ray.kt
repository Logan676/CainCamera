package com.badlogic.gdx.math

import java.io.Serializable

/** Kotlin port of the Ray class. */
class Ray() : Serializable {
    val origin: Vector3 = Vector3()
    val direction: Vector3 = Vector3()

    constructor(origin: Vector3, direction: Vector3) : this() {
        this.origin.set(origin)
        this.direction.set(direction).nor()
    }

    fun cpy(): Ray = Ray(origin, direction)

    fun getEndPoint(out: Vector3, distance: Float): Vector3 {
        return out.set(direction).scl(distance).add(origin)
    }

    fun mul(matrix: Matrix4): Ray {
        tmp.set(origin).add(direction)
        tmp.mul(matrix)
        origin.mul(matrix)
        direction.set(tmp.sub(origin))
        return this
    }

    fun set(origin: Vector3, direction: Vector3): Ray {
        this.origin.set(origin)
        this.direction.set(direction)
        return this
    }

    fun set(x: Float, y: Float, z: Float, dx: Float, dy: Float, dz: Float): Ray {
        origin.set(x, y, z)
        direction.set(dx, dy, dz)
        return this
    }

    fun set(ray: Ray): Ray {
        origin.set(ray.origin)
        direction.set(ray.direction)
        return this
    }

    override fun toString(): String = "ray [" + origin + ":" + direction + "]"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Ray) return false
        return direction == other.direction && origin == other.origin
    }

    override fun hashCode(): Int {
        var result = direction.hashCode()
        result = 73 * result + origin.hashCode()
        return result
    }

    companion object {
        private val tmp = Vector3()
    }
}
