package com.badlogic.gdx.math

class OrthographicCamera : Camera() {
    var zoom = 1f
    private val tmp = Vector3()

    constructor() : super() {
        near = 0f
    }

    constructor(viewportWidth: Float, viewportHeight: Float) : this() {
        this.viewportWidth = viewportWidth
        this.viewportHeight = viewportHeight
        update()
    }

    override fun update() {
        update(true)
    }

    override fun update(updateFrustum: Boolean) {
        projection.setToOrtho(
            zoom * -viewportWidth / 2f,
            zoom * (viewportWidth / 2f),
            zoom * -(viewportHeight / 2f),
            zoom * viewportHeight / 2f,
            near,
            far
        )
        view.setToLookAt(position, tmp.set(position).add(direction), up)
        combined.set(projection)
        Matrix4.mul(combined.`val`, view.`val`)
        if (updateFrustum) {
            invProjectionView.set(combined)
            Matrix4.inv(invProjectionView.`val`)
            frustum.update(invProjectionView)
        }
    }

    fun setToOrtho(yDown: Boolean) {
        setToOrtho(yDown, gdxGraphicsWidth.toFloat(), gdxGraphicsHeight.toFloat())
    }

    fun setToOrtho(yDown: Boolean, viewportWidth: Float, viewportHeight: Float) {
        if (yDown) {
            up.set(0f, -1f, 0f)
            direction.set(0f, 0f, 1f)
        } else {
            up.set(0f, 1f, 0f)
            direction.set(0f, 0f, -1f)
        }
        position.set(zoom * viewportWidth / 2f, zoom * viewportHeight / 2f, 0f)
        this.viewportWidth = viewportWidth
        this.viewportHeight = viewportHeight
        update()
    }

    fun rotate(angle: Float) {
        rotate(direction, angle)
    }

    fun translate(x: Float, y: Float) {
        translate(x, y, 0f)
    }

    fun translate(vec: Vector2) {
        translate(vec.x, vec.y, 0f)
    }
}
