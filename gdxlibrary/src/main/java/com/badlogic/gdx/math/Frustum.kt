package com.badlogic.gdx.math

import com.badlogic.gdx.math.Plane.PlaneSide

/** Kotlin port of Frustum supporting the features required in this project. */
class Frustum {
    companion object {
        private val clipSpacePlanePoints = arrayOf(
            Vector3(-1f, -1f, -1f), Vector3(1f, -1f, -1f),
            Vector3(1f, 1f, -1f), Vector3(-1f, 1f, -1f),
            Vector3(-1f, -1f, 1f), Vector3(1f, -1f, 1f),
            Vector3(1f, 1f, 1f), Vector3(-1f, 1f, 1f)
        )
        private val clipSpacePlanePointsArray = FloatArray(8 * 3).apply {
            var j = 0
            for (v in clipSpacePlanePoints) {
                this[j++] = v.x
                this[j++] = v.y
                this[j++] = v.z
            }
        }
    }

    private val tmpV = Vector3()
    val planes = Array(6) { Plane(Vector3(), 0f) }
    val planePoints = Array(8) { Vector3() }
    private val planePointsArray = FloatArray(8 * 3)

    fun update(inverseProjectionView: Matrix4) {
        java.lang.System.arraycopy(clipSpacePlanePointsArray, 0, planePointsArray, 0, clipSpacePlanePointsArray.size)
        Matrix4.prj(inverseProjectionView.`val`, planePointsArray, 0, 8, 3)
        var j = 0
        for (i in 0 until 8) {
            val v = planePoints[i]
            v.x = planePointsArray[j++]
            v.y = planePointsArray[j++]
            v.z = planePointsArray[j++]
        }

        planes[0].set(planePoints[1], planePoints[0], planePoints[2])
        planes[1].set(planePoints[4], planePoints[5], planePoints[7])
        planes[2].set(planePoints[0], planePoints[4], planePoints[3])
        planes[3].set(planePoints[5], planePoints[1], planePoints[6])
        planes[4].set(planePoints[2], planePoints[3], planePoints[6])
        planes[5].set(planePoints[4], planePoints[0], planePoints[1])
    }

    fun pointInFrustum(point: Vector3): Boolean {
        for (i in planes.indices) {
            if (planes[i].testPoint(point) == PlaneSide.Back) return false
        }
        return true
    }

    fun pointInFrustum(x: Float, y: Float, z: Float): Boolean {
        for (i in planes.indices) {
            if (planes[i].testPoint(x, y, z) == PlaneSide.Back) return false
        }
        return true
    }

    fun sphereInFrustum(center: Vector3, radius: Float): Boolean {
        for (i in 0 until 6) if ((planes[i].normal.x * center.x + planes[i].normal.y * center.y + planes[i].normal.z * center.z) < (-radius - planes[i].d)) return false
        return true
    }

    fun sphereInFrustum(x: Float, y: Float, z: Float, radius: Float): Boolean {
        for (i in 0 until 6) if ((planes[i].normal.x * x + planes[i].normal.y * y + planes[i].normal.z * z) < (-radius - planes[i].d)) return false
        return true
    }

    fun sphereInFrustumWithoutNearFar(center: Vector3, radius: Float): Boolean {
        for (i in 2 until 6) if ((planes[i].normal.x * center.x + planes[i].normal.y * center.y + planes[i].normal.z * center.z) < (-radius - planes[i].d)) return false
        return true
    }

    fun sphereInFrustumWithoutNearFar(x: Float, y: Float, z: Float, radius: Float): Boolean {
        for (i in 2 until 6) if ((planes[i].normal.x * x + planes[i].normal.y * y + planes[i].normal.z * z) < (-radius - planes[i].d)) return false
        return true
    }

    fun boundsInFrustum(bounds: BoundingBox): Boolean {
        for (i in planes.indices) {
            if (planes[i].testPoint(bounds.getCorner000(tmpV)) != PlaneSide.Back) continue
            if (planes[i].testPoint(bounds.getCorner001(tmpV)) != PlaneSide.Back) continue
            if (planes[i].testPoint(bounds.getCorner010(tmpV)) != PlaneSide.Back) continue
            if (planes[i].testPoint(bounds.getCorner011(tmpV)) != PlaneSide.Back) continue
            if (planes[i].testPoint(bounds.getCorner100(tmpV)) != PlaneSide.Back) continue
            if (planes[i].testPoint(bounds.getCorner101(tmpV)) != PlaneSide.Back) continue
            if (planes[i].testPoint(bounds.getCorner110(tmpV)) != PlaneSide.Back) continue
            if (planes[i].testPoint(bounds.getCorner111(tmpV)) != PlaneSide.Back) continue
            return false
        }
        return true
    }

    fun boundsInFrustum(center: Vector3, dimensions: Vector3): Boolean {
        return boundsInFrustum(center.x, center.y, center.z, dimensions.x / 2, dimensions.y / 2, dimensions.z / 2)
    }

    fun boundsInFrustum(x: Float, y: Float, z: Float, halfWidth: Float, halfHeight: Float, halfDepth: Float): Boolean {
        for (i in planes.indices) {
            if (planes[i].testPoint(x + halfWidth, y + halfHeight, z + halfDepth) != PlaneSide.Back) continue
            if (planes[i].testPoint(x + halfWidth, y + halfHeight, z - halfDepth) != PlaneSide.Back) continue
            if (planes[i].testPoint(x + halfWidth, y - halfHeight, z + halfDepth) != PlaneSide.Back) continue
            if (planes[i].testPoint(x + halfWidth, y - halfHeight, z - halfDepth) != PlaneSide.Back) continue
            if (planes[i].testPoint(x - halfWidth, y + halfHeight, z + halfDepth) != PlaneSide.Back) continue
            if (planes[i].testPoint(x - halfWidth, y + halfHeight, z - halfDepth) != PlaneSide.Back) continue
            if (planes[i].testPoint(x - halfWidth, y - halfHeight, z + halfDepth) != PlaneSide.Back) continue
            if (planes[i].testPoint(x - halfWidth, y - halfHeight, z - halfDepth) != PlaneSide.Back) continue
            return false
        }
        return true
    }
}
