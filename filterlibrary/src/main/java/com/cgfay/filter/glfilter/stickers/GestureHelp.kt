package com.cgfay.filter.glfilter.stickers

import com.badlogic.gdx.math.Camera
import com.badlogic.gdx.math.Vector3
import com.cgfay.filter.glfilter.base.GLImageFilter

/**
 * 相机和屏幕坐标转换，用于触摸控制贴纸的旋转，平移，缩放操作
 */
object GestureHelp {
    fun screenToStageCoordinates(camera: Camera, screenCoords: Vector3): Vector3 {
        camera.unproject(screenCoords)
        return screenCoords
    }

    fun stageToScreenCoordinates(camera: Camera, stageCoords: Vector3): Vector3 {
        camera.project(stageCoords)
        stageCoords.y = camera.screenHeight - stageCoords.y
        return stageCoords
    }

    fun hit(target: Vector3, filters: List<GLImageFilter>): StaticStickerNormalFilter? {
        for (glImageFilter in filters) {
            if (glImageFilter is StaticStickerNormalFilter) {
                val staticFilter = glImageFilter
                screenToStageCoordinates(staticFilter.camera, target)
                return staticFilter.hit(target)
            }
        }
        return null
    }
}
