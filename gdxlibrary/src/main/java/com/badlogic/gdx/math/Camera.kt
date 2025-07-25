package com.badlogic.gdx.math

/** Simplified Kotlin port of the Camera class providing functionality needed by the project. */
abstract class Camera {
    val position: Vector3 = Vector3()
    val direction: Vector3 = Vector3(0f, 0f, -1f)
    val up: Vector3 = Vector3(0f, 1f, 0f)

    val projection: Matrix4 = Matrix4()
    val view: Matrix4 = Matrix4()
    val combined: Matrix4 = Matrix4()
    val invProjectionView: Matrix4 = Matrix4()

    var near: Float = 1f
    var far: Float = 100f

    var viewportWidth: Float = 0f
    var viewportHeight: Float = 0f

    val frustum: Frustum = Frustum()

    private val tmpVec = Vector3()
    private val ray = Ray(Vector3(), Vector3())

    protected var gdxGraphicsWidth: Int = 0
    protected var gdxGraphicsHeight: Int = 0

    abstract fun update()
    abstract fun update(updateFrustum: Boolean)

    fun lookAt(x: Float, y: Float, z: Float) {
        tmpVec.set(x, y, z).sub(position).nor()
        if (!tmpVec.isZero) {
            val dot = tmpVec.dot(up)
            if (kotlin.math.abs(dot - 1f) < 0.000000001f) {
                up.set(direction).scl(-1f)
            } else if (kotlin.math.abs(dot + 1f) < 0.000000001f) {
                up.set(direction)
            }
            direction.set(tmpVec)
            normalizeUp()
        }
    }

    fun lookAt(target: Vector3) {
        lookAt(target.x, target.y, target.z)
    }

    fun normalizeUp() {
        tmpVec.set(direction).crs(up).nor()
        up.set(tmpVec).crs(direction).nor()
    }

    fun rotate(angle: Float, axisX: Float, axisY: Float, axisZ: Float) {
        direction.rotate(angle, axisX, axisY, axisZ)
        up.rotate(angle, axisX, axisY, axisZ)
    }

    fun rotate(axis: Vector3, angle: Float) {
        direction.rotate(axis, angle)
        up.rotate(axis, angle)
    }

    fun rotate(transform: Matrix4) {
        direction.rot(transform)
        up.rot(transform)
    }

    fun rotate(quat: Quaternion) {
        quat.transform(direction)
        quat.transform(up)
    }

    fun rotateAround(point: Vector3, axis: Vector3, angle: Float) {
        tmpVec.set(point)
        tmpVec.sub(position)
        translate(tmpVec)
        rotate(axis, angle)
        tmpVec.rotate(axis, angle)
        translate(-tmpVec.x, -tmpVec.y, -tmpVec.z)
    }

    fun transform(transform: Matrix4) {
        position.mul(transform)
        rotate(transform)
    }

    fun translate(x: Float, y: Float, z: Float) {
        position.add(x, y, z)
    }

    fun translate(vec: Vector3) {
        position.add(vec)
    }

    fun setGdxGraphicsSize(width: Int, height: Int) {
        gdxGraphicsWidth = width
        gdxGraphicsHeight = height
    }

    val screenWidth: Int
        get() = gdxGraphicsWidth
    val screenHeight: Int
        get() = gdxGraphicsHeight

    fun unproject(screenCoords: Vector3, viewportX: Float, viewportY: Float, viewportWidth: Float, viewportHeight: Float): Vector3 {
        var x = screenCoords.x
        var y = screenCoords.y
        x -= viewportX
        y = gdxGraphicsHeight - y - 1
        y -= viewportY
        screenCoords.x = (2 * x) / viewportWidth - 1
        screenCoords.y = (2 * y) / viewportHeight - 1
        screenCoords.z = 2 * screenCoords.z - 1
        screenCoords.prj(invProjectionView)
        return screenCoords
    }

    fun unproject(screenCoords: Vector3): Vector3 {
        unproject(screenCoords, 0f, 0f, gdxGraphicsWidth.toFloat(), gdxGraphicsHeight.toFloat())
        return screenCoords
    }

    fun project(worldCoords: Vector3): Vector3 {
        project(worldCoords, 0f, 0f, gdxGraphicsWidth.toFloat(), gdxGraphicsHeight.toFloat())
        return worldCoords
    }

    fun project(worldCoords: Vector3, viewportX: Float, viewportY: Float, viewportWidth: Float, viewportHeight: Float): Vector3 {
        worldCoords.prj(combined)
        worldCoords.x = viewportWidth * (worldCoords.x + 1) / 2 + viewportX
        worldCoords.y = viewportHeight * (worldCoords.y + 1) / 2 + viewportY
        worldCoords.z = (worldCoords.z + 1) / 2
        return worldCoords
    }

    fun getPickRay(screenX: Float, screenY: Float, viewportX: Float, viewportY: Float, viewportWidth: Float, viewportHeight: Float): Ray {
        unproject(ray.origin.set(screenX, screenY, 0f), viewportX, viewportY, viewportWidth, viewportHeight)
        unproject(ray.direction.set(screenX, screenY, 1f), viewportX, viewportY, viewportWidth, viewportHeight)
        ray.direction.sub(ray.origin).nor()
        return ray
    }

    fun getPickRay(screenX: Float, screenY: Float): Ray {
        return getPickRay(screenX, screenY, 0f, 0f, gdxGraphicsWidth.toFloat(), gdxGraphicsHeight.toFloat())
    }
}
