package com.badlogic.gdx.math

import android.opengl.GLES30
import androidx.compose.runtime.Stable

@Stable
abstract class Viewport {
    var camera: Camera? = null
    var worldWidth: Float = 0f
    var worldHeight: Float = 0f
    var screenX = 0
    var screenY = 0
    var screenWidth = 0
    var screenHeight = 0
    private val tmp = Vector3()
    protected var gdxGraphicsWidth = 0
    protected var gdxGraphicsHeight = 0

    fun apply() { apply(false) }

    open fun apply(centerCamera: Boolean) {
        GLES30.glViewport(screenX, screenY, screenWidth, screenHeight)
        camera?.let {
            if (centerCamera) it.position.set(worldWidth / 2f, worldHeight / 2f, 0f)
            it.viewportWidth = worldWidth
            it.viewportHeight = worldHeight
            it.update()
        }
    }

    fun unproject(screenCoords: Vector2): Vector2 {
        tmp.set(screenCoords.x, screenCoords.y, 1f)
        camera?.unproject(tmp, screenX.toFloat(), screenY.toFloat(), screenWidth.toFloat(), screenHeight.toFloat())
        screenCoords.set(tmp.x, tmp.y)
        return screenCoords
    }

    fun project(worldCoords: Vector2): Vector2 {
        tmp.set(worldCoords.x, worldCoords.y, 1f)
        camera?.project(tmp, screenX.toFloat(), screenY.toFloat(), screenWidth.toFloat(), screenHeight.toFloat())
        worldCoords.set(tmp.x, tmp.y)
        return worldCoords
    }

    fun unproject(screenCoords: Vector3): Vector3 {
        camera?.unproject(screenCoords, screenX.toFloat(), screenY.toFloat(), screenWidth.toFloat(), screenHeight.toFloat())
        return screenCoords
    }

    fun project(worldCoords: Vector3): Vector3 {
        camera?.project(worldCoords, screenX.toFloat(), screenY.toFloat(), screenWidth.toFloat(), screenHeight.toFloat())
        return worldCoords
    }

    fun getPickRay(screenX: Float, screenY: Float): Ray {
        return camera!!.getPickRay(screenX, screenY, this.screenX.toFloat(), this.screenY.toFloat(), screenWidth.toFloat(), screenHeight.toFloat())
    }

    fun toScreenCoordinates(worldCoords: Vector2, transformMatrix: Matrix4): Vector2 {
        tmp.set(worldCoords.x, worldCoords.y, 0f)
        tmp.mul(transformMatrix)
        camera?.project(tmp)
        tmp.y = gdxGraphicsHeight - tmp.y
        worldCoords.x = tmp.x
        worldCoords.y = tmp.y
        return worldCoords
    }

    fun setGdxGraphicsSize(width: Int, height: Int) { gdxGraphicsWidth = width; gdxGraphicsHeight = height }
}
