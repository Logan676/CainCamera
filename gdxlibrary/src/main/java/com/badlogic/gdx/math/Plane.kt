package com.badlogic.gdx.math

import java.io.Serializable

class Plane() : Serializable {
    enum class PlaneSide { OnPlane, Back, Front }

    val normal = Vector3()
    var d: Float = 0f

    constructor(normal: Vector3, d: Float) : this() {
        this.normal.set(normal).nor()
        this.d = d
    }

    constructor(normal: Vector3, point: Vector3) : this() {
        this.normal.set(normal).nor()
        this.d = -this.normal.dot(point)
    }

    constructor(point1: Vector3, point2: Vector3, point3: Vector3) : this() {
        set(point1, point2, point3)
    }

    fun set(point1: Vector3, point2: Vector3, point3: Vector3) {
        normal.set(point1).sub(point2)
            .crs(point2.x - point3.x, point2.y - point3.y, point2.z - point3.z)
            .nor()
        d = -point1.dot(normal)
    }

    fun set(nx: Float, ny: Float, nz: Float, d: Float) {
        normal.set(nx, ny, nz)
        this.d = d
    }

    fun distance(point: Vector3): Float = normal.dot(point) + d

    fun testPoint(point: Vector3): PlaneSide {
        val dist = normal.dot(point) + d
        return when {
            dist == 0f -> PlaneSide.OnPlane
            dist < 0f -> PlaneSide.Back
            else -> PlaneSide.Front
        }
    }

    fun testPoint(x: Float, y: Float, z: Float): PlaneSide {
        val dist = normal.dot(x, y, z) + d
        return when {
            dist == 0f -> PlaneSide.OnPlane
            dist < 0f -> PlaneSide.Back
            else -> PlaneSide.Front
        }
    }

    fun isFrontFacing(direction: Vector3): Boolean = normal.dot(direction) <= 0f

    fun set(point: Vector3, normal: Vector3) {
        this.normal.set(normal)
        d = -point.dot(normal)
    }

    fun set(pointX: Float, pointY: Float, pointZ: Float, norX: Float, norY: Float, norZ: Float) {
        this.normal.set(norX, norY, norZ)
        d = -(pointX * norX + pointY * norY + pointZ * norZ)
    }

    fun set(plane: Plane) {
        normal.set(plane.normal)
        d = plane.d
    }

    override fun toString(): String = "${normal}, $d"
}
