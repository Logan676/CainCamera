package com.cgfay.landmark

import android.graphics.PointF
import kotlin.math.hypot

/**
 * Utility class for calculating distances and centers between points.
 */
object FacePointsUtils {

    fun getDistance(p1: PointF, p2: PointF): Double =
        hypot((p1.x - p2.x).toDouble(), (p1.y - p2.y).toDouble())

    fun getDistance(point1: FloatArray, point2: FloatArray): Double =
        hypot((point1[0] - point2[0]).toDouble(), (point1[1] - point2[1]).toDouble())

    fun getDistance(x1: Float, y1: Float, x2: Float, y2: Float): Double =
        hypot((x1 - x2).toDouble(), (y1 - y2).toDouble())

    fun getCenter(p1: PointF, p2: PointF): PointF =
        PointF((p1.x + p2.x) / 2f, (p1.y + p2.y) / 2f)

    fun getCenter(point1: FloatArray, point2: FloatArray): FloatArray =
        floatArrayOf((point1[0] + point2[0]) / 2f, (point1[1] + point2[1]) / 2f)

    fun getCenter(x1: Float, y1: Float, x2: Float, y2: Float): FloatArray =
        floatArrayOf((x1 + x2) / 2f, (y1 + y2) / 2f)

    fun getCenter(result: FloatArray, x1: Float, y1: Float, x2: Float, y2: Float) {
        result[0] = (x1 + x2) / 2f
        result[1] = (y1 + y2) / 2f
    }
}
